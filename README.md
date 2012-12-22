#### Brisk - Lightweight RPC for Scala ####

* minimalistic, less is more
* BSON and Mongo style objects + syntactic sugar
* clustering supported, no external tools needed (e.g. Zookeeper)
* based on Netty, async/sync mode
* Scala 2.10 ready (Future/Promise/Try, Dynamics)

### Example ###

Basic single node case:

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

Clustered case:

```scala
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
```
[See the source](https://github.com/pbudzik/brisk/blob/master/src/main/scala/com/github/brisk/example/BasicBriskExample.scala)

Other:

```scala
//invoke across all servers
val all = client.invokeAll("foo")
//blocking await
all.foreach(Await.result(_, 5 seconds))
```

Predicates:

```scala
//completion predicate -> at least 2 results collected
//server predicate -> take all servers
 val result = client.invokeSpecific("foo", Message(), CountPredicate(2).atLeast, all)
```

[See the tests](https://github.com/pbudzik/brisk/tree/master/src/test/scala/com/github/brisk/rpc)

### Install ###

**sbt**

Dependencies:

    "com.github.brisk" %% "brisk" % "1.0-SNAPSHOT"

Repos:

    "sonatype-snapshots" at "https://oss.sonatype.org/content/groups/public"

