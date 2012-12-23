/*
* Copyright 2011 P.Budzik
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
*
* User: przemek
* Date: 12/16/12
* Time: 2:23 PM
*/
package com.github.brisk

import cluster.Server
import java.util.concurrent.atomic.AtomicInteger

object Predicates {

  def all(server: Server) = true

}

case class CountPredicate(count: Int) {
  val counter = new AtomicInteger

  def atLeast(message: Message): Boolean = {
    if (counter.get() >= count) true
    else {
      counter.incrementAndGet()
      false
    }
  }

  def atLeastThat(message: Message)(p: Message => Boolean): Boolean = atLeast(message) && p(message)

}
