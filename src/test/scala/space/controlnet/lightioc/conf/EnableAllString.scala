package space.controlnet.lightioc.conf

import space.controlnet.lightioc.Container

trait EnableAllString {
  Container.init("space.controlnet.lightioc", allStringId = true)
}
