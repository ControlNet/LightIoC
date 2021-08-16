package space.controlnet.lightioc.enumerate

import space.controlnet.lightioc.Factory.*=>

protected[lightioc] abstract class Entry[T](val id: Identifier, val scope: Scope)

private[lightioc] case class ValueEntry[T](override val id: Identifier, override val scope: Scope, value: () => T) extends Entry[T](id, scope)
private[lightioc] case class FactoryEntry[T](override val id: Identifier, override val scope: Scope, value: Any *=> T) extends Entry[T](id, scope)
private[lightioc] case class ServiceEntry[T, R](override val id: Identifier, override val scope: Scope, targetId: Identifier) extends Entry[T](id, scope)

private[lightioc] object ValueEntry
private[lightioc] object FactoryEntry
private[lightioc] object ServiceEntry
