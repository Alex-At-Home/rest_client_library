package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.RestRequestException
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped.{RuntimeStringToTypedHelper, RuntimeTypedToStringHelper}

import scala.reflect.runtime._
import scala.reflect.runtime.universe._
import scala.util.Try

/**
  * Supports typed object for custom classes only, ie if no JSON library is used
  * for some reason
  */
object NoJsonRuntimeHelpers {

  /**
    * Include this to support a typed API made entirely out of custom classes
    */
  implicit val NoJsonTypedToStringHelper = new RuntimeTypedToStringHelper() {
    def fromTyped[T](t: T)(implicit ct: WeakTypeTag[T]): String = t match {
      case custom: CustomTypedToString => custom.fromTyped
      case _ => throw RestRequestException(s"Type ${t.getClass} not supported with JSON lib")
    }
  }

  /**
    * Include this to support a typed API made entirely out of custom classes
    */
  implicit val NoJsontringToTypedHelper = new RuntimeStringToTypedHelper() {
    override def toType[T](s: String)(implicit ct: WeakTypeTag[T]): T = NoJsonRuntimeHelpers.createCustomTyped(s)
  }

  /** Handles getting at a class wtihin an object...
    * ...where the class is embedded in that object via a trait
    *
    * @param ct The type tag of the end class (ie inside an object)
    * @tparam T The type of the object being retrieved
    * @return A module mirror containing the end class
    */
  def getOuterInstanceMirror[T](ct: universe.WeakTypeTag[T]): scala.reflect.runtime.universe.InstanceMirror = {
    // From: http://stackoverflow.com/questions/18056107/reflection-getting-module-mirror-from-inner-class-mixed-into-a-singleton-object
    // (doesn't give you everything though because can't trivially get access to the module instance, see next SO post!)
    val TypeRef(pre, _, _) = ct.tpe

    // From: http://stackoverflow.com/questions/17012294/recovering-a-singleton-instance-via-reflection-from-sealed-super-trait-when-typ
    // Getting closer
    val classSymbol = pre.typeSymbol.asClass
    val compSymbol = classSymbol.companionSymbol // (note using companion here fails)
    val moduleSymbol = compSymbol.asModule
    val moduleMirror = currentMirror.reflectModule(moduleSymbol)

    // Now we can get an instance of the outer type
    currentMirror.reflect(moduleMirror.instance)
  }

  /** Given a class with a single constructor taking a string,
    * creates an instance of the class
    *
    * TODO: needs to handle trait version, see `CirceTypeModule` example
    *
    * @param s The input to the ctor
    * @param ct The weak type tag of the custom typed output object
    * @tparam T The type of the custom typed output object
    * @return An instnce of the type
    */
  def createCustomTyped[T](s: String)(implicit ct: universe.WeakTypeTag[T]): T = {
    val ctor =
      ct.tpe.members
        .filter(m => m.isMethod && m.asMethod.isPrimaryConstructor)
        .map(_.asMethod)
        .head

    val ctorMirror =
      Try { currentMirror.reflectClass(ct.tpe.typeSymbol.asClass) }
        .getOrElse {
          val moduleMirror = getOuterInstanceMirror(ct)
          val instanceMirror = currentMirror.reflect(moduleMirror.instance)
          instanceMirror.reflectClass(ct.tpe.typeSymbol.asClass)
        }
        .reflectConstructor(ctor)(s)

    ctorMirror.asInstanceOf[T]
  }

}
