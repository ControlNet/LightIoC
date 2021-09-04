package space.controlnet.lightioc

import space.controlnet.lightioc.Util.AnyExt
import space.controlnet.lightioc.enumerate._
import space.controlnet.lightioc.exception.NotRegisteredException

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.ClassTag

object Container extends StaticRegister with AutoWirer {
  val mappings: mutable.Map[Identifier, Entry[_]] = new mutable.HashMap[Identifier, Entry[_]] // TODO modifier
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

  private def getFromConstructor[T](entry: ConstructorEntry[T], identifier: Identifier)(implicit tag: ClassTag[T]): T = {

    def types: Seq[Class[_]] = entry.types.map {
      case StringId(id) => load(id)
      case ClassId(cls) => cls
    }

    def _getValue: T = {
      val args: Seq[Object] = entry.types.map(Container.resolve[Object](_))
      def _getInstance(cls: Class[_]): T = cls.getConstructor(types: _*).newInstance(args: _*).asInstanceOf[T]

      (entry.id, identifier, allStringId) match {
        // ID is converted from class to string
        case (ClassId(_), StringId(id), true) => _getInstance(load(id))
        // Disable string id conversion and the input is a ClassId
        case (ClassId(_), ClassId(cls), false) => _getInstance(cls)
        // Input is a StringId. So use runtime class to infer where class the constructor is
        case (StringId(_), StringId(_), _) => _getInstance(tag.runtimeClass)
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
  def register[T](identifier: Identifier): BindingSetter[T] = new BindingSetter(identifier)

  /**
   * Register for a type
   *
   * @tparam T : The type of registration
   */
  def register[T](implicit tag: ClassTag[T]): BindingSetter[T] = new BindingSetter(tag.runtimeClass)

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
    case entry: ValueEntry[T] => getValue[T](entry)
    case entry: ConstructorEntry[T] => getFromConstructor[T](entry, identifier)
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
   * @param classLoader The class loader used in this Container
   */
  def init(packageName: String, allStringId: Boolean = false, classLoader: => ClassLoader = Thread.currentThread.getContextClassLoader): Unit = {
    Container.register[String]("packageName") := packageName
    Container.register[Boolean]("allStringId") := allStringId
    Container.register[ClassLoader]("classLoader") := classLoader
    staticRegister()
  }

  private[lightioc] def allStringId: Boolean = resolveOrElse("allStringId", false)

  private[lightioc] def checkAndConvert(identifier: Identifier): Identifier = (identifier, allStringId) match {
    case (ClassId(id), true) => StringId(id.getName)
    case _ => identifier
  }

  protected def loader: ClassLoader = resolveOrElse("classLoader", Thread.currentThread.getContextClassLoader)
  protected[lightioc] def load(className: String): Class[_] = Class.forName(className, true, loader)

  /**
   * Remove everything in the Container.
   */
  def reset(): Unit = mappings.clear()
}
