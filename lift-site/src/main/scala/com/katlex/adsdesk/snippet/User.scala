package com.katlex.adsdesk.snippet

import net.liftweb.http.S
import net.liftweb.json.JsonAST.{JValue, JString}
import net.liftweb.http.js.JE.{JsRaw, JsFunc}
import net.liftweb.common.Loggable
import S.PFPromoter._
import net.liftweb.http.js.JsCmd
import xml.{Text, Attribute, Elem, NodeSeq}

object User extends Loggable {

  def receiveLogin : PartialFunction[JValue, JsCmd] = {
    case JString(str) ⇒ JsFunc("window.alert", JString(str)).cmd
    case v => logger.debug(v)
  }

  lazy val login: NodeSeq => NodeSeq = _.collect {
    case e:Elem =>
      val (jsCall, jsCmd) = S.createJsonFunc(receiveLogin)
      val initJS = JsRaw("funcId") === JString(jsCall.funcId)
      S.appendGlobalJs(jsCmd)
      e % Attribute("ng-init", Text(initJS.toJsCmd), e.attributes)
  }

}
