package space.controlnet.lightioc.samples

import space.controlnet.lightioc.annotation.Provider


@Provider(stringId = NamedProviderClass.ID)
class NamedProviderClass

object NamedProviderClass {
  final val ID = "NamedProviderClass"
}