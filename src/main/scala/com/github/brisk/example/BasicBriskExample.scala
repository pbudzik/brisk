package com.github.brisk.example

import util.{Failure, Success}
import com.github.brisk.{Clients, Message, Brisk}
import concurrent.Await
import scala.concurrent.duration._
import com.github.brisk.cluster.ClusteredBrisk

object BasicBriskExample extends App {
  val server = new Brisk(8080) {
    service("foo") {
      in => Message("status" -> 200, "time" -> System.currentTimeMillis())
    }
  }
  server.start()

  val client = Clients.create("localhost", 8080)
  //sync invocation
  client.invokeSync("foo") match {
    case Success(out) => {
      println(out.status)
      println(out.time)
    }
    case Failure(e) => throw e
  }
  //async invocation
  val future = client.invoke("foo")
  //....
  Await.result(future, 5 seconds) match {
    case Success(out) => {
      println(out.status)
      println(out.time)
    }
    case Failure(e) => throw e
  }
  server.stop()
  client.destroy()
}

object ClusteredBriskExample extends App {
  val s1 = new ClusteredBrisk(8080, "test-cluster") {
    service("foo") {
      in => Message("node" -> 1, "status" -> 100, "time" -> System.currentTimeMillis())
    }
  }
  val s2 = new ClusteredBrisk(8081, "test-cluster") {
    service("foo") {
      in => Message("node" -> 2, "status" -> 100, "time" -> System.currentTimeMillis())
    }
  }
  val servers = List(s1, s2)
  servers.foreach(_.start())

  val client = Clients.clustered("test-cluster")

  //by default it is round-robin
  invoke()
  invoke()
  invoke()

  client.destroy()

  servers.foreach(_.stop())

  def invoke() {
    //sync invocation
    client.invokeSync("foo") match {
      case Success(out) => {
        println("node: " + out.node)
      }
      case Failure(e) => throw e
    }
  }
}