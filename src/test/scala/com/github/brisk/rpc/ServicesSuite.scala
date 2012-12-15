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
import com.github.brisk.{Clients, Message, Brisk}
import com.github.brisk.cluster.Server

class ServicesSuite extends FunSuite with ShouldMatchers with BeforeAndAfterEach {

  var servers: List[Brisk] = _

  //  test("multinode basic test") {
  //    val b1 = new Brisk(9091) {
  //      service("foo") {
  //        in => Message("status" -> 100, "time" -> System.currentTimeMillis())
  //      }
  //    }
  //    val b2 = clone(b1, 9092)
  //    val b3 = clone(b1, 9093)
  //    val b4 = clone(b1, 9094)
  //    servers = List(b1, b2, b3, b4)
  //
  //    servers.foreach(_.start())
  //
  //    val client = Clients.multiNode(localhostServers(9091, 9092, 9093, 9094): _*)
  //
  //    for (_ <- 1 to 100) {
  //      val out = client.invokeSync("foo")
  //      out.get("status") should be(100)
  //      out.getAs[Long]("time") should be > (0L)
  //    }
  //
  //  }

  test("multinode invocations") {
    val b1 = new Brisk(9091) {
      service("foo") {
        in => Message("node" -> 1)
      }
    }
    val b2 = new Brisk(9092) {
      service("foo") {
        in => Message("node" -> 2)
      }
    }
    val b3 = new Brisk(9093) {
      service("foo") {
        in => Message("node" -> 3)
      }
    }

    servers = List(b1, b2, b3)

    servers.foreach(_.start())

    val client = Clients.multiNode(localhostServers(9091, 9092, 9093): _*)

    val out1 = client.invokeSync("foo")
    println(out1)
    val out2 = client.invokeSync("foo")
    println(out2)
    val out3 = client.invokeSync("foo")
    println(out3)

    val out4 = client.invokeAll("foo")
    println(out4)
  }

  def localhostServers(ports: Int*) = ports.map(Server("localhost", _))

  def clone(source: Brisk, port: Int) = {
    val brisk = new Brisk(port)
    source.services.foreach(e => brisk.services.put(e._1, e._2))
    brisk
  }

  override def beforeEach() {
  }

  override def afterEach() {
    servers.foreach(_.stop())
  }

}