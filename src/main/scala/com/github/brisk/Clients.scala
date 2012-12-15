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
* Date: 12/13/12
* Time: 8:18 PM
*/

package com.github.brisk

import cluster.{Server, Clustered}
import collection.mutable.Map
import java.util.concurrent.atomic.AtomicInteger

object Clients {
  def multiNode(servers: Server*) = new GroupBriskClient(servers)

  def clustered(cluster: String) = new ClusteredBriskClient(cluster)
}

trait RPC {

  val clients: Map[Server, BriskClient]

  def invokeSync(service: String, in: Message = Message(), selector: Selector = RoundRobin) =
    selector.selectOne(clients).invokeSync(service, in)

  def invoke(service: String, in: Message = Message(), selector: Selector = RoundRobin) =
    selector.selectOne(clients).invoke(service, in)

  def invokeAll(service: String, in: Message = Message()): List[Message] =
    clients.values.par.map(_.invokeSync(service, in)).toList

  def destroy()

  trait Selector {
    def selectOne(client: Map[Server, BriskClient]): BriskClient
  }

  object RoundRobin extends Selector {
    val counter = new AtomicInteger

    def selectOne(clients: Map[Server, BriskClient]) =
      if (clients.isEmpty) throw new Exception("No Brisk servers connected to cluster")
      else
        clients.values.toList(counter.incrementAndGet() % clients.size)

  }

}

class GroupBriskClient(servers: Seq[Server]) extends RPC {
  val clients = Map[Server, BriskClient]() ++ servers.map {
    server => (server, new BriskClient(server.host, server.port))
  }

  def destroy() {
    clients.values.foreach(_.destroy())
  }
}

class ClusteredBriskClient(cluster: String) extends RPC with Clustered {
  val clients = Map[Server, BriskClient]()

  connectCluster(cluster)

  def membersRegistered(members: Set[Server]) {
    clients.synchronized {
      for (server <- members; if server.port > 0)
        clients += (server -> new BriskClient(server.host, server.port))
    }
  }

  def membersLost(members: Set[Server]) {
    clients.synchronized {
      for (server <- members) {
        clients(server).destroy()
        clients -= server
      }
    }
  }

  def destroy() {
    disconnectCluster()
    clients.values.foreach(_.destroy())
  }


}