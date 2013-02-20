package com.katlex.adsdesk
package snippet

import net.liftweb.builtin.snippet.{Menu, Loc}
import net.liftweb.http.DispatchSnippet
import xml.NodeSeq
import net.liftweb.http.js.JsCmds.{JsCrVar, Script}
import net.liftweb.http.js.JsExp._
import net.liftweb.util.Helpers._

class Title extends DispatchSnippet {
  val JS_CONST_NAME = "TITLE_SEPARATOR"
  val SEPARATOR = " :: "

  def dispatch = {
    case "joined" =>
      Loc.dispatch("i") andThen
      ("* *" #> appendSeparator _) andThen
      Menu.dispatch("title") andThen
      appendSeparatorJsConst
  }

  private def appendSeparator(ns: NodeSeq) = ns.toString + SEPARATOR
  private def appendSeparatorJsConst(ns: NodeSeq) = ns ++ Seq(Script(JsCrVar(JS_CONST_NAME, SEPARATOR)))
}
