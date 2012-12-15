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
* Time: 6:11 PM
*/

package com.github.brisk.cluster

import org.jgroups.protocols._
import pbcast.{GMS, STABLE, NAKACK}
import org.jgroups.{JChannel}
import org.jgroups.stack.ProtocolStack
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicLong

object ClusterUtils {

  val memberIdSequence = new AtomicLong

  def configure(channel: JChannel) {
    val stack = new ProtocolStack(); // 2
    channel.setProtocolStack(stack); // 3
    stack.addProtocol(new UDP()
      //.setValue("bind_addr", InetAddress.getByName("192.168.1.11"))
      .setValue("ucast_recv_buf_size", 130000)
      .setValue("ucast_send_buf_size", 130000)
      .setValue("mcast_send_buf_size", 130000)
      .setValue("mcast_recv_buf_size", 130000))
      .addProtocol(new PING())
      .addProtocol(new MERGE2())
      .addProtocol(new FD_SOCK())
      .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
      .addProtocol(new VERIFY_SUSPECT())
      .addProtocol(new BARRIER())
      .addProtocol(new NAKACK())
      .addProtocol(new UNICAST2())
      .addProtocol(new STABLE())
      .addProtocol(new GMS())
      .addProtocol(new UFC())
      .addProtocol(new MFC())
      .addProtocol(new FRAG2()); // 4
    stack.init();
  }

  def nextMemberId = InetAddress.getLocalHost.getHostAddress + ":" + memberIdSequence.incrementAndGet()
}