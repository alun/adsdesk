package com.katlex.adsdesk
package snippet

import net.liftweb.http.{S, DispatchSnippet}
import net.liftweb.util.Helpers._
import xml.{Elem, NodeSeq}

object Resource extends DispatchSnippet {
  def dispatch = {
    case "script" => script
    case "css" => css
  }

  lazy val script = "* [src]" #> S.attr("src") & "* [type]" #> "text/javascript"
  lazy val css:NodeSeq => NodeSeq = _.collect {
      case e:Elem => <link rel="stylesheet" type="text/css" href={"/" + S.attr("src").openOr("global.css")}/>
    }
}
