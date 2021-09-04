package space.controlnet.lightioc.main

import org.scalatest.funspec.AnyFunSpec
import space.controlnet.lightioc.{ AutowiredTest, DynamicRegistryTest, OperatorsTest, StaticRegistryTest }
import space.controlnet.lightioc.conf.EnableAllStringIdConf

class EnableAllStringIdTest extends AnyFunSpec with EnableAllStringIdConf
  with DynamicRegistryTest
  with StaticRegistryTest
  with AutowiredTest
  with OperatorsTest
