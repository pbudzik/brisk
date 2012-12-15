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
* Date: 7/10/11
* Time: 9:48 PM
*/

package com.github.brisk.cluster

import org.jgroups._

trait Clustered {
  System.setProperty("java.net.preferIPv4Stack", "true")
  val channel = new JChannel
  var members = emptySet

  ClusterUtils.configure(channel)

  lazy val emptySet = collection.mutable.Set.empty[Server]

  private def buildUniqueName(port: Int) = ClusterUtils.nextMemberId + ":" + port

  def connectCluster(cluster: String, port: Int = 0) {
    channel.setName(buildUniqueName(port))
    registerReceiver(cluster)
  }

  def disconnectCluster() {
    channel.close()
  }

  def membersRegistered(members: Set[Server])

  def membersLost(members: Set[Server])

  private def asServer(a: Address) = {
    val s = a.toString
    if (!s.contains(":")) throw new IllegalArgumentException("Invalid address format")
    val Array(ip, _, port, _*) = s.split(":")
    Server(ip, port.toInt)
  }

  private def registerReceiver(cluster: String) {

    channel.setReceiver(new ReceiverAdapter() {

      override def viewAccepted(view: View) {
        import scala.collection.JavaConversions._
        val currentMembers = (for (member <- view.getMembers) yield asServer(member)).toSet
        val newMembers = currentMembers.filter(!members.contains(_))
        val lostMembers = members.filter(!currentMembers.contains(_))
        members = emptySet ++ currentMembers
        if (newMembers.nonEmpty)
          membersRegistered(newMembers.toSet)
        if (lostMembers.nonEmpty)
          membersLost(lostMembers.toSet)
      }
    })

    channel.connect(cluster)
  }
}

sealed case class Server(host: String = "localhost", port: Int) {
  override def toString = host + ":" + port
}

