package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, TypedOperation}
import org.elastic.rest.scala.driver.RestBaseImplicits.JsonToStringHelper
import org.elastic.rest.scala.driver.RestBaseRuntimeTyped.RuntimeTypedToStringHelper
import org.elastic.rest.scala.driver.RestBaseImplicits._

import scala.annotation.StaticAnnotation
import scala.reflect.macros._

/**
  * Contains scala macros
  */
object MacroUtils {

  /**
    * Injects the name of the method into the parameter
    * @param c Whitebox macro context
    * @param annottees The code to annotate
    * @return
    */
  def modifierImpl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val newMethod = annottees map (_.tree) toList match {
      case (methodDef: DefDef) :: Nil =>
        methodDef match {
          case q"$modifiers def $methodName($fieldName: $fieldVal): $retType = $body" =>
            val methodNameStr = s"${methodName.toString}"
            q"""def $methodName($fieldName: $fieldVal): this.type = {
               this.asInstanceOf[BaseDriverOp].withModifier(($methodNameStr, $fieldName)).asInstanceOf[this.type]
            }"""
          case x @ _ => c.abort(c.enclosingPosition,
            s"Invalid annottee - needs to be method in format def <name>(args: Any): this.type = Modifier.body vs $x"
          )
        }

      case x @ _ => c.abort(c.enclosingPosition,
        s"Invalid annottee - needs to be method in format def <name>(args: Any): this.type = Modifier.body vs $x"
      )
    }
    c.Expr[Any]{ newMethod }
  }

  /** OpType macro
    * (from: http://stackoverflow.com/questions/25127140/how-can-parameters-settings-be-passed-to-a-scala-macro/25219644#25219644)
    *
    * @param op The operation (has to be specified as a string literal)
    */
  class OpType(op: String) extends StaticAnnotation

  /** Get the operation type from the @OpType macro
    * (from: http://stackoverflow.com/questions/25127140/how-can-parameters-settings-be-passed-to-a-scala-macro/25219644#25219644)
    *
    * @param c The context of the macro operation
    * @return The operation type
    */
  def getOpType(c: blackbox.Context): String = {
    import c.universe._

    c.macroApplication.symbol.annotations.find(
      _.tree.tpe <:< typeOf[OpType]
    ).flatMap(
      _.tree.children.tail.collectFirst {
        case Literal(Constant(s: String)) => s
        case x @ _ => c.abort(
          c.enclosingPosition, s"OpType method needs to be string literal, not $x")
      }
    ).getOrElse(
      c.abort(c.enclosingPosition,
        "Failed to get method, did you specify the @OpType(method) annotation?" +
          s" annotations = ${c.macroApplication.symbol.annotations}")
    )
  }

  /** The actual code in quasi-codes for building the header/param modifiable case class
    * (typed return)
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
        override def withModifier(kv: (String, Any)): this.type = Internal(resource, op, body, kv :: mods, headers)
          .asInstanceOf[this.type]
        override def withHeader(h: String): this.type = Internal(resource, op, body, mods, h :: headers)
          .asInstanceOf[this.type]
      }
      Internal($self, $opType, $body, $modifiers, $headers)
    """
  }

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
    * @tparam T The trait containing the list of available modifiers
    * @return The macro code to inject
    */
  private def buildInternalClass[T]
    (c: blackbox.Context)
    (self: c.Expr[c.PrefixType], opType: String, body: c.Expr[Option[String]],
     modifiers: List[String], headers: List[String],
     ctt: c.WeakTypeTag[T]) =
  {
    import c.universe._
    q"""
      case class Internal
      (resource: RestResource, op: String, body: Option[String], mods: List[(String, Any)], headers: List[String])
        extends $ctt
      {
        override def withModifier(kv: (String, Any)): this.type = Internal(resource, op, body, kv :: mods, headers)
          .asInstanceOf[this.type]
        override def withHeader(h: String): this.type = Internal(resource, op, body, mods, h :: headers)
          .asInstanceOf[this.type]
      }
      Internal($self, $opType, $body, $modifiers, $headers)
    """
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param ct The type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl[T <: BaseDriverOp]
    (c: blackbox.Context)()
    (implicit ct: c.WeakTypeTag[T])
    : c.Expr[T] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix

    c.Expr[T] {
      buildInternalClass[T](c)(self, opType, reify { None }, List(), List(), ct).asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param ctt The operation type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param cto The return type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam O The output type (combination of `Modifier` classes and `BaseDriverOp`)
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_TypedOutput[T <: BaseDriverOp, O]
  (c: blackbox.Context)()
  (implicit ctt: c.WeakTypeTag[T], cto: c.WeakTypeTag[O])
  : c.Expr[T with TypedOperation[O]] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix

    c.Expr[T with TypedOperation[O]] {
      buildInternalClass[T, O](c)(self, opType, reify { None }, List(), List(), ctt, cto)
        .asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ct The type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_JBody[T <: BaseDriverOp, J]
    (c: blackbox.Context)(body: c.Expr[J])
    (jsonToStringHelper: c.Expr[JsonToStringHelper[J]])
    (implicit ct: c.WeakTypeTag[T])
    : c.Expr[T] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix
    val maybeBody = reify { Option(jsonToStringHelper.splice.fromJson(body.splice)) }

    c.Expr[T] {
      buildInternalClass[T](c)(self, opType, maybeBody, List(), List(), ct)
        .asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param ctt The operation type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param cto The return type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam O The output type (combination of `Modifier` classes and `BaseDriverOp`)
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_JBody_TypedOutput[T <: BaseDriverOp, J, O]
  (c: blackbox.Context)(body: c.Expr[J])
  (jsonToStringHelper: c.Expr[JsonToStringHelper[J]])
  (implicit ctt: c.WeakTypeTag[T], cto: c.WeakTypeTag[O])
  : c.Expr[T with TypedOperation[O]] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix
    val maybeBody = reify { Option(jsonToStringHelper.splice.fromJson(body.splice)) }

    c.Expr[T with TypedOperation[O]] {
      buildInternalClass[T, O](c)(self, opType, maybeBody, List(), List(), ctt, cto)
        .asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ctt The operation type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param ctc The body type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam C The input type
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_CBody[T <: BaseDriverOp, C]
  (c: blackbox.Context)(body: c.Expr[C])
  (typeToStringHelper: c.Expr[RuntimeTypedToStringHelper])
  (implicit ctt: c.WeakTypeTag[T], ctc: c.WeakTypeTag[C])
  : c.Expr[T] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix
    val resource = c.Expr[c.PrefixType] { q"$self.resource" }
    val maybeBody =
      if (typeToStringHelper != null)
        reify { Option(typeToStringHelper.splice.fromTyped[C](body.splice)) }
      else
        c.Expr[Option[String]] { q"Some($body.fromTyped)" }

    c.Expr[T] {
      buildInternalClass[T](c)(resource, opType, maybeBody, List(), List(), ctt)
        .asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    * (Custom input type)
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ctt The operation type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param ctc The body type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam C The input type
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_CBodyCustom[T <: BaseDriverOp, C <: CustomTypedToString]
    (c: blackbox.Context)(body: c.Expr[C])
    (implicit ctt: c.WeakTypeTag[T], ctc: c.WeakTypeTag[C])
    : c.Expr[T] =
  {
    materializeOpImpl_CBody[T, C](c)(body)(null)(ctt, ctc).asInstanceOf[c.Expr[T]]
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ctt The operation type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param ctc The body type evidence
    * @param cto The output type evidence
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam C The input type
    * @tparam O The output type
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_CBody_TypedOutput[T <: BaseDriverOp, C, O]
    (c: blackbox.Context)(body: c.Expr[C])
    (typeToStringHelper: c.Expr[RuntimeTypedToStringHelper])
    (implicit ctt: c.WeakTypeTag[T], ctc: c.WeakTypeTag[C], cto: c.WeakTypeTag[O])
    : c.Expr[T] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix
    val resource = c.Expr[c.PrefixType] { q"$self.resource" }
    val maybeBody =
      if (typeToStringHelper != null)
        reify { Option(typeToStringHelper.splice.fromTyped[C](body.splice)) }
      else
        c.Expr[Option[String]] { q"Some($body.fromTyped)" }

    c.Expr[T with TypedOperation[O]] {
      buildInternalClass[T, O](c)(resource, opType, maybeBody, List(), List(), ctt, cto)
        .asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    * (Custom input type)
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ctt The operation type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param ctc The body type evidence
    * @param cto The output type evidence
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam C The input type
    * @tparam O The output type
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_CBodyCustom_TypedOutput[T <: BaseDriverOp, C <: CustomTypedToString, O]
    (c: blackbox.Context)(body: c.Expr[C])
    (implicit ctt: c.WeakTypeTag[T], ctc: c.WeakTypeTag[C], cto: c.WeakTypeTag[O])
    : c.Expr[T] =
  {
    materializeOpImpl_CBody_TypedOutput[T, C, O](c)(body)(null)(ctt, ctc, cto).asInstanceOf[c.Expr[T]]
  }
  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ctt The type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_Body[T <: BaseDriverOp]
    (c: blackbox.Context)(body: c.Expr[String])
    (implicit ctt: c.WeakTypeTag[T])
    : c.Expr[T] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix
    val maybeBody = reify { Option(body.splice) }

    c.Expr[T] {
      buildInternalClass[T](c)(self, opType, maybeBody, List(), List(), ctt)
        .asInstanceOf[c.Tree]
    }
  }

  /**
    * The Macro implementation, allows for modifiers to be chained
    * Without this, needed two extra case classes for each combination of modifiers
    * (one extra class - the first case class can be replaced with a much simpler list
    *
    * @param c The macro context
    * @param body The body to write to the resource (or None for pure reads)
    * @param ctt The type evidence (combination of `Modifier` classes and `BaseDriverOp`)
    * @param cto The output type evidence
    * @tparam T The type (combination of `Modifier` classes and `BaseDriverOp`)
    * @tparam O The output type
    * @return A chainable version of the `BaseDriverOp` mixed with T
    */
  def materializeOpImpl_Body_TypedOutput[T <: BaseDriverOp, O]
  (c: blackbox.Context)(body: c.Expr[String])
  (implicit ctt: c.WeakTypeTag[T], cto: c.WeakTypeTag[O])
  : c.Expr[T] =
  {
    import c.universe._

    val opType = getOpType(c)
    val self = c.prefix
    val maybeBody = reify { Option(body.splice) }

    c.Expr[T] {
      buildInternalClass[T, O](c)(self, opType, maybeBody, List(), List(), ctt, cto)
        .asInstanceOf[c.Tree]
    }
  }
}
