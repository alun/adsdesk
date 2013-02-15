package com.katlex.adsdesk
package boot

import utils.{StreamImplicits}
import org.mortbay.jetty
import org.mortbay.jetty.webapp.WebAppContext
import java.io.File
import java.util.zip.ZipEntry
import net.liftweb.common.{Failure, Full, Box}
import java.util.jar.{JarEntry, JarFile}
import net.liftweb.util.Helpers._

/**
 * Start embedded Jetty server with AdsDesk server application deployed on it as root app
 */
object Server extends EntryPoint {

  val RUN_DIR = new File(System.getProperty("user.home") + File.separator + ".adsserver")

  val BAD_CLASSPATH_ERROR = "Bad classpath" -> 1
  val JAR_EXTRACTING_ERROR = "Webapp extraction error" -> 2
  val SERVER_DOWN = "Server is down" -> 2

  val DEFAULT_SERVER_PORT = 8080
  val DEFAULT_RUN_MODE = "production"

  def run(args:Array[String]) = installWebapp match {
    case Left(e) => handleError(e)
    case Right(webappRoot) =>
      defaultSystemProperty("run.mode", DEFAULT_RUN_MODE)
      bootstrap.liftweb.Boot.loggingSetup.foreach(_())

      val port = (Box !! System.getProperty("adsdesk.port")).flatMap(x => tryo(x.toInt)).openOr(DEFAULT_SERVER_PORT)
      val server = new jetty.Server(port)
      val context = new WebAppContext
      context.setServer(server)
      context.setContextPath("/")
      context.setWar(webappRoot)

      server.addHandler(context)

      tryo {
        server.start()
        server.join()
      } match {
        case Failure(_, ebox, _) =>
          ebox.foreach(_.printStackTrace())
          handleError(SERVER_DOWN)
        case _ => 0
      }
  }

  def handleError(e: (String, Int)) = {
    val (error, code) = e
    println(error)
    code
  }

  /**
   * Installs webapp by extracting it from JAR or targeting sources
   */
  def installWebapp: Either[(String, Int), String] = {
    getClass.getResource("/webapp").toString match {
      case null => Left(BAD_CLASSPATH_ERROR)
      case res if !res.startsWith("jar:") => Right(res)
      case res => unzipWebApp(res.split("!")(0)) match {
          case Full(_) => Right(RUN_DIR.getPath)
          case Failure(_, Full(e), _) =>
            e.printStackTrace()
            Left(JAR_EXTRACTING_ERROR)
          case _ => Left(JAR_EXTRACTING_ERROR)
        }
    }
  }

  def prepareRunDir = {
    if (RUN_DIR.exists()) {
      def deleteRecursive(file:File) {
        if (file.isDirectory) {
          for {
            child <- file.listFiles()
          } deleteRecursive(child)
        }
        file.delete()
      }
      deleteRecursive(RUN_DIR)
    }
    RUN_DIR.mkdirs()
  }

  def unzipWebApp(jarPath:String) = {
    prepareRunDir

    val jarFile = new JarFile(jarPath.substring(9))
    val entries = jarFile.entries()

    /*
     * Filter extractor of webapp file jar entry and target file (where to install)
     */
    val WebAppFile = new {
      def unapply(entry:ZipEntry) = {
        val name = entry.getName
        if (!entry.isDirectory && name.startsWith("webapp"))
          Some((entry, new File(RUN_DIR, name.replace("webapp/", ""))))
        else None
      }
    }

    val webappEntries = for {
      WebAppFile(entry, target) <- new Iterator[JarEntry] {
        def hasNext = entries.hasMoreElements
        def next() = entries.nextElement
      }
    } yield (entry, target)

    import StreamImplicits._
    webappEntries.foldLeft[Box[Unit]](Full(())) {
      case (prevBox, (entry, target)) =>
        prevBox.flatMap(_ => jarFile.unzipEntry(entry, target))
    }
  }

  def defaultSystemProperty(name:String, default:String) {
    import System._
    val value = getProperty(name)
    if (value == null) {
      setProperty(name, default)
    }
  }
}
