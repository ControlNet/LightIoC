package space.controlnet.lightioc.enumerate

import scala.language.implicitConversions
import scala.reflect.ClassTag

sealed trait Identifier
case class ClassId[T: ClassTag](id: Class[T])(implicit val tag: ClassTag[T]) extends Identifier
case class StringId(id: String) extends Identifier

object Identifier {
  implicit def asClassId[T: ClassTag](id: Class[T]): ClassId[T] = ClassId[T](id)
  implicit def asStringId(id: String): StringId = StringId(id)

  final val NullId = StringId(null)
}