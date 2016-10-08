package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.RestRequestException
import org.elastic.rest.scala.driver.RestBaseImplicits.CustomTypedToString
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped.{RuntimeStringToTypedHelper, RuntimeTypedToStringHelper}

/**
  * Supports typed object for custom classes only, ie if no JSON library is used
  * for some reason
  */
object NoJsonRuntimeHelpers {

  /**
    * Include this to support a typed API made entirely out of custom classes
    */
  implicit val NoJsonTypedToStringHelper = new RuntimeTypedToStringHelper() {
    def fromTyped[T](t: T): String = t match {
      case custom: CustomTypedToString => custom.fromTyped
      case _ => throw RestRequestException(s"Type ${t.getClass} not supported with JSON lib")
    }
  }

  /**
    * Include this to support a typed API made entirely out of custom classes
    * TODO implement this via macros
    */
  implicit val NoJsontringToTypedHelper = new RuntimeStringToTypedHelper() {
    override def toType[T](s: String): T = throw new Exception("NOT YET IMPLEMENTED")
  }
}
