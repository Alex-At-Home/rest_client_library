package org.elastic.rest.scala.driver.utils

import org.elastic.rest.scala.driver.RestBase.{BaseDriverOp, Modifier, RestResource, TypedDriverOp}
import org.elastic.rest.scala.driver.RestBaseImplicits.{CustomTypedToString, _}
import org.elastic.rest.scala.driver.RestResources._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

/** Helper implicits for custom types
  */
object NoJsonHelpers {

  /** Enables use of value classes for (eg) string constants */
  trait ToStringAnyVal[T] extends Any {
    /** The underlying value */
    def value: T
    override def toString: String = value.toString
  }

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

  /** Annotate the fromTyped: String in `CustomTypedToString` with these to create JSON objects defined
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

    /** A placeholder for `fromTyped`, which gets overwritten by this macro */
    val AutoGenerated: String = null.asInstanceOf[String]

    /** Sealed trait defining the simple DSL for describing */
    sealed trait Element

    /** Build an object in the `SimpleObjectDescription` DSL, ie mapping to `{ $els }`
      *
      * @param els The contents of the object
      */
    case class SimpleObject(els: Element*) extends Element

    /** Constructor for an object in the `SimpleObjectDescription` DSL */
    object SimpleObject {

      class SimpleObjectInner(key: String) {
        /** Adds elements to the object
          *
          * @param els The contents of the object
          * @return An object representation
          */
        def apply(els: Element*) = Constant(key, SimpleObject(els: _*))
      }

      /** Helper to build an object injected at `key` in the `SimpleObjectDescription` DSL
        * see constructor for extra params (`els`)
        *
        * @param key The (constant) key at which the object is inserted
        * @return Temp function returning an object representation
        */
      def apply(key: String): SimpleObjectInner = new SimpleObjectInner(key)
    }

    /** Inserts a map of key-value pairs into an object, where the key and value are both taken from the
      * constructor params of the case class
      * Maps to `k1: v1, k2: v2, ...` ie MUST BE WRAPPED IN `SimpleObject`
      *
      * @param keyValuesParam  The name of the ctor param that will represent the map of key/value
      * @param prefix     A prefix to be inserted before the key
      * @param valueParamName If `extras` are specified then the value is mapped into an object together with the extras
      *                       - this parameter specifies the name that is used for the key at which the value
      *                       is injected
      * @param extras     A list of additional elements ... if these are present then the `valueParam`
      *                   is injected into an object under the (constant) name of the `valueParam` string
      *                   and the extras are injected along side (ie at the same level)
      */
    case class KeyValues(keyValuesParam: String, prefix: String, valueParamName: String, extras: Element*) extends Element

    /** Constructor for a key/value pair in the `SimpleObjectDescription` DSL */
    object KeyValues {

      class KeyValuesInner(keyValuesParam: String, prefix: String) {

        /** Inserts a map of key-value pairs into an object, where the key and value are both taken from the
          * constructor params of the case class
          *
          * @return A key-value pair representation
          */
        def apply() = KeyValues(keyValuesParam, prefix, "")

        /** Inserts a map of key-value pairs into an object, where the key and value are both taken from the
          * constructor params of the case class
          *
          * @param valueParamName If `extras` are specified then the value is mapped into an object together with the extras
          *                       - this parameter specifies the name that is used for the key at which the value
          *                       is injected
          * @param extras     A list of additional elements ... if these are present then the `valueParam`
          *                   is injected into an object under the (constant) name of the `valueParam` string
          *                   and the extras are injected along side (ie at the same level)
          * @return A key-value pair representation
          */
        def apply(valueParamName: String, extras: Element*) = KeyValues(keyValuesParam, prefix, valueParamName, extras: _*)

      }

      /** Inserts a key-value pair into an object, where the key and value are both taken from the
        * constructor params of the case class - see constructor for extra params (`valueParam`, `extras`)
        *
        * @param keyParam The name of the ctor param that will represent the key
        * @param prefix   A prefix to be inserted before the key
        * @return Temp function returning a key-value pair representation
        */
      def apply(keyParam: String, prefix: String = ""): KeyValuesInner = new KeyValuesInner(keyParam, prefix)
    }

