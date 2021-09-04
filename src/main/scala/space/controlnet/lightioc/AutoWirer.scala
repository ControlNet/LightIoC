package space.controlnet.lightioc

import space.controlnet.lightioc.annotation.Autowired
import space.controlnet.lightioc.annotation.Constants.{ NULL, Null }
import space.controlnet.lightioc.enumerate.Identifier

import java.lang.reflect.Field

protected trait AutoWirer {
  this: Container.type =>

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
    val identifier: Identifier = (annotation.stringId, annotation.classId, allStringId) match {
      case (NULL, _: Class[Null], false) => field.getType
      case (NULL, _: Class[Null], true) => field.getType.getName
      case (NULL, _: Class[_], false) => annotation.classId
      case (NULL, _: Class[_], true) => annotation.classId.getName
      case (_, _, _) => annotation.stringId
    }
    field.set(obj, resolve(identifier))
    obj
  }

  protected[lightioc] def autowire[T](obj: T): T = {
    getAutowiredFields(obj.getClass).map {
      case (field, autowired) => wireField[T](obj, field, autowired)
    }
    obj
  }

}
