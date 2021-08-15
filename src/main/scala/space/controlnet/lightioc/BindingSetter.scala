package space.controlnet.lightioc

import space.controlnet.lightioc.Factory.*=>
import space.controlnet.lightioc.Util.AnyExt
import space.controlnet.lightioc.enumerate.{ ClassId, Identifier, StringId }
import space.controlnet.lightioc.exception.RegistryTypeException

import scala.reflect.ClassTag

class BindingSetter[T](identifier: Identifier) {
  import BindingSetter._

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
  /**
   * Register to another registration by type
   */
  def toService[R: ClassTag](implicit tag: ClassTag[R]): ServiceScopeSetter[T, R] =
    new ServiceScopeSetter[T, R](identifier, tag.runtimeClass)

  /**
   *  Register a constructor or a value to Container in Transient scope
   */
  def ->[R <: T] : PartialFunction[Any, Container.type] = {
    case Self => toSelf.inTransientScope.done()
    case constructor : Class[R] => to[R](constructor).inTransientScope.done()
    case value : T => toValue(value).inTransientScope.done()
    case _ => throw RegistryTypeException
  }

  /**
   * Register a constructor or a value to Container in Singleton scope
   */
  def :=[R <: T] : PartialFunction[Any, Container.type] = {
    case Self => toSelf.inSingletonScope.done()
    case constructor : Class[R] => to[R](constructor).inSingletonScope.done()
    case value : T => toValue(value).inSingletonScope.done()
    case _ => throw RegistryTypeException
  }

  /**
   * Register a factory to Container.
   */
  def ~> (function: Any *=> T): Container.type = new FactoryScopeSetter(identifier, function).inTransientScope.done()

  /**
   * Register to another service.
   */
  def >> [R](targetIdentifier: Identifier): Container.type = toService[R](targetIdentifier).done()
}

object BindingSetter {
  /**
   * A object to be used as an identifier in `->` and `:=`, meaning toSelf.
   */
  object Self
}