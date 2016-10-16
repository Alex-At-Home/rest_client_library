package org.elastic.rest.scala.driver.json.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, Modifier, RestDriver, TypedDriverOp}
import org.elastic.rest.scala.driver.RestBaseImplicits.{CustomStringToTyped, CustomTypedToString}
import org.elastic.rest.scala.driver.utils.MacroUtils.getOpType

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.macros.blackbox

/**
  * Utilities to handle typed APIs via CIRCE
  * Use macros to support ScalaJS as well as JVM (CIRCE doesn't really provide much runtime support anyway)
  */
object MacroUtils {

  /** Executes a typed operation using the provided driver
    *
    * @param c The macro context
    * @param driver The driver that actually execute the request
    * @param ec The execution context
    * @param ct The class tag for the output type
    * @tparam T The output type
    * @return A future containing the typed result
    */
  def execMaterialize[T]
    (c: blackbox.Context)
    ()
    (driver: c.Expr[RestDriver], ec: c.Expr[ExecutionContext])
    (implicit ct: c.WeakTypeTag[T])
    : c.Expr[Future[T]] =
  {
    import c.universe._

    val self = c.prefix

    if (ct.tpe <:< typeOf[CustomStringToTyped]) { // (has a single constructor taking a string)
      c.Expr[Future[T]] {
        q"""
          import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

           $driver.exec($self.typedOp.asInstanceOf[BaseDriverOp])
            .map(s => new $ct(s))
          """
      }
    }
    else {
      c.Expr[Future[T]] {
        q"""
          import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
          import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

           $driver.exec($self.typedOp.asInstanceOf[BaseDriverOp])
            .map(s => decode[$ct](s).fold(e => throw new Exception(e.toString), v => v))
          """
      }
    }
  }

  /** Creates a string out of the supplied type and builds an operation using the
    * `OpType` annotation - this is for untyped outputs (see `typedWriteMaterialize` where the output is also typed)
    *
    * @param c The macro context
    * @param body The (typed) body of the operation
    * @param ctd Classtag for the resource being operated on
    * @param cti Classtag for the input type
    * @tparam D The resource operation type
    * @tparam I The input type
    * @return The operated resource
    */
  def writeMaterialize[D <: Modifier, I]
    (c: blackbox.Context)(body: c.Expr[I])
    (implicit ctd: c.WeakTypeTag[D], cti: c.WeakTypeTag[I])
    : c.Expr[D with BaseDriverOp] =
  {
    import c.universe._

    val self = c.prefix
    val opType = getOpType(c).toLowerCase

    val q1 =
      if (cti.tpe <:< typeOf[CustomTypedToString]) {
        q"""import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
          import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

          val toWrite = $body.asInstanceOf[CustomTypedToString].fromTyped
         """
      }
      else {
        q"""import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
          import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

          val toWrite = $body.asJson.noSpaces
         """
      }

    c.Expr[D with BaseDriverOp] {
      opType match {
        case "get" | "read" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestWithDataReadable[$ctd] with RestResource]
             typedResource.readS(toWrite)"""

        case "put" | "write" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestWritable[$ctd] with RestResource]
             typedResource.writeS(toWrite)"""

        case "post" | "send" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestSendable[$ctd] with RestResource]
             typedResource.sendS(toWrite)"""

        case "delete" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestWithDataDeletable[$ctd] with RestResource]
             typedResource.deleteS(toWrite)"""

        case _ => throw new Exception(
          s"Invalid opType, must be one of GET/read, PUT/write, SEND/post, DELETE/delete: $opType")
      }
    }
  }

  /** Creates a string out of the supplied type and builds an operation using the
    * `OpType` annotation - this is for untyped outputs (see `typedWriteMaterialize` where the output is also typed)
    *
    * @param c The macro context
    * @param body The (typed) body of the operation
    * @param ctd Classtag for the resource being operated on
    * @param cti Classtag for the input type
    * @tparam D The resource operation type
    * @tparam I The input type
    * @return The operated resource
    */
  def writeTypedOutputMaterialize[D <: Modifier, I, O]
    (c: blackbox.Context)(body: c.Expr[I])
    (implicit ctd: c.WeakTypeTag[D], cti: c.WeakTypeTag[I], cto: c.WeakTypeTag[O])
    : c.Expr[D with TypedDriverOp[O]] =
  {
    import c.universe._

    val self = c.prefix
    val opType = getOpType(c).toLowerCase

    val q1 =
      if (cti.tpe <:< typeOf[CustomTypedToString]) {
        q"""import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
          import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

          val toWrite = $body.asInstanceOf[CustomTypedToString].fromTyped
         """
      }
      else {
        q"""import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
          import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

          val toWrite = $body.asJson.noSpaces
         """
      }

    c.Expr[D with TypedDriverOp[O]] {
      opType match {
        case "get" | "read" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestWithDataReadableTT[$ctd, $cti, $cto] with RestResource]
             typedResource.readS(toWrite)"""

        case "put" | "write" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestWritableTT[$ctd, $cti, $cto] with RestResource]
             typedResource.writeS(toWrite)"""

        case "post" | "send" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestSendableTT[$ctd, $cti, $cto] with RestResource]
             typedResource.sendS(toWrite)"""

        case "delete" => q""" ..$q1
             val typedResource = $self.resource.asInstanceOf[RestWithDataDeletableTT[$ctd, $cti, $cto] with RestResource]
             typedResource.deleteS(toWrite)"""

        case _ => throw new Exception(
          s"Invalid opType, must be one of GET/read, PUT/write, SEND/post, DELETE/delete: $opType")
      }
    }
  }
}