    /** Inserts a key-value pair into an object, where the key and value are both taken from the
      * constructor params of the case class
      * Maps to `k1: v1` or `k1: { v: v1, $extras }` ie MUST BE WRAPPED IN `SimpleObject`
      *
      * @param keyParam   The name of the ctor param that will represent the key
      * @param valueParam The name of the ctor param that will represent the value
      * @param prefix     A prefix to be inserted before the key
      * @param valueParamName If `extras` are specified then the value is mapped into an object together with the extras
      *                       - this parameter specifies the name that is used for the key at which the value
      *                       is injected
      * @param extras     A list of additional elements ... if these are present then the `valueParam`
      *                   is injected into an object under the (constant) name of the `valueParam` string
      *                   and the extras are injected along side (ie at the same level)
      */
    case class KeyValue
    (keyParam: String, valueParam: Element, prefix: String, valueParamName: String, extras: Element*) extends Element

    /** Constructor for a key/value pair in the `SimpleObjectDescription` DSL */
    object KeyValue {

      class KeyValueInner(keyParam: String, prefix: String) {
        /**
          * @param valueParam The object DSL (object or raw value) that is injected under key `keyParam`
          * @return A key-value pair representation
          */
        def apply(valueParam: Element) = KeyValue(keyParam, valueParam, prefix, "")

        /**
          * @param valueParam The object DSL (object or raw value) that is injected under key `keyParam`
          * @param valueParamName If `extras` are specified then the value is mapped into an object together with the extras
          *                       - this parameter specifies the name that is used for the key at which the value
          *                       is injected
          * @param extras     A list of additional elements ... if these are present then the `valueParam`
          *                   is injected into an object under the (constant) name of the `valueParam` string
          *                   and the extras are injected along side (ie at the same level)
          * @return A key-value pair representation
          */
        def apply(valueParam: Element, valueParamName: String, extras: Element*) = KeyValue(
          keyParam, valueParam, prefix, valueParamName, extras: _*
        )
        /**
          * @param valueParam The name of the ctor param that will represent the value (has to be a simple field)
          * @param extras     A list of additional elements ... if these are present then the `valueParam`
          *                   is injected into an object under the (constant) name of the `valueParam` string
          *                   and the extras are injected along side (ie at the same level)
          * @return A key-value pair representation
          */
        def apply(valueParam: String, extras: Element*) = KeyValue(
          keyParam, FieldValue(valueParam), prefix, valueParam, extras: _*
        )
      }

      /** Inserts a key-value pair into an object, where the key and value are both taken from the
        * constructor params of the case class - see constructor for extra params (`valueParam`, `extras`)
        *
        * @param keyParam The name of the ctor param that will represent the key
        * @param prefix   A prefix to be inserted before the key
        * @return Temp function returning a key-value pair representation
        */
      def apply(keyParam: String, prefix: String = ""): KeyValueInner = new KeyValueInner(keyParam, prefix)
    }

    /** This inserts a key/val pair for a Seq, where if there are 0 values then the key/val
      * pair isn't inserted, (if there is 1 value, it is inserted as either value or single-value-array (depending on
      * `arrayIfSingleton`), and if there are >1 then it is inserted as an array
      * Also works for Map, where it simply does not display the field if the map is empty
      * Maps to `k: v` or `k: [ v1, v2, ... ]` ie MUST BE WRAPPED IN `SimpleObject`
      *
      * @param fieldParam The case class parameter containing the field value
      * @param arrayIfSingleton If false (defaults to true)
      * @param prefix     A prefix to be inserted before the field
      */
    case class MultiTypeField(fieldParam: String, arrayIfSingleton: Boolean = true, prefix: String = "") extends Element

    /** This inserts a key/val pair into an object, where the key is given by the `fieldParam` string,
      * and the value is given by the case class parameter of that name
      * If the `fieldParam` is an `Option` then it is only inserted if present (etc)
      * Maps to `k: v` ie MUST BE WRAPPED IN `SimpleObject`
      *
      * @param fieldParam The name of the ctor param that will represent the field (and contain its value)
      * @param prefix     A prefix to be inserted before the key
      */
    case class Field(fieldParam: String, prefix: String = "") extends Element

