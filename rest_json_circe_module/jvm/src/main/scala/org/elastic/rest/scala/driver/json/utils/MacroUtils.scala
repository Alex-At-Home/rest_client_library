package org.elastic.rest.scala.driver.json.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, RestDriver}
import org.elastic.rest.scala.driver.utils.MacroUtils.getOpType

import scala.reflect.macros.blackbox
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.reflect.macros._



/**
  * TODO
  */
object MacroUtils {

  /**
    * TODO
    * @param c
    * @param driver
    * @param ec
    * @param ct
    * @tparam T
    * @return
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

    c.Expr[Future[T]] {
      q"""
        import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
        import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

         $driver.exec($self.typedOp.asInstanceOf[BaseDriverOp])
          .map(s => decode[$ct](s).fold(e => throw new Exception(e.toString), v => v))
        """
    }
  }

  /**
    *
    * @param c
    * @param body
    * @param ctd
    * @param cti
    * @tparam D
    * @tparam I
    * @return
    */
  def writeMaterialize[D <: BaseDriverOp, I]
    (c: blackbox.Context)(body: c.Expr[I])
    (implicit ctd: c.WeakTypeTag[D], cti: c.WeakTypeTag[I])
    : c.Expr[D] =
  {
    import c.universe._

    val self = c.prefix
    val opType = getOpType(c).toLowerCase

    val q1 =
      q"""
        import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
        import org.elastic.rest.scala.driver.RestBase.BaseDriverOp

        val toWrite = $body.asJson.noSpaces
        val typedResource = $self.resource.asInstanceOf[RestWritable[$ctd] with RestResource]
       """

    c.Expr[D] {
      opType match {
        case "get" | "read" => q""" ..$q1
             typedResource.read(toWrite)"""

        case "put" | "write" => q""" ..$q1
             typedResource.write(toWrite)"""

        case "post" | "send" => q""" ..$q1
             typedResource.send(toWrite)"""

        case "delete" => q""" ..$q1
             typedResource.delete(toWrite)"""

        case _ => throw new Exception(
          s"Invalid opType, must be one of GET/read, PUT/write, SEND/post, DELETE/delete: $opType")
      }
    }
  }

}
