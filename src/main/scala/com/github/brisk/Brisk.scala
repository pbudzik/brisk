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
* Date: 12/11/12
* Time: 9:52 PM
*/

package com.github.brisk

import cluster.{Server, Clustered}

class Brisk(port: Int) {

  lazy val server = new BriskServer(port)

  val services = new collection.mutable.HashMap[String, Message => Message]()

  def service(name: String)(handler: Message => Message) {
    services += (name -> handler)
  }

  def start() {
    server.configure(mainHandler)
    server.start()
  }

  def stop() {
    server.stop()
  }

  def mainHandler(in: Message) = {
    val service = in.getString(Message.Service)
    val handler = services.get(service)
    if (handler.nonEmpty)
      handler.get(in - Message.Service)
    else
      throw new Exception("No service defined: " + service)
  }

}

class ClusteredBrisk(port: Int, cluster: String) extends Brisk(port) with Clustered {

  override def start() {
    super.start()
    connectCluster(cluster, port)
  }

  override def stop() {
    disconnectCluster()
    super.stop()
  }

  def membersRegistered(members: Set[Server]) {}

  def membersLost(members: Set[Server]) {}
}

