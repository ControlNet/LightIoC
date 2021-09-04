package space.controlnet.lightioc.main

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.conf.EnableAllString
import space.controlnet.lightioc.{ AutowiredTest, Container, DynamicRegistryTest, OperatorsTest, StaticRegistryTest }

class EnableAllStringIdTest extends AnyFunSpec with BeforeAndAfterAll with EnableAllString
  with DynamicRegistryTest
  with StaticRegistryTest
  with AutowiredTest
  with OperatorsTest {

  override protected def afterAll(): Unit = Container.reset()
}
