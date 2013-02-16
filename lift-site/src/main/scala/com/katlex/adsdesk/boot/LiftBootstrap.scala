package com.katlex.adsdesk
package boot

import net.liftweb._
import common._
import common.Full
import http._
import http.Html5Properties
import mongodb.{MongoHost, MongoAddress, DefaultMongoIdentifier, MongoDB}
import sitemap._

import util.{Mailer, Props}
import javax.mail.{PasswordAuthentication, Authenticator}
import com.katlex.adsdesk.service.ResourceService

class LiftBootstrap extends Bootable with LazyLoggable {

  def boot() {
    LiftRules.addToPackages("com.katlex.adsdesk")

    setupDB
    Logger.setup = LiftBootstrap.loggingSetup

    LiftRules.setSiteMap(SiteMap(
      Menu.i("Main") / "index",
      Menu.i("Bids") / "bids",
      Menu.i("Blog") / "blog",
      Menu.i("Service") / "service",
      Menu.i("Help") / "help"
    ))

   // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQueryArtifacts

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("login" :: Nil, "", _, false), GetRequest, _) =>
        RewriteResponse("index" :: Nil)
    }

    ResourceService.init()

    configMailer
  }

  private def setupDB {
    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("127.0.0.1"), "adsdesk"))
  }

  private def configMailer {

    var isAuth = Props.get("mail.smtp.auth", "false").toBoolean

    Mailer.customProperties = Props.get("mail.smtp.host", "localhost") match {
      case "smtp.gmail.com" =>
        isAuth = true
        Map(
          "mail.smtp.host" -> "smtp.gmail.com",
          "mail.smtp.port" -> "587",
          "mail.smtp.auth" -> "true",
          "mail.smtp.starttls.enable" -> "true"
        )
      case h => Map(
        "mail.smtp.host" -> h,
        "mail.smtp.port" -> Props.get("mail.smtp.port", "25"),
        "mail.smtp.auth" -> isAuth.toString
      )
    }

    if (isAuth) {
      (Props.get("mail.smtp.user"), Props.get("mail.smtp.pass")) match {
        case (Full(username), Full(password)) =>
          Mailer.authenticator = Full(new Authenticator() {
            override def getPasswordAuthentication = new
              PasswordAuthentication(username, password)
          })
        case _ => logger.error("Username/password not supplied for Mailer.")
      }
    }
  }
}
object LiftBootstrap {
  lazy val loggingSetup =
    for {
      url <- Box !! getClass.getResource("/conf/logconfig.xml")
    } yield Logback.withFile(url) _
}
