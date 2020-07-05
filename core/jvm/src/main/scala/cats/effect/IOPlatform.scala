/*
 * Copyright 2020 Typelevel
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

package cats.effect

import scala.concurrent.ExecutionContext

import java.util.concurrent.CountDownLatch

private[effect] abstract class IOPlatform[+A] { self: IO[A] =>

  final def unsafeRunSync(ec: ExecutionContext, timer: UnsafeTimer): A = {
    @volatile
    var results: Either[Throwable, A] = null
    val latch = new CountDownLatch(1)

    unsafeRunFiber(ec, timer, false) { e =>
      results = e
      latch.countDown()
    }

    latch.await()
    results.fold(
      throw _,
      a => a)
  }
}