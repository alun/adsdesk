package com.katlex.adsdesk.snippet

import net.liftweb.json.JsonAST.JString
import net.liftweb.common.Loggable
import net.liftweb.json.JsonDSL._

object User extends Loggable with AjaxBinding {

  lazy val loginHandler = bindDataProcessor {
    case JString(str) ⇒ str
  }

}
