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
* Time: 10:23 PM
*/

package com.github.brisk

import cluster.Server
import org.jboss.netty.bootstrap.ClientBootstrap
import java.util.concurrent.{TimeUnit, CountDownLatch, Executors}
import org.jboss.netty.channel._
import group.{ChannelGroup, DefaultChannelGroup}
import java.net.InetSocketAddress
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import socket.nio.NioClientSocketChannelFactory
import org.xerial.snappy.Snappy
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import Message._

case class BriskClient(host: String, port: Int, timeout: Long = 5000) extends Logging {
  val bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
    Executors.newCachedThreadPool()))
  val channels = new DefaultChannelGroup
  bootstrap.setOption("tcpNoDelay", true)
  bootstrap.setOption("keepAlive", true)

  def invoke(service: String, in: Message) = future {
    val handler = new ClientHandler(channels, in + (Message._service -> service), timeout)
    bootstrap.setPipelineFactory(new BriskClientPipelineFactory(handler))
    bootstrap.connect(new InetSocketAddress(host, port))
    handler.get
  }

  def invokeSync(service: String, in: Message) = {
    val future = invoke(service: String, in: Message)
    Await.result(future, timeout milliseconds)
  }

  def destroy() {
    channels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
    debug("Client " + host + ":" + port + " destroyed")
  }

  class BriskClientPipelineFactory(handler: ChannelHandler) extends ChannelPipelineFactory {

    def getPipeline = {
      val pipeline = org.jboss.netty.channel.Channels.pipeline()
      pipeline.addLast("handler", handler)
      pipeline
    }
  }

  lazy val getHost = Server(host, port)
}

class ClientHandler(channels: ChannelGroup, in: Message, timeout: Long) extends SimpleChannelUpstreamHandler with Logging {

  val done = new CountDownLatch(1)

  var out: Try[Message] = _

  def get = {
    try {
      done.await(timeout, TimeUnit.MILLISECONDS)
    } catch {
      case e: InterruptedException => throw e
    }
    out
  }

  override def messageReceived(ctx: ChannelHandlerContext, event: MessageEvent) {
    debug("Message received in client")
    val bytes = event.getMessage.asInstanceOf[ChannelBuffer].array()
    val message = (Message.decode(Snappy.uncompress(bytes)))
    if (message.underlying.containsField(_error))
      out = Failure(new IllegalAccessException(message.getAs[String](_error)))
    else
      out = Success(message)
    debug("out=" + out)
    ctx.getChannel.close().awaitUninterruptibly()
    done.countDown()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    warn("Client: Unexpected exception from downstream: %s".format(e.getCause))
    out = Failure(e.getCause)
    e.getChannel.close()
    done.countDown()
  }

  override def channelConnected(ctx: ChannelHandlerContext, event: ChannelStateEvent) {
    debug("Writting....")
    val channel = event.getChannel
    channels.add(channel)
    channel.write(ChannelBuffers.copiedBuffer(Snappy.compress(Message.encode(in))))
    debug("Written")
  }

}

