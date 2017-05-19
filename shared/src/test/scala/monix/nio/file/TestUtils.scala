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

package monix.nio.file

import java.io.File
//import java.nio.ByteBuffer
//import java.nio.file.StandardOpenOption
//import monix.execution.misc.NonFatal
//import monix.execution.schedulers.TrampolineExecutionContext.immediate
//import scala.concurrent.{ExecutionContext, Future, Promise}

object TestUtils {
  /** Returns a temporary path for a file that doesn't exist yet. */
  def createTempFilePath(prefix: String, suffix: String): String = {
    val file = File.createTempFile(prefix, suffix)
    if (file.exists()) file.delete()
    file.getAbsolutePath
  }

//  def readIntoString(file: File)
//    (implicit ec: ExecutionContext): Future[String] = {
//
//    if (!file.exists()) Future.successful("") else {
//      val channel = AsyncFileChannel(file, StandardOpenOption.READ)
//      val p = Promise[String]()
//      readIntoString(file).onComplete { r =>
//        try {
//          channel.close()
//          p.complete(r)
//        } catch {
//          case NonFatal(e) =>
//            if (!p.tryFailure(e)) ec.reportFailure(e)
//        }
//      }(immediate)
//      p.future
//    }
//  }
//
//  def readIntoString(channel: AsyncFileChannel)
//    (implicit ec: ExecutionContext): Future[String] = {
//
//    def loop(buffer: ByteBuffer, array: Array[Byte], position: Long, acc: StringBuilder): Future[String] =
//      channel.read(buffer, position).flatMap { readBytes =>
//        if (readBytes < 0)
//          Future.successful(acc.toString())
//        else {
//          if (readBytes > 0) {
//            acc.append(new String(array, 0, readBytes))
//            buffer.position(0)
//          }
//
//          loop(buffer, array, position+readBytes, acc)
//        }
//      }
//
//    val array = new Array[Byte](1024)
//    val buffer = ByteBuffer.wrap(array)
//    loop(buffer, array, 0, new StringBuilder)
//  }
}
