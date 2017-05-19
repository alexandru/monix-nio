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
import java.nio.file.StandardOpenOption
import monix.execution.Scheduler
import monix.nio.file.AsyncFileChannel

private[file] trait AsyncFileChannelBuilders {
  /** Opens a channel for the given file reference, returning an
    * [[AsyncFileChannel]] instance for handling reads and writes.
    *
    * @param file is the file reference to open
    * @param options specifies options for opening the file
    *        (e.g. create, append, etc.)
    *
    * @param s is the `Scheduler` used for asynchronous computations
    */
  def apply(file: File, options: StandardOpenOption*)
    (implicit s: Scheduler): AsyncFileChannel
}
