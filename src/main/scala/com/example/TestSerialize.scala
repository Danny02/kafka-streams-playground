package com.example

import com.example.M2NResolver.BetreuerZuordnungInfo
import io.circe.generic.auto._
import io.circe.syntax._

object TestSerialize {

  def main(args: Array[String]): Unit = {
    println(List(Betreuer(BetreuerId("somebid"), "name")).asJson)
  }
}
