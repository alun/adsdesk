package com.katlex.adsdesk
package utils

import net.liftweb.common.{Empty, Logback, Box, Logger}

object Logging {
  val CONFIG = "/conf/logconfig.xml"
  private var wasSetup = false
  def setup =
    if (!wasSetup) {
      wasSetup = true
      (Box !! getClass.getResource(CONFIG)) map (Logback.withFile(_) _)
    } else Empty
}