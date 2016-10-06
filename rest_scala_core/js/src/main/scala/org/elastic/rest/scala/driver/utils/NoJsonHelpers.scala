package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.{CustomTypedToString, RestRequestException}
import org.elastic.rest.scala.driver.RestBaseTyped.TypedToStringHelper

/**
  * Supports typed object for custom classes only, ie if no JSON library is used
  * for some reason
  */
object NoJsonHelpers {

  /**
    * Include this to support a typed API made entirely out of custom classes
    */
  implicit val NoJsonTypedToStringHelper = new TypedToStringHelper() {
    def fromTyped[T](t: T): String = t match {
      case custom: CustomTypedToString => custom.fromTyped
      case _ => throw RestRequestException(s"Type ${t.getClass} not supported with JSON lib")
    }
  }
}
