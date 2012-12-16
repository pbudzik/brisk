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
import com.github.brisk.cluster.Server
import concurrent.Await
import concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import Utils._

class ServicesSuite extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  var servers: List[Brisk] = Nil

  test("multinode basic test") {
    val b1 = new Brisk(9091) {
      service("foo") {
        in => Message("status" -> 100, "time" -> System.currentTimeMillis())
      }
    }
    val b2 = cloneBrisk(b1, 9092)
    val b3 = cloneBrisk(b1, 9093)
    val b4 = cloneBrisk(b1, 9094)
    servers = List(b1, b2, b3, b4)

    servers.foreach(_.start())

    val client = Clients.multiNode(localServers(9091, 9092, 9093, 9094): _*)

    for (_ <- 1 to 100) {
      val out = client.invokeSync("foo")
      out.get("status") should be(100)
      out.getAs[Long]("time") should be > (0L)
    }

    client.destroy()
    println("---------------")
    servers.foreach(_.stop())
  }

  test("multinode invocations") {
    val b1 = new Brisk(9081) {
      service("foo") {
        in => Message("node" -> 1)
      }
    }
    val b2 = new Brisk(9082) {
      service("foo") {
        in => Message("node" -> 2)
      }
    }
    val b3 = new Brisk(9083) {
      service("foo") {
        in => Message("node" -> 3)
      }
    }

    servers = List(b1, b2, b3)

    servers.foreach(_.start())

    val client = Clients.multiNode(localServers(9081, 9082, 9083): _*)

    val nodes = Seq(1, 2, 3)

    val out1 = client.invokeSync("foo")
    assert(nodes contains (out1.get("node")))
    println(out1)
    println("\n\n")
    val out2 = client.invokeSync("foo")
    assert(nodes contains (out2.get("node")))
    println(out2)
    val out3 = client.invokeSync("foo")
    assert(nodes contains (out3.get("node")))
    println(out3)

    val out5 = client.invoke("foo")
    println(out5)
    Await.result(out5, Duration.create(5, TimeUnit.SECONDS))
    println(out5.value)

    val out4 = client.invokeAll("foo")
    println("futures: " + out4)
    out4.foreach(Await.result(_, Duration.create(5, TimeUnit.SECONDS)))

    val o = client.invokeSpecific("foo", Message(), CountPredicate(2).atLeast)
    Thread.sleep(500)
    println(o)
  }


  //-----------------------

  override def beforeEach() {
  }

  override def afterEach() {
    servers.foreach(_.stop())
  }

}