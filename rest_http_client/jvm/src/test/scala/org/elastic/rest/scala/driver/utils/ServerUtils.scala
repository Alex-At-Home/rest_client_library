package org.elastic.rest.scala.driver.utils

import akka.actor.ActorSystem
import colossus._
import core._
import protocols.http._

import scala.util.Try

/** Utilities for test HTTP client
  */
object ServerUtils {

  /** Akka actor system on which Colossus depends */
  implicit val actorSystem = ActorSystem.create("Test")
  /** Colossus IO system on which everything depends */
  implicit val system = IOSystem()

  /** Finds an open port and binds the test server
    *
    * @param handler The route handlers
    * @return The created server and the port it is bound to
    */
  def createTestServer(handler: ServerContext => HttpService): (ServerRef, Int) = {

    // Assign a port that works
    val (server, port) = (9000 to 32000).flatMap { port =>
      val serverSettings = ServerSettings(port = port, bindingRetry = NoRetry)
      val tryServer = Try {
        Server.basic("test-server", serverSettings)(handler)
      }
      tryServer.map(server => List((server, port))).getOrElse(List())

    }.head

    (server, port)
  }

}
