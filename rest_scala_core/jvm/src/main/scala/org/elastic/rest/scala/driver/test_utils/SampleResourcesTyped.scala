package org.elastic.rest.scala.driver.test_utils

import org.elastic.rest.scala.driver.RestBaseTyped._
import org.elastic.rest.scala.driver.test_utils.SampleResources.{InWrapper, OutWrapper}

import scala.reflect.runtime.universe

/**
  * Useful sample REST resources for testing
  */
object SampleResourcesTyped {

  /** Dummy helper to convert `InWrapper` to string */
  implicit val myTypedToStringHelper = new  TypedToStringHelper {
    override def fromTyped[T](t: T)(implicit ct: universe.WeakTypeTag[T]): String =
      t.asInstanceOf[InWrapper].fromTyped
  }
  implicit val myStringToTypedHelper = new StringToTypedHelper {
    override def toType[T](s: String)(implicit ct: universe.WeakTypeTag[T]): T =
      OutWrapper(s).asInstanceOf[T]
  }
}
