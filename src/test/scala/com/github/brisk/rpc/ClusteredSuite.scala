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
import com.github.brisk.{Clients, Message}
import com.github.brisk.cluster.ClusteredBrisk
import com.github.brisk.Utils._

class ClusteredSuite extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  var servers: List[ClusteredBrisk] = Nil

  test("clustered basic test") {
    val b1 = new ClusteredBrisk(9191, "test-cluster") {
      service("foo") {
        in => Message("status" -> 100, "time" -> System.currentTimeMillis())
      }
    }
    val b2 = cloneClusteredBrisk(b1, 9192)
    val b3 = cloneClusteredBrisk(b1, 9193)
    val b4 = cloneClusteredBrisk(b1, 9194)
    servers = List(b1, b2, b3, b4)

    servers.foreach(_.start())

    val client = Clients.clustered("test-cluster")


    for (_ <- 1 to 100) {
      val out = client.invokeSync("foo").get
      out.get("status") should be(100)
      out.getAs[Long]("time") should be > (0L)
    }

    client.destroy()
  }

  override def beforeEach() {
  }

  override def afterEach() {
    servers.foreach(_.stop())
  }

}