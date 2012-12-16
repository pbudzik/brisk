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
* Date: 1/28/12
* Time: 3:04 PM
*/

package com.github.brisk

import org.jboss.netty.channel._
import group.ChannelGroup
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import org.xerial.snappy.Snappy

class BriskHandler(channels: ChannelGroup, handler: Message => Message) extends SimpleChannelUpstreamHandler with Logging {

  override def messageReceived(ctx: ChannelHandlerContext, event: MessageEvent) {
    debug("Message received in server")
    val inBytes = event.getMessage.asInstanceOf[ChannelBuffer].array()
    val in = (Message.decode(Snappy.uncompress(inBytes)))
    debug("Decoded in: " + in + ", Handler: " + handler)
    val out = handler(in)
    debug("Out: " + out)
    val outBytes: Array[Byte] = Message.encode(out)
    val channel = event.getChannel
    channels.add(channel)
    channel.write(ChannelBuffers.copiedBuffer(Snappy.compress(outBytes)))
    channel.close().awaitUninterruptibly()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    warn("Server: Unexpected exception from downstream: %s".format(e.getCause))
    e.getChannel.close()
    e.getCause.printStackTrace()
  }
}

