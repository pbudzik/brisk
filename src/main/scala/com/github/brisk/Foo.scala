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
* Date: 12/8/12
* Time: 12:12 PM
*/

package com.github.brisk

object MyApp extends App {

  val brisk = new Brisk(9091) {
    service("foo") {
      in => Message("status" -> 100)
    }
  }

  brisk.start()
  val client = new BriskClient("127.0.0.1", 9091)
  val out = client.invokeSync("foo", Message("bar" -> 1, "x" -> "doooo"))

  println(out)
  client.destroy()
  brisk.stop()


  val cb1 = new ClusteredBrisk(9091, "fufo") {
    service("foo") {
      in => Message("status" -> 100, "time" -> System.nanoTime(), "x" -> in.get("a"))
    }
  }

  val cb2 = new ClusteredBrisk(9092, "fufo") {
    service("foo") {
      in => Message("status" -> 100, "time" -> System.nanoTime(), "x" -> in.get("a"))
    }
  }

  cb1.start()
  cb2.start()

  val clClient = Clients.clustered("fufo")

  println(clClient.invokeSync("foo", Message("a" -> 1)))
  println(clClient.invokeSync("foo", Message("a" -> 2)))
  println(clClient.invokeSync("foo", Message("a" -> 3)))

  println(clClient.invokeAll("foo", Message("a" -> 1)))

  val f = clClient.invoke("foo", Message("a" -> 1))

  println("---------------------- " + f)

  cb1.stop()

  println(clClient.invokeAll("foo", Message("a" -> 1)))

  cb2.stop()

  clClient.destroy()

  println("---------------------- " + f())
}