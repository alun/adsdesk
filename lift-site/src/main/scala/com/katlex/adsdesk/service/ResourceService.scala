package com.katlex.adsdesk
package service

import net.liftweb.http._
import net.liftweb.common.{Full, Box, Loggable, Empty}
import script.{JsCompiler, CoffeeCompiler}
import utils.StreamImplicits._
import net.liftweb.http.InMemoryResponse
import net.liftweb.util.Props

object ResourceService extends Loggable {

  val SCRIPT_RESOURCES_ROOT:String = "scripts-hidden"
  val TEMPLATE_RESOURCES_ROOT:String = "templates-hidden"

  case class Resource(path:List[String], suffix:String) {
    override def toString = path.mkString("/", "/", ".") + suffix
    def getBytes = LiftRules.getResource(toString).map(_.openStream.toByteArray)
    def getString = getBytes.map(new String(_, "UTF-8"))
  }
  private implicit def implicitResource(t:(List[String], String)) = Resource(t._1, t._2)

  def init():Unit = {
    LiftRules.explicitlyParsedSuffixes += "tmpl"
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(SCRIPT_RESOURCES_ROOT :: _, _, _, _), _, _) =>
        RewriteResponse("bad-request.html" :: Nil, true)
    }
    LiftRules.dispatch.
      append(serveTemplate).
      append(serveScript)
  }

  val serveScript: LiftRules.DispatchPF = {
    case Req(list, "js", GetRequest) if list.indexOf("ajax_request") == -1 =>
      val scriptBase = SCRIPT_RESOURCES_ROOT :: list
      () => for {
        jsSource <- (for {
            coffeeSource <- (scriptBase -> "coffee").getString
            jsSource <- {
                val result = CoffeeCompiler.compile(coffeeSource, false)
                result.left.toOption.foreach(logger.error(_))
                result.right.toOption
              }
          } yield jsSource) or (scriptBase -> "js").getString
        jsCompiled = if (Props.devMode) jsSource else JsCompiler.compile(jsSource)
      } yield stringResponse(jsCompiled, Some("application/x-javascript"))
  }

  val serveTemplate: LiftRules.DispatchPF = {
    case Req(list, "tmpl", GetRequest) =>
      () => for {
        nodes <- Templates.apply(TEMPLATE_RESOURCES_ROOT :: list)
      } yield stringResponse(nodes.toString, Some("text/html"))
  }

  protected def stringResponse(string:String, contentType:Option[String]) =
    InMemoryResponse(
      string.getBytes("UTF-8"),
      contentType.map(v => ("Content-type" -> v) :: Nil).getOrElse(Nil),
      Nil,
      200
    )

}
