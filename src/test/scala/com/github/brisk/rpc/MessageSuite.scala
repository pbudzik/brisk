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
* Date: 7/2/11
* Time: 12:07 PM
*/

package com.github.brisk.rpc

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import com.github.brisk.Message

class MessageSuite extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  test("encode/decode message") {
    val msg = Message("foo" -> 1)
    Message.decode(Message.encode(msg)) should equal(msg)
  }

  test("dynamic message") {
    val msg = Message("foo" -> 1, "bar" -> true, "baz" -> "LONVXTHYTFDSMI")
    msg.foo should be(1)
    msg.bar should equal(true)
    msg.baz should equal("LONVXTHYTFDSMI")
  }

}