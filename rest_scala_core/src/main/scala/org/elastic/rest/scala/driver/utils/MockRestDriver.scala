package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, RestDriver, RestServerException}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** A mock driver that you pass the list of handlers into.
  * Note that BaseDriverOp can be decomposed as
  * BaseDriverOp(resource, method, body, params, headers)
  *
  * @param handler The partial function, a set of cases on the decomposed BaseDriverOp
  */
class MockRestDriver
  (handler: PartialFunction[BaseDriverOp, Future[String]]) extends RestDriver
{
  private val fallback: PartialFunction[BaseDriverOp, Future[String]] =
   { case _ =>
     Future.failed(RestServerException(404, "Resource not found", None))
   }

  override def exec(op: BaseDriverOp): Future[String] = (handler orElse fallback)(op)
  override def timeout: Duration = Duration("5 seconds")
}
