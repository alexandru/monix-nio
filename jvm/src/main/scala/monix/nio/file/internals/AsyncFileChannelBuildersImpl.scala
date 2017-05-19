/*
 * Copyright (c) 2014-2017 by its authors. Some rights reserved.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.nio.file.internals

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.{ AsynchronousFileChannel, CompletionHandler }
import java.nio.file.StandardOpenOption
import monix.eval.Callback
import monix.execution.{ Cancelable, Scheduler }
import monix.nio.file.AsyncFileChannel
import scala.concurrent.blocking
import scala.util.control.NonFatal

/** JVM specific implementation for [[AsyncFileChannelBuilders]]. */
private[file] class AsyncFileChannelBuildersImpl
  extends AsyncFileChannelBuilders {

  final override def apply(file: File, options: StandardOpenOption*)
    (implicit s: Scheduler): AsyncFileChannel = {

    import scala.collection.JavaConverters._
    new NewIOImplementation(
      AsynchronousFileChannel.open(
        file.toPath,
        options.toSet.asJava,
        ExecutorServiceWrapper(s)
      )
    )
  }

  /** Implementation for [[AsyncFileChannel]] that uses Java's NIO. */
  private final class NewIOImplementation(underlying: AsynchronousFileChannel)
    (implicit scheduler: Scheduler) extends AsyncFileChannel {

    override def isOpen: Boolean =
      underlying.isOpen

    private[this] val cancelable: Cancelable =
      Cancelable { () =>
        try underlying.close() catch {
          case NonFatal(ex) => scheduler.reportFailure(ex)
        }
      }
    override def close(): Unit =
      cancelable.cancel()

    override def size(cb: Callback[Long]): Unit =
      scheduler.executeAsync { () =>
        var streamErrors = true
        try {
          val size = underlying.size()
          streamErrors = false
          cb.onSuccess(size)
        } catch {
          case NonFatal(ex) =>
            if (streamErrors) cb.onError(ex)
            else scheduler.reportFailure(ex)
        }
      }

    override def read(dst: ByteBuffer, position: Long, cb: Callback[Int]): Unit = {
      require(position >= 0, "position >= 0")
      require(!dst.isReadOnly, "!dst.isReadOnly")
      try {
        underlying.read(dst, position, cb, completionHandler) // Can throw NonReadableChannelException
      } catch {
        case NonFatal(ex) =>
          cb.onError(ex)
      }
    }

    override def write(src: ByteBuffer, position: Long, cb: Callback[Int]): Unit = {
      require(position >= 0, "position >= 0")
      try underlying.write(src, position, cb, completionHandler)
      catch { case NonFatal(ex) => cb.onError(ex) }
    }

    override def flush(metaData: Boolean, cb: Callback[Unit]): Unit =
      scheduler.executeAsync { () =>
        try blocking {
          underlying.force(true)
          cb.onSuccess(())
        } catch {
          case NonFatal(ex) =>
            cb.onError(ex)
        }
      }

    private[this] val completionHandler =
      new CompletionHandler[Integer, Callback[Int]] {
        def completed(result: Integer, cb: Callback[Int]): Unit =
          cb.onSuccess(result)
        def failed(exc: Throwable, cb: Callback[Int]): Unit =
          cb.onError(exc)
      }
  }
}
