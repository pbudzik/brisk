package com.github.brisk.example

import util.{Failure, Success}
import com.github.brisk.{Clients, Message, Brisk}
import concurrent.Await
import scala.concurrent.duration._

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