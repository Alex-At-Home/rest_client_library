package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, Modifier, RestResource, TypedDriverOp}
import org.elastic.rest.scala.driver.RestBaseImplicits._
import org.elastic.rest.scala.driver.RestResources._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

/** Helper implicits for custom types
  */
object NoJsonHelpers {

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataReadableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestWithDataReadableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataReadableTU[D, I]
  {
    @MacroUtils.OpType("GET")
    override def read(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperSendableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestSendableTU[D, I] with RestResource)
    extends TypedToStringHelperSendableTU[D, I]
  {
    @MacroUtils.OpType("POST")
    override def send(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWritableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestWritableTU[D, I] with RestResource)
    extends TypedToStringHelperWritableTU[D, I]
  {
    @MacroUtils.OpType("PUT")
    override def write(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataDeletableTU[D <: Modifier, I <: CustomTypedToString]
    (val resource: RestWithDataDeletableTU[D, I] with RestResource)
    extends TypedToStringHelperWithDataDeletableTU[D, I]
  {
    @MacroUtils.OpType("DELETE")
    override def delete(body: I): D with BaseDriverOp = macro MacroUtils.materializeOpImpl_CBodyCustom[D, I]
  }

  // Typed output:

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataReadableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestWithDataReadableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataReadableTT[D, I, O]
  {
    @MacroUtils.OpType("GET")
    override def read(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperSendableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestSendableTT[D, I, O] with RestResource)
    extends TypedToStringHelperSendableTT[D, I, O]
  {
    @MacroUtils.OpType("POST")
    override def send(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWritableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestWritableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWritableTT[D, I, O]
  {
    @MacroUtils.OpType("PUT")
    override def write(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  /** Typed input case */
  implicit class NoJsonTypedStringToTypeHelperWithDataDeletableTT[D <: Modifier, I <: CustomTypedToString, O]
    (val resource: RestWithDataDeletableTT[D, I, O] with RestResource)
    extends TypedToStringHelperWithDataDeletableTT[D, I, O]
  {
    @MacroUtils.OpType("DELETE")
    override def delete(body: I): D with TypedDriverOp[O] =
      macro MacroUtils.materializeOpImpl_CBodyCustom_TypedOutput[D, I, O]
  }

  ///////////////////////////////////////////////////////////////////////

  /** Annotate the fromTyped: String in XXX with these to create JSON objects defined
    * using the DSL defined in the `SimpleObjectDescription` companion object (eg
    * it is recommended to add `import SimpleObjectDescription._` to the package/object/etc in
    * which they are declared)
    * @param packageAlias The alias used to prefix `SimpleObject`, `Field` etc in the client-side code
    *                     eg if you have `import {SimpleObjectDescription => obj}`, this would be `"obj"`
    * @param els The elements to insert into the root object
    */
  class SimpleObjectDescription(packageAlias: String, els: SimpleObjectDescription.Element*) extends StaticAnnotation {
    def macroTransform(annottees: Any*) = macro MacroUtils.simpleObjectDescriptionImpl
  }
  /** Companion object containing the DSL elements to build simple CustomTypedToString helpers */
  object SimpleObjectDescription {
    //TODO: add scaladoc for all the DSL objects
    //TODO: note fine that the syntax allows for bad objects because we can test at "runtime" which is a complication phase!
    //TODO: handle Seq/String/Integer/Long/Boolean/Option[....]/CustomStringToTyped
    //TODO: handle Maps?
    //TODO: constant values
    //TODO: prefixes everywhere...?

    /** A placeholder for `fromTyped`, which gets overwritten by this macro */
    val AutoGenerated: String = null.asInstanceOf[String]

    /** Sealed trait defining the simple DSL for describing*/
    sealed trait Element

    /** Build an object in the `SimpleObjectDescription` DSL
      * @param els The contents of the object
      */
    case class SimpleObject(els: Element*) extends Element
    /** Constructor for an object in the `SimpleObjectDescription` DSL */
    object SimpleObject {
      class SimpleObjectInner(key: String) {
        /** Adds elements to the object
          * @param els The contents of the object
          * @return An object representation
          */
        def apply(els: Element*) = Constant(key, SimpleObject(els:_*))
      }
      /** Helper to build an object injected at `key` in the `SimpleObjectDescription` DSL
        * see constructor for extra params (`els`)
        * @param key The (constant) key at which the object is inserted
        * @return Temp function returning an object representation
        */
      def apply(key: String): SimpleObjectInner = new SimpleObjectInner(key)
    }

    /** Inserts a key-value pair into an object, where the key and value are both taken from the
      * constructor params of the case class
      * @param keyParam The name of the ctor param that will represent the key
      * @param valueParam The name of the ctor param that will represent the value
      * @param prefix A prefix to be inserted before the key
      * @param extras A list of additional elements ... if these are present then the `valueParam`
      *               is injected into an object under the (constant) name of the `valueParam` string
      *               and the extras are injected along side (ie at the same level)
      */
    case class KeyValue(keyParam: String, valueParam: Element, prefix: String, extras: Element*) extends Element
    /** Constructor for a key/value pair in the `SimpleObjectDescription` DSL */
    object KeyValue {
      class KeyValueInner(keyParam: String, prefix: String) {
        /**
          * @param valueParam The name of the ctor param that will represent the value
          * @param extras A list of additional elements ... if these are present then the `valueParam`
          *               is injected into an object under the (constant) name of the `valueParam` string
          *               and the extras are injected along side (ie at the same level)
          * @return A key-value pair representation
          */
        def apply(valueParam: Element, extras: Element*) = KeyValue(keyParam, valueParam, prefix, extras: _*)

      }
      /** Inserts a key-value pair into an object, where the key and value are both taken from the
        * constructor params of the case class - see constructor for extra params (`valueParam`, `extras`)
        * @param keyParam The name of the ctor param that will represent the key
        * @param prefix A prefix to be inserted before the key
        * @return Temp function returning a key-value pair representation
        */
      def apply(keyParam: String, prefix: String = ""): KeyValueInner = new KeyValueInner(keyParam, prefix)
    }

    /** This inserts a key/val pair for a Seq, where if there are 0 values then the key/val
      * pair isn't inserted, if there is 1 value, it is inserted as value, and if there are >1 then it is inserted
      * as an array
      * @param fieldParam The case class parameter containing the field value
      * @param prefix A prefix to be inserted before the field
      */
    case class MultiTypeField(fieldParam: String, prefix: String = "") extends Element

    /** This inserts a key/val pair into an object, where the key is given by the `fieldParam` string,
      * and the value is given by the case class parameter of that name
      * If the `fieldParam` is an `Option` then it is only inserted if present (etc)
      *
      * @param fieldParam The name of the ctor param that will represent the field (and contain its value)
      * @param prefix A prefix to be inserted before the key
      */
    case class Field(fieldParam: String, prefix: String = "") extends Element

    /** Inserts a constant key/value pair with the designated values into an object
      * @param field The key name
      * @param value The value (should be a `CustomTypedToString`, `Seq`, `String`, `Long`, `Double`, `Integer` or
      *              `Boolean`, or finally `Element`)
      */
    case class Constant(field: String, value: Any) extends Element

    //////////////////////////////////////////

    def el2Str(el: Element): String = el match {

        //TODO: need to pass key into any2Str since if optional the whole thing vanishes...
        //TODO: handle varargs in pattern match...
      case SimpleObject(els @ _*) =>
        s""" { ${els.map(el2Str(_)).mkString(",")} } """
      case KeyValue(keyParam, valueParam, prefix, extras @ _*) =>
        val actualKey = ""//TODO how to interpret keyParam? (need to figure out escaping in string interp)
        val bodyLogic = extras.toList match {
          case Nil => s"${el2Str(valueParam)}"
          case _ => //TODO: need to restrict allowed types of el (here and elsewhere?)
            // eg here extras need to be a key value pair
            s"""
               {
                  "$valueParam": ${el2Str(valueParam)},
                  ${extras.map(el2Str(_)).mkString(",")}
               }
             """.stripMargin
        }
        s"""  "$actualKey": $bodyLogic """
      case MultiTypeField(fieldParam, prefix) =>
        ""//TODO
      case Field(fieldParam, prefix) =>
        s""" ${'$'}{any2Str("$fieldParam", $fieldParam)} """
      case Constant(field, value) =>
        s""" ${any2Str(field, value)} """
    }

    /**
      * TODO
      * @param key
      * @param any
      * @return
      */
    def any2Str(key: String, any: Any): String = any match {
      case s: String => s""" "$key": "$s"  """
      case b: Boolean => s""" "$key": $b  """
      //TODO Seq/Option/Base/Custom also handle Element
      case el: Element => s""" "$key": ${el2Str(el)}  """
      case _ => throw new Exception(s"Invalid value: $any")
    }

  }

}
