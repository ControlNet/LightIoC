package space.controlnet.lightioc

import space.controlnet.lightioc.Util.AnyExt
import space.controlnet.lightioc.enumerate.{ ClassId, ConstructorEntry, Entry, FactoryEntry, Identifier, Scope, ServiceEntry, Singleton, Transient, ValueEntry }
import space.controlnet.lightioc.exception.{ NotRegisteredException, ResolveTypeException }

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.ClassTag

object Container extends StaticRegister with AutoWirer {
  val mappings: mutable.Map[Identifier, Entry[_]] = new mutable.HashMap[Identifier, Entry[_]]
  private val singletons: mutable.Map[Identifier, Any] = new mutable.HashMap[Identifier, Any]

  /**
   * Add new registry to Container.
   */
  private[lightioc] def addMapping(mapping: (Identifier, Entry[_])): Unit = mapping._2.scope match {
    case Transient => mappings += mapping
    case Singleton =>
      singletons.remove(mapping._1)
      mappings += mapping
  }
  /**
   * Alias of addMapping. Add new registry to Container.
   */
  private[lightioc] def +=(mapping: (Identifier, Entry[_])): Unit = addMapping(mapping)

  /**
   * Get registry entry in mapping by identifier.
   */
  private[lightioc] def getEntry[T: ClassTag](identifier: Identifier): Entry[T] = mappings.get(identifier) match {
    case Some(entry) => entry.asInstanceOf[Entry[T]]
    case None => throw NotRegisteredException(s"Identifier: ${ identifier.id } is not registered.")
  }

  /**
   * Get registry entry in mapping by type
   */
  private[lightioc] def getEntry[T](implicit tag: ClassTag[T]): Entry[T] = getEntry[T](tag.runtimeClass)

  private def getSingleton[T: ClassTag](identifier: Identifier): Option[T] =
    singletons.get(identifier).asInstanceOf[Option[T]]
  private def getSingleton[T](implicit tag: ClassTag[T]): Option[T] = getSingleton[T](tag.runtimeClass)

  private def getValue[T: ClassTag](entry: ValueEntry[T]): T = (entry.scope match {
    case Transient => entry.value()
    case Singleton => getSingleton(entry.id) match {
      case Some(instance) => instance
      case None =>
        val instance = entry.value()
        singletons += entry.id -> instance
        instance
    }
  }) |> autowire[T]

  private def getFromConstructor[T: ClassTag](
    entry: ConstructorEntry[T], identifier: Option[ClassId[T]], types: Seq[Class[_]])(implicit tag: ClassTag[T]): T = {

     def _getValue: T = {
      val args: Seq[Object] = types.map((t: Class[_]) => Container.resolve[Object](t: Identifier))
      identifier match {
        case Some(ClassId(id)) => id.getConstructor(types: _*).newInstance(args: _*)
        case None => tag.runtimeClass.getConstructor(types: _*).newInstance(args: _*).asInstanceOf[T]
      }
    }

    entry.scope match {
      case Transient => _getValue
      case Singleton => getSingleton[T](entry.id)(tag) match {
        case Some(instance) => instance
        case None =>
          val instance = _getValue
          singletons += entry.id -> instance
          instance
      }
    }
  }

  /**
   * Register a specified identity, it can be String or Class[_].
   *
   * @param identifier : The identifier for registration.
   * @tparam T : The type of identifier. Class[_] or String.
   */
  def register[T: ClassTag](identifier: Identifier): BindingSetter[T] = new BindingSetter(identifier)

  /**
   * Register for a type
   *
   * @tparam T : The type of registration
   */
  def register[T: ClassTag](implicit tag: ClassTag[T]): BindingSetter[T] = new BindingSetter(tag.runtimeClass)

  /**
   * Resolve the item from container by type
   */
  def resolve[T](implicit tag: ClassTag[T]): T = resolve[T](tag.runtimeClass)

  /**
   * Resolve item by identifier
   */
  @tailrec
  def resolve[T: ClassTag](identifier: Identifier): T = getEntry[T](identifier) match {
    case entry@ValueEntry(id, scope, value) => getValue[T](entry)
    case entry@ConstructorEntry(id: ClassId[T], scope, types) => getFromConstructor[T](entry, Some(id), types)
    case entry@ConstructorEntry(id, scope, types) => getFromConstructor[T](entry, None, types)
    case FactoryEntry(id, scope, factory) => factory(this)
    case ServiceEntry(id, scope, targetId) => resolve[T](targetId)
  }

  /**
   * Checking if the identifier is in Container
   */
  def has(identifier: Identifier): Boolean = mappings.keySet.contains(identifier)
  /**
   * Checking if the type is in Container
   */
  def has[T: ClassTag](implicit tag: ClassTag[T]): Boolean = has(tag.runtimeClass)

  /**
   * Initialization for static annotation registration
   */
  def init(packageName: String): Unit = {
    Container.register[String]("packageName").toValue(packageName).inSingletonScope.done()
    staticRegister()
  }
}

