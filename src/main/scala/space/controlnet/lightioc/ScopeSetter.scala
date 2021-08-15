package space.controlnet.lightioc

import space.controlnet.lightioc.Factory.*=>
import space.controlnet.lightioc.enumerate.{ Entry, FactoryEntry, Identifier, Scope, ServiceEntry, Singleton, Transient, ValueEntry }

sealed abstract class ScopeSetter[T](val identifier: Identifier)

sealed trait ScopeSettable {
  def inSingletonScope: EntryBuildable[_]
  def inTransientScope: EntryBuildable[_]
}

sealed trait HasScope {
  def scope: Scope
}

sealed trait SingletonScope extends HasScope {
  def scope: Scope = Singleton
}

sealed trait TransientScope extends HasScope {
  def scope: Scope = Transient
}

sealed trait HasValue {
  val value: Any
}

sealed trait ValueEntryHasValue[T] extends HasValue {
  val value: () => T
}

sealed trait FactoryEntryHasValue[T] extends HasValue {
  val value: Any *=> T
}

sealed trait EntryBuildable[T] {
  this: ScopeSetter[T] =>
  protected def entry: Entry[T]
  private def build(): Unit = Container += identifier -> entry
  def done(): Container.type = {
    build()
    Container
  }
}

sealed trait ValueEntryBuildable[T] extends EntryBuildable[T]  {
  this: ScopeSetter[T] with HasScope with ValueEntryHasValue[T] =>
  protected def entry: Entry[T] = new ValueEntry[T](identifier, scope, value)
}

sealed trait FactoryEntryBuildable[T] extends EntryBuildable[T] {
  this: ScopeSetter[T] with HasScope with FactoryEntryHasValue[T] =>
  protected def entry: Entry[T] = new FactoryEntry[T](identifier, scope, value)
}

sealed class ValueScopeSetter[T](identifier: Identifier, val value: () => T) extends ScopeSetter[T](identifier) with ScopeSettable with ValueEntryHasValue[T] {
  override def inSingletonScope: ValueScopeSetter[T] with SingletonScope with ValueEntryBuildable[T] = new ValueScopeSetter[T](identifier, value) with SingletonScope with ValueEntryBuildable[T]
  override def inTransientScope: ValueScopeSetter[T] with TransientScope with ValueEntryBuildable[T] = new ValueScopeSetter[T](identifier, value) with TransientScope with ValueEntryBuildable[T]
}

sealed class FactoryScopeSetter[T](identifier: Identifier, val value: Any *=> T) extends ScopeSetter[T](identifier) with ScopeSettable with FactoryEntryHasValue[T] {
  override def inSingletonScope: FactoryScopeSetter[T] with SingletonScope with FactoryEntryBuildable[T] = new FactoryScopeSetter[T](identifier, value) with SingletonScope with FactoryEntryBuildable[T]
  override def inTransientScope: FactoryScopeSetter[T] with TransientScope with FactoryEntryBuildable[T] = new FactoryScopeSetter[T](identifier, value) with TransientScope with FactoryEntryBuildable[T]
}

final class ServiceScopeSetter[T, R](identifier: Identifier, val targetIdentifier: Identifier) extends ScopeSetter[T](identifier) with EntryBuildable[T] {
  protected def entry: ServiceEntry[T, R] = ServiceEntry(identifier, targetScope, targetIdentifier)
  private def targetScope = Container.getEntry(targetIdentifier).scope
}

