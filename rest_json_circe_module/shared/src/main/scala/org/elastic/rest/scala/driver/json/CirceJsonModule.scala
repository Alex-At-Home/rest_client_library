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

  /** A specialized version of `DeclareJson` that is created from a string */
  trait DeclareJsonFromString extends DeclareJson{
    /** The string that generates the JSON */
    def asString: String

    /** The JSON object over which we are generating the view */
    lazy val asJson: JsonObject = asParsedRawJson.left.map(throw _).right.toOption.flatMap(_.asObject).get

    /** The very basic JSON-or-parse error generated from `io.circe.parser.parse` */
    lazy val asParsedRawJson: Either[ParsingFailure, Json] = parse(asString)
  }

  /** A trait with some useful methods for making creating a JSON view more declarative */
  trait DeclareJson {
    /** The JSON object over which we are generating the view */
    def asJson: JsonObject
    /** In CIRCE, `JsonObject` isn't a subclass of `Json` - use this wrapper */
    def asRawJson = Json.fromJsonObject(asJson)

    /** Maps a lens to a non-JSON type (atomic or case class) to its actual value, using a default if it doesn't exist */
    def registerTyped[T](lens: monocle.Optional[Json, T], default: T): T = lens.getOption(asRawJson).getOrElse(default)
    /** Maps a lens to a non-JSON type (atomic or case class) to its actual (optional) value */
    def registerTyped[T](lens: monocle.Optional[Json, T]): Option[T] = lens.getOption(asRawJson)
    /** Maps a lens to an array of non-JSON types (atomic or case class) to a list (empty if not present) */
    def registerTyped[T](lens: monocle.Traversal[Json, T]): List[T] = lens.getAll(asRawJson)
    /** Maps a lens to a JSON object to its actual (optional) value */
    def registerJson(lens: monocle.Optional[Json, Json]): Option[JsonObject] = lens.getOption(asRawJson).flatMap(_.asObject)
    /** Maps a lens to an array of JSON objects to a list (empty if not present) */
    def registerJson(lens: monocle.Traversal[Json, Json]): List[JsonObject] = lens.getAll(asRawJson).flatMap(_.asObject)
  }

}
