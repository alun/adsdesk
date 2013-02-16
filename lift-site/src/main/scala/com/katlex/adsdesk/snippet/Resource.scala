package com.katlex.adsdesk
package snippet

import net.liftweb.http.{S, DispatchSnippet}
import net.liftweb.util.Helpers._
import net.liftweb.builtin.snippet.Embed

object Resource extends DispatchSnippet {
  def dispatch = {
    case "script" => script
//    case "css" => css
    case "template" => template
  }

  lazy val template = Embed.render _
  lazy val script = "* [src]" #> S.attr("src") & "* [type]" #> "text/javascript"
}
