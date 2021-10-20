package space.controlnet.lightioc.enumerate

import scala.language.implicitConversions
import scala.reflect.ClassTag

private[lightioc] sealed trait Identifier { val id: Any }
private[lightioc] case class ClassId[T](id: Class[T])(implicit val tag: ClassTag[T]) extends Identifier
private[lightioc] case class StringId(id: String) extends Identifier

private[lightioc] object Identifier {
  implicit def asClassId[T: ClassTag](id: Class[T]): ClassId[T] = ClassId[T](id)
  implicit def asStringId(id: String): StringId = StringId(id)

  final val NullId = StringId(null)
}
private[lightioc] object ClassId
private[lightioc] object StringId
