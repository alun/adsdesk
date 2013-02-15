package bootstrap.liftweb

import net.liftweb._
import common._
import http._
import mongodb.{MongoHost, MongoAddress, DefaultMongoIdentifier, MongoDB}
import sitemap._

import util.{Mailer, Props}
import javax.mail.{PasswordAuthentication, Authenticator}
import io.Source

class Boot extends LazyLoggable {

  def boot {
    LiftRules.addToPackages("com.katlex.adsdesk")

    setupDB
    Logger.setup = Boot.loggingSetup

    LiftRules.setSiteMap(SiteMap(
      Menu.i("Main") / "index"
    ))

   // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQueryArtifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    LiftRules.dispatch.append {
      case Req(list, "js", GetRequest) =>
        val resourceName = "/resources-hidden/js/" + list.mkString("/") + ".js"
        logger.debug(resourceName + " requested")
        () => (for {
          url <- LiftRules.getResource(resourceName)
        } yield {
          logger.debug(url)
          val data = Source.fromURL(url).mkString.getBytes("UTF-8")
          InMemoryResponse(data, "Contenet-type" -> "application/javascript" :: Nil, Nil, 200)
        })
    }

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
object Boot {
  lazy val loggingSetup =
    for {
      url <- Box !! getClass.getResource("/conf/logconfig.xml")
    } yield Logback.withFile(url) _
}
