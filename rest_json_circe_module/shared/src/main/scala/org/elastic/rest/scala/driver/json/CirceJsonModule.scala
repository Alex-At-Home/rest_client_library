package org.elastic.rest.scala.driver.json

import io.circe._
import io.circe.parser.parse
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** Integration for CIRCE with REST drivers
  * to decide which JSON lib to use for typed on a case by case....
  */
object CirceJsonModule {

  /** JSON inputs */
  implicit val jsonToStringHelper = new JsonToStringHelper[Json] {
    override def fromJson(j: Json): String = j.noSpaces
  }

  /** JSON outputs */
  implicit class StringToCirceHelper(op: BaseDriverOp) extends StringToJsonHelper[Json] {
    /** Actually executes the operation (async)
      *
      * @param driver The driver which executes the operation
      * @return A future containing the result of the operation as JSON
      */
    def execJ()(implicit driver: RestDriver): Future[Json] = {
      driver.exec(op)
        .map(s => toJson(s))
    }
  }

  // Utilities

  /** Utility method to convert string to JSON
    *
    * @param s The JSON string to parse
    * @return Parsed string
    */
  private def toJson(s: String): Json = {
    parse(s).left.map { err =>
      throw RestServerException(200, s"JSON serialization error: $err", Option(s))
    }.right.getOrElse(Json.Null)
  }
}
