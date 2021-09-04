package space.controlnet.lightioc

import space.controlnet.lightioc.Util.AnyExt
import space.controlnet.lightioc.enumerate._
import space.controlnet.lightioc.exception.{ NotImplementedException, NotRegisteredException }

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.ClassTag

object Container extends StaticRegister with AutoWirer {
  val mappings: mutable.Map[Identifier, Entry[_]] = new mutable.HashMap[Identifier, Entry[_]]
  private val singletons: mutable.Map[Identifier, Any] = new mutable.HashMap[Identifier, Any]

  /**
   * Add new registry to Container.
   */
  private[lightioc] def addMapping(mapping: (Identifier, Entry[_])): Unit = {
    val newMapping = (mapping._1, allStringId) match {
      case (ClassId(id), true) => (StringId(id.getName), mapping._2)
      case _ => mapping
    }

    newMapping._2.scope match {
      case Transient => mappings += newMapping
      case Singleton =>
        singletons.remove(newMapping._1)
        mappings += newMapping
    }
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
    case None => throw NotRegisteredException(s"Identifier ${ identifier.getClass.getSimpleName }: {${ identifier.id }} is not registered.")
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
    entry: ConstructorEntry[T], identifier: Option[Identifier], types: Seq[Class[_]])(implicit tag: ClassTag[T]): T = {

    def _getValue: T = {
      val args: Seq[Object] = types.map((t: Class[_]) => Container.resolve[Object](t: Identifier))
      def _getInstance(cls: Class[_]): T = cls.getConstructor(types: _*).newInstance(args: _*).asInstanceOf[T]

      identifier match {
        case Some(ClassId(id)) => _getInstance(id)
        case Some(StringId(id)) if allStringId => _getInstance(Class.forName(id))
        case Some(StringId(_)) if !allStringId =>
          throw NotImplementedException("Constructor binding with for custom string ID is not implemented.")
        case None if allStringId => _getInstance(tag.runtimeClass)
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
  def resolve[T](implicit tag: ClassTag[T]): T = resolve[T] {
    val cls = tag.runtimeClass
    if (allStringId) StringId(cls.getName)
    else ClassId(cls)
  }

  /**
   * Resolve item by identifier
   */
  @tailrec
  def resolve[T: ClassTag](identifier: Identifier): T = getEntry[T](identifier) match {
    case entry@ValueEntry(id, scope, value) => getValue[T](entry)
    case entry@ConstructorEntry(id: ClassId[T], scope, types) => getFromConstructor[T](entry, Some(id), types)
    case entry@ConstructorEntry(id: StringId, scope, types) if allStringId => getFromConstructor[T](entry, Some(id), types)
    case entry@ConstructorEntry(id, scope, types) => getFromConstructor[T](entry, None, types)
    case FactoryEntry(id, scope, factory) => factory(this)
    case ServiceEntry(id, scope, targetId) => resolve[T](targetId)
  }

  /**
   * Resolve item with default value if that item is not registered.
   */
  def resolveOrElse[T: ClassTag](identifier: Identifier, default: T): T =
    if (has(identifier)) resolve[T](identifier)
    else default

  /**
   * Checking if the identifier is in Container
   */
  def has(identifier: Identifier): Boolean = mappings.keySet.contains(identifier)
  /**
   * Checking if the type is in Container
   */
  def has[T: ClassTag](implicit tag: ClassTag[T]): Boolean =
    if (!allStringId) has(tag.runtimeClass)
    else has(tag.runtimeClass.getName)

  /**
   * Initialization for static annotation registration
   * @param packageName The package name of scan range
   * @param allStringId True then use Class name string as ID instead of Class object. It is for multiple ClassLoader.
   */
  def init(packageName: String, allStringId: Boolean = false): Unit = {
    Container.register[String]("packageName") := packageName
    Container.register[Boolean]("allStringId") := allStringId
    staticRegister()
  }

  private[lightioc] def allStringId: Boolean = resolveOrElse("allStringId", false)

  /**
   * Remove everything in the Container.
   */
  def reset(): Unit = mappings.clear()
}
