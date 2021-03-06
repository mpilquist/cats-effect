/*
 * Copyright 2017 Daniel Spiewak
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

package cats
package effect

import scala.concurrent.duration.Duration
import scala.util.Either

import java.util.concurrent.{CountDownLatch, TimeUnit}
import java.util.concurrent.atomic.AtomicReference

private[effect] object IOPlatform {

  def unsafeResync[A](ioa: IO[A], limit: Duration): Option[A] = {
    val latch = new CountDownLatch(1)
    val ref = new AtomicReference[Either[Throwable, A]](null)

    ioa unsafeRunAsync { a =>
      ref.set(a)
      latch.countDown()
    }

    if (limit == Duration.Inf)
      latch.await()
    else
      latch.await(limit.toMillis, TimeUnit.MILLISECONDS)

    Option(ref.get()).map(_.fold(throw _, a => a))
  }
}
