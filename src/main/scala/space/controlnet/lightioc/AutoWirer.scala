package space.controlnet.lightioc

import space.controlnet.lightioc.annotation.Autowired
import space.controlnet.lightioc.annotation.Helpers.{ NULL, Null }

import java.lang.reflect.Field

trait AutoWirer {

  private def getAutowiredFields(cls: Class[_]): List[(Field, Autowired)] = {
    cls.getDeclaredFields.toList.map {
      field => (field, field.getAnnotations)
    }.map {
      case (field, annotations) => (field, annotations.find(_.annotationType == classOf[Autowired]))
    }.filter {
      _._2.isDefined
    }.map {
      case (field, Some(annotation)) => (field, annotation.asInstanceOf[Autowired])
    }
  }

  private def wireField[T](obj: T, field: Field, annotation: Autowired): T = {
    field.setAccessible(true)
    val value = (annotation.stringId, annotation.classId) match {
      case (NULL, _: Class[Null]) => Container.resolve[Any](field.getType)
      case (NULL, _: Class[_]) => Container.resolve[Any](annotation.classId)
      case (_, _) => Container.resolve[Any](annotation.stringId())
    }
    field.set(obj, value)
    obj
  }

  def autowire[T](obj: T): T = {
    getAutowiredFields(obj.getClass).map {
      case (field, autowired) => wireField[T](obj, field, autowired)
    }
    obj
  }

}
