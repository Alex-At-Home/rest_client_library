package org.elastic.rest.scala.driver.utils

import scala.reflect.macros.blackbox

/** JVM specific macro utils to handle the different (to JS) typed handling
  */
object MacroUtilsTyped {
  
  /** The actual code in quasi-codes for building the header/param modifiable case class
    * (untyped return)
    *
    * @param c The context of the macro operation
    * @param self The `RestResource`
    * @param opType The operation type
    * @param body The body to post
    * @param modifiers The parameters/modifiers
    * @param headers The headers
    * @param ctt The evidence for the trait containing the list of available modifiers
    * @param cto The evidence for the output
    * @tparam T The trait containing the list of available modifiers
    * @tparam O The output type
    * @return The macro code to inject
    */
  def buildInternalClass[T, O]
    (c: blackbox.Context)
    (self: c.Expr[c.PrefixType], opType: String, body: c.Expr[Option[String]],
     modifiers: List[String], headers: List[String],
     ctt: c.WeakTypeTag[T], cto: c.WeakTypeTag[O]) =
  {
    import c.universe._

    q"""
      case class Internal
      (resource: RestResource, op: String, body: Option[String], mods: List[(String, Any)], headers: List[String])
        extends $ctt with TypedOperation[$cto]
      {
        override val ct: scala.reflect.runtime.universe.WeakTypeTag[$cto] =
          scala.reflect.runtime.universe.weakTypeTag[$cto]

        override def withModifier(kv: (String, Any)): this.type = Internal(resource, op, body, kv :: mods, headers)
          .asInstanceOf[this.type]
        override def withHeader(h: String): this.type = Internal(resource, op, body, mods, h :: headers)
          .asInstanceOf[this.type]
      }
      Internal($self, $opType, $body, $modifiers, $headers)
    """
  }
}
