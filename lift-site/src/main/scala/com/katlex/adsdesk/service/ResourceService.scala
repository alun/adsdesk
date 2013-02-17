package com.katlex.adsdesk
package service

import css.LessCompiler
import net.liftweb.http._
import net.liftweb.common.{Full, Box, Loggable, Empty}
import script.{JsCompiler, CoffeeCompiler}
import utils.StreamImplicits._
import net.liftweb.http.InMemoryResponse
import net.liftweb.util.Props

object ResourceService extends Loggable {

  val CSS_RESOURCES_ROOT:String = "css-hidden"
  val SCRIPT_RESOURCES_ROOT:String = "scripts-hidden"
  val TEMPLATE_RESOURCES_ROOT:String = "templates-hidden"

  case class Resource(path:List[String], suffix:String) {
    override def toString = path.mkString("/", "/", ".") + suffix
    def getBytes = LiftRules.getResource(toString).map(_.openStream.toByteArray)
    def getString = getBytes.map(new String(_, "UTF-8"))
  }
  private implicit def implicitResource(t:(List[String], String)) = Resource(t._1, t._2)

  def init():Unit = {
    // close access to hidden resources folders
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(p :: _, _, _, _), _, _)
        if p == CSS_RESOURCES_ROOT || p == SCRIPT_RESOURCES_ROOT || p == TEMPLATE_RESOURCES_ROOT =>
          RewriteResponse("bad-request.html" :: Nil, true)
    }

    // no need for state while generating JS and CSS
    LiftRules.statelessDispatch.
      append(Serve.script).
      append(Serve.css)

    // templates need session
    LiftRules.explicitlyParsedSuffixes += "tmpl"
    LiftRules.dispatch.append(Serve.template)
  }

  object Build {
    def script(path: List[String]) =
      for {
        jsSource <- (for {
          coffeeSource <- (path -> "coffee").getString
          jsSource <- {
            val result = CoffeeCompiler.compile(coffeeSource, false)
            result.left.toOption.foreach(logger.error(_))
            result.right.toOption
          }
        } yield jsSource) or (path -> "js").getString
        jsCompiled = if (Props.devMode) jsSource else JsCompiler.compile(jsSource)
      } yield jsCompiled

    def css(path: List[String]) =
      for {
        lessSource <- (path -> "less").getString
        cssSource <- {
          val result = LessCompiler.compile(path.mkString("/") + ".css", lessSource, Props.devMode)
          result.left.toOption.foreach(logger.error(_))
          result.right.toOption.map(_.cssContent)
        }
      } yield cssSource
  }

  object Serve {
    lazy val script: LiftRules.DispatchPF = {
      case Req(list, "js", GetRequest) if list.indexOf("ajax_request") == -1 =>
        debugReq("JS", list)
        Build.script(SCRIPT_RESOURCES_ROOT :: list) -> Some("application/x-javascript")
    }

    lazy val css: LiftRules.DispatchPF = {
      case Req(list, "css", GetRequest) if list.indexOf("css") == -1 =>
        debugReq("CSS", list)
        Build.css(CSS_RESOURCES_ROOT :: list) -> Some("text/css")
    }

    lazy val template: LiftRules.DispatchPF = {
      case Req(list, "tmpl", GetRequest) =>
        debugReq("Template", list)
        S.runTemplate(TEMPLATE_RESOURCES_ROOT :: list) -> Some("text/html")
    }

    implicit def boxWithMimeTypeToLazyBoxedLiftResponse[T >: AnyRef](
                                t: (Box[T], Option[String])): () => Box[LiftResponse] = t match {
      case (box, mimeType) =>
        () => for {
          unBoxed <- box
        } yield stringResponse(unBoxed.toString, mimeType)
    }

    protected def stringResponse(string:String, contentType:Option[String]) =
      InMemoryResponse(
        string.getBytes("UTF-8"),
        contentType.map(v => ("Content-Type" -> v) :: Nil).getOrElse(Nil),
        Nil,
        200
      )

    protected def debugReq(kind:String, path:List[String]) =
      logger.debug(s"${kind} request: " + path.mkString("/"))
  }


}
