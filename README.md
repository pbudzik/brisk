#### Brisk - Lightweight RPC for Scala ####

* minimalistic, less is more
* BSON and Mongo style objects + syntactic sugar
* clustering supported, no external tools needed (e.g. Zookeeper)
* based on Netty, async/sync mode
* Scala 2.10 ready (Future/Promise/Try, Dynamics)

### Example ###

Server:
```scala
import util.{Failure, Success}

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
```


[See the tests](https://github.com/pbudzik/brisk/tree/master/src/test/scala/com/github/brisk/rpc)

### Install ###

**sbt**

Dependencies:

    "com.github.brisk" %% "brisk" % "1.0-SNAPSHOT"

Repos:

    "sonatype-snapshots" at "https://oss.sonatype.org/content/groups/public"

