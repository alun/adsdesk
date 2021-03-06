import sbt._
import sbt.Keys._
import com.github.siasia.WebPlugin
import scala.Some
import WebPlugin._

object BuildSettings { 
  val buildOrganization = "com.katlex" 
  val buildScalaVersion = "2.10.0" 
  val buildVersion      = "0.1.0-SNAPSHOT" 
  val buildSettings = Defaults.defaultSettings ++ Seq(
        organization := buildOrganization,
        scalaVersion := buildScalaVersion,
        version      := buildVersion
  ) 
} 

object AdsDesk extends Build { 
  import BuildSettings._

  val liftVersion = "2.5-M4"

  lazy val embeddedWebAppSettings = Seq(
      resourceGenerators in Compile <+=
        (resourceManaged, baseDirectory) map { (managedBase, base) =>
          val webappBase = base / "src" / "main" / "webapp"
          for {
            (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
          } yield {
            Sync.copy(from, to)
            to
          }
        },
      // exclude war from publishing
      packagedArtifacts ~= (_.filter(_._1.`type` != "war")),
      publishArtifact in (Compile, packageBin) := true
    )

  lazy val root = Project("ads-server-root", file("."))

  lazy val server = Project("ads-lift-server", file ("lift-site"),
    settings = buildSettings ++ webSettings ++ embeddedWebAppSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "container,compile",
        "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
        "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default",
        "org.scala-sbt" % "launcher-interface" % "0.12.0" % "provided",
        "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
        "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default",
        "net.liftweb" %% "lift-testkit" % liftVersion % "compile->default",
        "com.google.javascript" % "closure-compiler" % "r2180",
        "org.mozilla" % "rhino" % "1.7R3"
      ) map (_.withSources),
      resolvers += Resolver.typesafeIvyRepo("releases"),
      publishTo := Some(Resolver.file("katlex-repo", file(sys.props("user.home") + "/katlex.github.com/maven2/snapshots")))
    )
  )
}
