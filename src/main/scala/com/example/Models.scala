package com.example

import java.util.UUID

case class Betreuer(id: UUID, name: String)

case class Zuordnung(betreuerId: UUID, info: String)
case class Zuordnungen(schulId: UUID, zuordnungen: List[Zuordnung])
