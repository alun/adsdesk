import sbt._
import Keys._
import com.github.siasia.WebPlugin
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

object Dependencies { 
  val siteResolvers = Seq(
    Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)
  )
} 

object AdsDesk extends Build { 
  import Dependencies._ 
  import BuildSettings._

  val liftVersion = "2.5-M4"

  lazy val useWebappAsResource = resourceGenerators in Compile <+=
    (resourceManaged, baseDirectory) map { (managedBase, base) =>
      val webappBase = base / "src" / "main" / "webapp"
      for {
        (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
      } yield {
        Sync.copy(from, to)
        to
      }
    }

  lazy val server = Project("ads-lift-server", file ("lift-site"),
    settings = buildSettings ++ webSettings ++ Seq(
      useWebappAsResource,
      libraryDependencies ++= Seq(
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "container,compile",
        "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
        "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default",
        "org.scala-sbt" % "launcher-interface" % "0.12.0" % "provided",
        "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
        "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default",
        "com.yahoo.platform.yui" % "yuicompressor" % "2.3.6"
      ) map (_.withSources),

      // exclude war from publishing
      packagedArtifacts ~= (_.filter(_._1.`type` != "war")),
      //resolvers ++= siteResolvers,
      publishArtifact in (Compile, packageBin) := true,

      publishTo := Some(Resolver.ssh("katlex-repo", "katlex.com", 1022, "katlex/maven2") 
        withPermissions("0644") as ("alun", Path(Path.userHome) / ".ssh/id_rsa"))
    )
  )
}
