package com.katlex.adsdesk.snippet

import net.liftweb.json.JsonAST.JValue
import xml.{Text, Attribute, Elem, NodeSeq}
import net.liftweb.http.S
import net.liftweb.http.js.JE.{JsFunc, JsRaw}
import net.liftweb.http.js.JsCmd
import net.liftweb.json.JsonAST.JString
import net.liftweb.http.js.{JsExp, JsCmds}
import JsCmds.Run
import JsExp._

trait AjaxBinding {

  def bindDataProcessor(f: PartialFunction[JValue, JValue]): NodeSeq => NodeSeq =
    _.collect {
      case e:Elem =>
        val (jsCall, jsCmd) = S.createJsonFunc { handlerWrapper(f) }
        val bindHandler = (JsRaw("funcId") === JString(jsCall.funcId)).cmd & Run("bindDataHandler()")
        S.appendGlobalJs(jsCmd)
        e % Attribute("ng-init", Text(bindHandler.toJsCmd), e.attributes)
    }

  def handlerWrapper(f: PartialFunction[JValue, JValue]) =
    new PartialFunction[JValue, JsCmd] {
      def isDefinedAt(x: JValue) = f.isDefinedAt(x)

      def apply(v: JValue) = {
        val fnName = S.request.get.paramNames.headOption.get
        JsFunc("ajaxCallback", JString(fnName), f(v)).cmd
      }
    }

}
