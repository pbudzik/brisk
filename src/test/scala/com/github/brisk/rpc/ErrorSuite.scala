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
import com.github.brisk._
import concurrent.Await
import concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import Utils._

class ErrorSuite extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  var servers: List[Brisk] = Nil

  test("server is down") {
    val client = Clients.multiNode(localServers(9081, 9082): _*)
    val result = client.invokeSync("foo")
    result.isFailure should be(true)
    client.destroy()
  }

  test("incorrect service call") {
    servers = List(new Brisk(9099) {
      service("foo") {
        in => Message("status" -> 100, "time" -> System.currentTimeMillis())
      }
    })
    servers.foreach(_.start())
    val client = Clients.multiNode(localServers(9099): _*)
    client.invokeSync("foo1").isFailure should be(true)
    client.invokeSync("foo").isFailure should be(false)
    client.destroy()
  }

  //-----------------------

  override def beforeEach() {
  }

  override def afterEach() {
    servers.foreach(_.stop())
  }

}