package space.controlnet.lightioc

import space.controlnet.lightioc.Factory.*=>
import space.controlnet.lightioc.Util.AnyExt
import space.controlnet.lightioc.enumerate.{ ClassId, Identifier, StringId }

import scala.reflect.ClassTag

class BindingSetter[T](identifier: Identifier) {
  /**
   * Only support class with no-parameter constructor yet
   * Register to another class implementation
   */
  def to[R <: T](constructor: Class[R]): ValueScopeSetter[T] = (constructor match {
    case cls: Class[_] if isScalaObject(cls) => () => getScalaObject(cls.getName)
    case cls: Class[_] => () => constructor.getConstructor().newInstance()
  }) |> _to

  /**
   * Register to the constructor or value of itself
   */
  def toSelf: ValueScopeSetter[T] = (identifier match {
    case ClassId(clazz) if isScalaObject(clazz) => () => getScalaObject(clazz.getName)
    case ClassId(clazz) => () => clazz.getConstructor().newInstance()
    case StringId(string) => () => string
  }).asInstanceOf[() => T] |> _to

  /**
   * Register to a value.
   */
  def toValue(value: T): ValueScopeSetter[T] = (() => value) |> _to

  private def _to(value: () => T): ValueScopeSetter[T] = new ValueScopeSetter(identifier, value)

  /**
   * Check if the class is a Scala Object
   * As the Scala Object has a '$' in the last of JVM reflection name.
   */
  private def isScalaObject(cls: Class[_]): Boolean = cls.getName.last == '$'
  private def getScalaObject(name: String): T = {
    val clazz = java.lang.Class.forName(name)
    clazz.getField("MODULE$").get(clazz).asInstanceOf[T]
  }

  /**
   * Register to a factory function.
   */
  def toFactory(function: Any *=> T): FactoryScopeSetter[T] = new FactoryScopeSetter(identifier, function)

  /**
   * Register to another registration
   */
  def toService[R](targetIdentifier: Identifier): ServiceScopeSetter[T, R] =
    new ServiceScopeSetter[T, R](identifier, targetIdentifier)

  def toService[R: ClassTag](implicit tag: ClassTag[R]): ServiceScopeSetter[T, R] =
    new ServiceScopeSetter[T, R](identifier, tag.runtimeClass)
}
