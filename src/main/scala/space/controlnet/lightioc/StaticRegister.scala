package space.controlnet.lightioc

import com.google.common.reflect.ClassPath
import space.controlnet.lightioc.annotation.Helpers.{ NULL, Null }
import space.controlnet.lightioc.annotation.{ Provider, Singleton }

import java.lang.annotation.Annotation
import scala.reflect.ClassTag

protected trait StaticRegister {
  this: Container.type =>

  private def loader = Thread.currentThread.getContextClassLoader
  private def classPath = ClassPath.from(loader)
  private def appendInnerClasses(cls: Class[_]): List[Class[_]] =
    cls :: cls.getClasses.flatMap(appendInnerClasses).toList
  private def packageName = Container.resolve[String]("packageName")

  private def classes: List[Class[_]] = classPath.getTopLevelClassesRecursive(packageName)
    .toArray.toList.map(_.asInstanceOf[ClassPath.ClassInfo].load)
    .flatMap(appendInnerClasses)

  private def getAnnotatedClassPairs[T <: Annotation : ClassTag](implicit tag: ClassTag[T]): List[(Class[_], T)] =
    classes.map(cls => (cls, cls.getAnnotations))
    .map {
      case (cls, annotations) => (cls, annotations.find(_.annotationType == tag.runtimeClass))
    }
    .filter {
      case (cls, Some(annotation)) => true
      case (cls, None) => false
    }
    .map {
      case (cls, Some(annotation)) => (cls, annotation.asInstanceOf[T])
    }

  // Get all Provider annotated classes and fix object class
  private lazy val providers: List[(Class[_], Provider)] = getAnnotatedClassPairs[Provider].map {
    case (cls, provider) if provider.isObject && noDollarSignEnd(cls) => (Class.forName(cls.getName + '$'), provider)
    case (cls, provider) => (cls, provider)
  }
  private lazy val singletons: List[(Class[_], Singleton)] = getAnnotatedClassPairs[Singleton].map {
    case (cls, singleton) if singleton.isObject && noDollarSignEnd(cls) => (Class.forName(cls.getName + '$'), singleton)
    case (cls, singleton) => (cls, singleton)
  }

  private def noDollarSignEnd(cls: Class[_]): Boolean = cls.getName.last != '$'

  protected def staticRegister(): Unit = {
    // register to Container
    providers.foreach {
      case (cls, provider) if provider.classId() == classOf[Null] && provider.stringId() == NULL =>
        Container.register(cls).toSelf.inTransientScope.done()
      case (cls, provider) if provider.classId() != classOf[Null] && provider.stringId() == NULL =>
        Container.register(provider.classId()).to(cls).inTransientScope.done()
      case (cls, provider) if provider.stringId() != NULL =>
        Container.register(provider.stringId()).to(cls).inTransientScope.done()
    }

    singletons.foreach {
      case (cls, provider) if provider.classId() == classOf[Null] && provider.stringId() == NULL =>
        Container.register(cls).toSelf.inSingletonScope.done()
      case (cls, provider) if provider.classId() != classOf[Null] && provider.stringId() == NULL =>
        Container.register(provider.classId()).to(cls).inSingletonScope.done()
      case (cls, provider) if provider.stringId() != NULL =>
        Container.register(provider.stringId()).to(cls).inSingletonScope.done()
    }
  }
}