    /** Inserts a constant key/value pair with the designated values into an object
      * Maps to `k: v` ie MUST BE WRAPPED IN `SimpleObject`
      *
      * @param field The key name
      * @param value The value (should be a `CustomTypedToString`, `Seq`, `String`, `Long`, `Double`, `Integer` or
      *              `Boolean`, or finally `Element`)
      */
    case class Constant(field: String, value: Any) extends Element

    /** Inserts a raw value based on the `field` variable (with no key - ie can break JSON format
      * and can only be used as the `element` param for a `KeyValue` or `KeyValues` element)
      * @param field The variable whose value(s) to insert (with no key - ie can break JSON format)
      */
    case class FieldValue(field: String) extends Element

    /** Inserts a raw constant value (with no key - ie can break JSON format
      * and can only be used as the `element` param for a `KeyValue` or `KeyValues` element)
      * @param value The value to insert (with no key - ie can break JSON format)
      */
    case class ConstantValue(value: Any) extends Element

    //////////////////////////////////////////

    //TODO: need to pass in an "allowed values" enum set that generates compile-time errors if invalid JSON is generated
    // (eg `ConstantValue` and `FieldValue` are only allowed as element params to `KeyValue`))
    // Until then there is "compile-time-valid" declarations that will result in runtime errors, which is not ideal

    /** Converts an element in the simple object description DSL into a string
      *  (Called as part of macro only)
      * @param el The DSL element to convert
      * @param isFirst Is the first element in a seq, ie shouldn't prepend a comma regardless
      * @return A string that can be compiled into runtime code
      */
    def el2Str(el: Element, isFirst: Boolean): String = el match {

      case SimpleObject(els @ _*) =>
        // 2 important notes here:
        //1] (no leading "," here ever - the only case where that can happen is if it's in an array, which is handled
        //    by the array itself) - hence why there's no `addComma` here
        //2] Also ... there's a big problem with handling the first element(s) being missing `Option`s because
        //   (unlike `KeyValues` below) the logic can be nested inside the runtime code (`el2Str` vs `any2Str`)
        //   That's the reason for the nasty extra `${'$'}` and the `replaceFirst` construct
        val embeddedStr = els2Str(els.toList, startsObject = true).mkString(" ")
        s"""${'$'}{ s\"\"\" {  $embeddedStr } \"\"\".replaceFirst("^[ ]*[{][ ]*,", " { ") }"""

      case KeyValues(keyValuesParam, prefix, valueParamName, extras @ _*) =>
        //TODO: handle extras here, until then error at compile time (see note below)

        // Note that adding `extras` is a significant refactoring effort due to el2Str not being useable at
        // runtime (only macro time), so the foldLeft (at runtime) construct below doesn't work at all.).
        // So likely to get left this way for the foreseeable future

        if (extras.nonEmpty)
          throw new Exception(s"Cannot currently specify 'extras' in KeyValues declaration, please contact developer")

        //(note that the leading "," is inserted by the any2Str call)
        s"""${'$'}{$keyValuesParam.foldLeft("") { (acc, kv) =>
             acc + any2Str(kv._1, "$prefix", kv._2, $isFirst && acc.trim().isEmpty)
          }
        }"""

      case KeyValue(keyParam, valueParam, prefix, valueParamName, extras @ _*) =>
        val actualKeyStr = s"""${'$'}{"$prefix" + $keyParam}"""
        val mainValStr = el2Str(valueParam, isFirst = true)
        val bodyLogicStr = extras match {
          case Seq() => s"$mainValStr"
          case _ =>
            // Unlike the use of `els2Str` under `SimpleObject`, this case is fine because we always want
            // a leading `,` because of the `k: v` clause that is guaranteed to provide it
            val extraStr = els2Str(extras.toList, startsObject = false).mkString(" ")
            s"""
             ${'$'}{
                val extras = s\"\"\"$extraStr\"\"\"
                if (extras.trim().isEmpty()) {
                  s\"\"\"  $mainValStr \"\"\"
                }
                else {s\"\"\"{
                    "$valueParamName": $mainValStr
                    $extraStr
                  }\"\"\"
                }
             }"""
        }
        s""" ${addComma(isFirst)} "$actualKeyStr": $bodyLogicStr """

      case MultiTypeField(fieldParam, arrayIfSingleton, prefix) =>
        s"""${'$'}{$fieldParam match {
          case m: Map[_, _] if m.isEmpty => ""
          case Seq() => ""
          case Seq(el) if !$arrayIfSingleton => any2Str("$fieldParam", "$prefix", el, $isFirst)
          case _ => any2Str("$fieldParam", "$prefix", $fieldParam, $isFirst)
        }}"""

      case Field(fieldParam, prefix) =>
        s""" ${'$'}{any2Str("$fieldParam", "$prefix", $fieldParam, $isFirst)} """

      case Constant(field, value) =>
        s""" ${any2Str(field, "", value, isFirst)} """

      case FieldValue(field) =>
        s""" ${'$'}{any2Str($field)} """

      case ConstantValue(value) =>
        s""" ${any2Str(value)} """
    }

