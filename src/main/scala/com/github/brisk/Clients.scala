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

object Clients {
  def multiNode(servers: Server*) = new MultiNodeBriskClient(servers)

  def clustered(cluster: String) = new ClusteredBriskClient(cluster)
}

class MultiNodeBriskClient(servers: Seq[Server]) extends RPC {
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