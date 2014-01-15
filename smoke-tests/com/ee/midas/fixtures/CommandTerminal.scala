package com.ee.midas.fixtures

import org.specs2.form.Form
import org.specs2.specification.Forms._

case class CommandTerminal(args: String*) {
  var commandLine: String = args.mkString(" ")
  val terminal = MidasUtils

  def startMidas = {
    terminal.startMidas(commandLine)
    Thread.sleep(4000)
    Form("Open Command Terminal").
      tr(field(">", s"midas ${commandLine}"))
  }
  def stopMidas(port: Int) = {
    println("in stop midas................"+port)
    terminal.stopMidas(port)
    Form("Open Command Terminal").
    tr(field("Shut Down Midas"))
  }

}