    /** Converts any supported type into a string
      * (Can be called either as part of macro or as part of runtime)
      * @param key The key at which to inject the value
      * @param any The value to be injected
      * @param isFirst Is the first element in a seq, ie shouldn't prepend a comma regardless
      * @return An embeddable string representing the key value pair
      */
    def any2Str(key: String, prefix: String, any: Any, isFirst: Boolean): String = any match {
      case (_:String | _:Boolean | _:Integer | _:Long | _:Float | _:Double | _:Element | _:ToStringAnyVal[_] | _:CustomTypedToString) =>
        s"""  ${addComma(isFirst)} "$prefix${removeBackTicks(key)}": ${any2Str(any)} """

      case e: Either[_, _] => any2Str(key, prefix, e.fold[Any](l => l, r => r), isFirst)
      case o: Option[_] => o.map(c => any2Str(key, prefix, c, isFirst)).getOrElse("")
      case map: Map[_, _] =>
        s""" ${addComma(isFirst)} "$prefix${removeBackTicks(key)}": ${any2Str(map)} """
      case seq: Seq[_] =>
        s""" ${addComma(isFirst)} "$prefix${removeBackTicks(key)}": ${any2Str(seq)} """

      case _ => throw new Exception(s"Invalid prefix/key/value: $prefix / $key / $any")
    }

    /** Converts any supported type into a string
      * (Can be called either as part of macro or as part of runtime - note that the
      * `case el: Element` in theory can break this but in practice can only be called within the macro,
      * at least except in pathological cases)
      *
      * @param any The value to be injected
      * @return An embeddable string representing a value
      */
    def any2Str(any: Any): String = any match {
      case s: String => s""" "$s" """
      case b: Boolean => s""" $b """
      case i: Integer => s""" $i """
      case l: Long => s""" $l """
      case f: Float => s""" $f """
      case d: Double => s""" $d """
      case el: Element => el2Str(el, isFirst = true) //(adding command handled by calling version of any2Str)
      case t: ToStringAnyVal[_] => any2Str(t.value)
      case custom: CustomTypedToString => custom.fromTyped
      case seq: Seq[_] => s""" [ ${seq.map(c => any2Str(c)).mkString(",")} ] """
      case map: Map[_, _] => s""" { ${map.map { case (k, v) => s""" "$k": ${any2Str(v)} """}.mkString(",")} } """
      case _ => throw new Exception(s"Invalid value: $any")
    }

    /** Converts a sequence of elements into a sequence of strings with commas correctly inserted
      * (handling potentially empty elements - this is only known at runtime, so can't just use
      *  `mkString(",")` in the auto-generated code)
      *  (Called as part of macro only)
      * @return A sequence with the elements converted to (possibly empty) compilable-to-runtime code strings
      *         (with commas inserted almost correctly ... see the additional logic called where `els2Str` is
      *          called for more details)
      */
    private def els2Str(els: List[Element], startsObject: Boolean): List[String]= els match {
      case Nil => Nil
      case a :: tail => el2Str(a, isFirst = startsObject) :: tail.map(el => el2Str(el, isFirst = false))
    }

    /** Utility for inserting commans correctly between non-empty elements*/
    private def addComma(isFirst: Boolean) = isFirst match {
      case true => ""
      case false => ","
    }

    /** Remove backticks from keys */
    private def removeBackTicks(key: String) =
    if (key.charAt(0) == '`') key.substring(1, key.length - 1)
    else key

  }
}

