package org.elastic.rest.scala.driver.json

import io.circe.jawn._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.elastic.rest.scala.driver.RestBase._
import org.elastic.rest.scala.driver.RestBaseTyped._
import org.elastic.rest.scala.driver.RestResources.RestWritableTU
import org.elastic.rest.scala.driver.utils.NoJsonHelpers
import org.elastic.rest.scala.driver.json.utils.MacroUtils
import org.elastic.rest.scala.driver.utils.MacroUtils.OpType

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime._
import scala.reflect.runtime.universe._
import scala.language.experimental.macros

/** Integration for CIRCE with REST drivers
  * to decide which JSON lib to use for typed on a case by case....
  */
object CirceTypeModule {

  /**
    * TODO
    * @param typedOp
    * @tparam T
    */
  implicit class TestStringToTypeHelper[T](val typedOp: TypedOperation[T]) extends StringToTypedImplicitBase[T] {

    /**
      * TODO
      * @param driver
      * @param ec
      * @return
      */
    override def testExec()(implicit driver: RestDriver, ec: ExecutionContext): Future[T] =
      macro MacroUtils.execMaterialize[T]
  }

  implicit class TestWriteHelper[D <: BaseDriverOp, I](val resource: RestWritableTU[D, I]) extends TypedToStringImplicitBaseWritableTU[D, I] {
    @OpType("write")
    override def testWrite(body: I): D = macro MacroUtils.writeMaterialize[D, I]
  }

  //////////////////////////////////////////////////////////////////////////////

  // These have to be at the front for some reason
  // See below for the implicits

  /** A lazily constructed map of auto-generated encoders */
  val encoderRegistry: scala.collection.concurrent.Map[String, Encoder[_]] =
    new java.util.concurrent.ConcurrentHashMap[String, Encoder[_]]().asScala

  /** A lazily constructed map of auto-generated decoders */
  val decoderRegistry: scala.collection.concurrent.Map[String, Decoder[_]] =
    new java.util.concurrent.ConcurrentHashMap[String, Decoder[_]]().asScala

  /** Looks inside a case class to find an auto-generated encoder/decoder
    *
    * @param picker Decoder/Encoder check
    * @param ct Weak Type Tag of case class
    * @tparam T case class
    * @return List of matching encoder/decoder instances
    */
  def generatedXcoder[T]
  (picker: universe.Type => Boolean)
  (implicit ct: universe.WeakTypeTag[T]) = {

    val companionMirror =
      scala.util.Try { //(easy case, the inner class is in an object)
        currentMirror.reflectModule(ct.tpe.typeSymbol.companion.asModule)
      }.getOrElse {
        //(difficult case, the inner class is in a trait)
        val outerMirror = NoJsonHelpers.getOuterInstanceMirror(ct)
        // Create an instance mirror for T from the outer module:
        outerMirror.reflectModule(ct.tpe.typeSymbol.companion.asModule)
      }

    ct.tpe.companion.members.toList
      .filter(_.isMethod)
      .filter(_.isImplicit)
      .map(_.asMethod)
      .filter(m => picker(m.returnType))
      .map { t => currentMirror
        .reflect(companionMirror.instance)
        .reflectMethod(t).apply()
      }
  }

  /** Finds the decoder inside a case class
    * (which must have been annotated with @JsonCodec
    * or at least have a companion object containing an implicit decoder)
    *
    * @param ct Weak Type Tag of case class
    * @tparam T case class
    * @return A decoder to use and cache
    */
  def getGeneratedDecoder[T](implicit ct: universe.WeakTypeTag[T]) = {
    generatedXcoder[T](t => t <:< typeOf[Decoder[_]])
      .head.asInstanceOf[Decoder[T]]
  }

  /** Finds the encoder inside a case class
    * (which must have been annotated with @JsonCodec
    * or at least have a companion object containing an implicit encoder)
    *
    * @param ct Weak Type Tag of case class
    * @tparam T case class
    * @return An encoder to use and cache
    */
  def getGeneratedEncoder[T](implicit ct: universe.WeakTypeTag[T]) = {
    generatedXcoder[T](t => t <:< typeOf[Encoder[_]])
      .head.asInstanceOf[Encoder[T]]
  }

  // Implicits

  /** Typed inputs */
  implicit val typedToStringHelper = new TypedToStringHelper() {
    override def fromTyped[T](t: T)(implicit ct: WeakTypeTag[T]): String = t match {
      case custom: CustomTypedToString => custom.fromTyped
      case _ =>
        //(lazily build a registry of encoders)
        val encoder = encoderRegistry
          .getOrElseUpdate(ct.tpe.toString, getGeneratedEncoder[T])

        t.asJson(encoder.asInstanceOf[Encoder[T]]).noSpaces
    }
  }

  /** Typed outputs */
  implicit val stringToTypedHelper = new StringToTypedHelper() {
    override def toType[T](s: String)(implicit ct: WeakTypeTag[T]): T = {
      if (ct.tpe <:< typeOf[CustomStringToTyped]) {
        NoJsonHelpers.createCustomTyped(s)
      }
      else { // normal cases
        //(lazily build a registry of decoders)
        val decoder = decoderRegistry
          .getOrElseUpdate(ct.tpe.toString, getGeneratedDecoder[T])

        decode[T](s)(decoder.asInstanceOf[Decoder[T]])
          .leftMap({ err =>
            throw RestServerException(200, s"JSON serialization error: $err", Option(s)) }
          ).toOption.get
      }
    }
  }
}
