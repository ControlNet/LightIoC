package space.controlnet.lightioc

import space.controlnet.lightioc.Util.{ AnyExt, Factory }
import space.controlnet.lightioc.enumerate.{ ClassId, Identifier, StringId }
import space.controlnet.lightioc.exception.RegistryTypeException

import scala.reflect.ClassTag

private[lightioc] class BindingSetter[T](identifier: Identifier) {
  import BindingSetter._
  /**
   * Only support class with no-parameter constructor yet
   * Register to another class implementation
   */
  def to[R <: T](constructor: Class[R]): ValueScopeSetter[T] = (constructor match {
    case cls: Class[_] if isScalaObject(cls) => () => getScalaObject(cls.getName)
    case cls: Class[_] => () => Container.load(cls.getName).newInstance().asInstanceOf[R]
  }) |> _to

  /**
   * Register to the constructor or value of itself
   */
  def toSelf: ValueScopeSetter[T] = (identifier match {
    case ClassId(clazz) if isScalaObject(clazz) => () => getScalaObject(clazz.getName)
    case ClassId(clazz) => () => Container.load(clazz.getName).newInstance()
    case StringId(string) => () => string
  }).asInstanceOf[() => T] |> _to

  /**
   * Register to a constructor with parameters
   * @param types The types of parameters for the constructor.
   */
  def toConstructor(types: Identifier*): ConstructorScopeSetter[T] = identifier match {
    case ClassId(id) => new ConstructorScopeSetter(identifier, types.map(Container.checkAndConvert))
    case StringId(id) => throw RegistryTypeException("Constructor bindings only support ClassId.")
  }

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
    val clazz = Container.load(name)
    clazz.getField("MODULE$").get(clazz).asInstanceOf[T]
  }

  /**
   * Register to a factory function.
   */
  def toFactory(function: Factory[T]): FactoryScopeSetter[T] = new FactoryScopeSetter(identifier, function)

  /**
   * Register to another registration
   */
  def toService[R](targetIdentifier: Identifier): Container.type =
    new ServiceScopeSetter[T, R](identifier, Container.checkAndConvert(targetIdentifier)).done()
  /**
   * Register to another registration by type
   */
  def toService[R](implicit tag: ClassTag[R]): Container.type = toService[R](tag.runtimeClass)

  /**
   *  Register a constructor or a value to Container in Transient scope
   */
  def ->[R <: T] : PartialFunction[Any, Container.type] = {
    case Self => toSelf.inTransientScope()
    case New(types@_*) => toConstructor(types: _*).inTransientScope()
    case cls : Class[R] => to[R](cls).inTransientScope()
    case value : T => toValue(value).inTransientScope()
    case other => throw RegistryTypeException(s"Wrong registry type, get: $other")
  }

  /**
   * Register a constructor or a value to Container in Singleton scope
   */
  def :=[R <: T] : PartialFunction[Any, Container.type] = {
    case Self => toSelf.inSingletonScope()
    case New(types@_*) => toConstructor(types: _*).inSingletonScope()
    case cls : Class[R] => to[R](cls).inSingletonScope()
    case value : T => toValue(value).inSingletonScope()
    case other => throw RegistryTypeException(s"Wrong registry type, get: $other")
  }

  /**
   * Register a factory to Container.
   */
  def ~> (function: Factory[T]): Container.type = new FactoryScopeSetter(identifier, function).inTransientScope()

  /**
   * Register to another service.
   */
  def >> [R](targetIdentifier: Identifier): Container.type = toService[R](targetIdentifier)
}

object BindingSetter {
  /**
   * A object to be used as an identifier in `->` and `:=`, meaning toSelf.
   */
  object Self

  /**
   * A class to be used to register a constructor with parameters.
   */
  case class New(types: Identifier*)
}
