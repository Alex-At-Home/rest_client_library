package org.elastic.rest.scala.driver.json

import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

//TODO dynamic vs object - former input latter output?

/** Integration for ScalaJS with REST drivers
  * to decide which JSON lib to use for typed on a case by case....
  */
object ScalaJsJsonModule {

  /** JSON inputs */
  implicit val jsonToStringHelper = new JsonToStringHelper[js.Dynamic] {
    override def fromJson(j: js.Dynamic): String = js.JSON.stringify(j)
  }

  /** JSON outputs */
  implicit class StringToCirceHelper(op: BaseDriverOp) extends StringToJsonHelper[js.Object] {
    /** Actually executes the operation (async)
      *
      * @param driver The driver which executes the operation
      * @return A future containing the result of the operation as JSON
      */
    def execJ()(implicit driver: RestDriver): Future[js.Object] = {
      driver.exec(op)
        .map(s => toJson(s).asInstanceOf[js.Object])
    }
  }

  // Utilities

  /** Utility method to convert string to JSON
    *
    * @param s The JSON string to parse
    * @return Parsed string
    */
  private def toJson(s: String): js.Dynamic = js.JSON.parse(s)
}
