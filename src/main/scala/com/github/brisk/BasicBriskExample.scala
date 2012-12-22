package com.github.brisk

import util.{Failure, Success}

object BasicBriskExample extends App {
  val server = new Brisk(8080) {
    service("foo") {
      in => Message("status" -> 200, "time" -> System.currentTimeMillis())
    }
  }
  server.start()

  val client = Clients.create("localhost", 8080)
  client.invokeSync("foo") match {
    case Success(out) => {
      println(out.status)
      println(out.time)
    }
    case Failure(e) => throw e
  }

  server.stop()
  client.destroy()
}