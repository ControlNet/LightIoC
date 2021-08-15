package space.controlnet.lightioc.enumerate

import space.controlnet.lightioc.Factory.*=>

abstract class Entry[T](val id: Identifier, val scope: Scope)

case class ValueEntry[T](override val id: Identifier, override val scope: Scope, value: () => T) extends Entry[T](id, scope)
case class FactoryEntry[T](override val id: Identifier, override val scope: Scope, value: Any *=> T) extends Entry[T](id, scope)
case class ServiceEntry[T, R](override val id: Identifier, override val scope: Scope, targetId: Identifier) extends Entry[T](id, scope)
