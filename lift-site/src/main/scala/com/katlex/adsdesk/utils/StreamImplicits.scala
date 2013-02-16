package com.katlex.adsdesk
package utils

import java.io.{FileOutputStream, File, InputStream}
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import net.liftweb.common.Box
import net.liftweb.util.Helpers._

object StreamImplicits {
  case class InputStreamOps(input:InputStream) {
    def ==> (sink: File):Box[InputStream] = tryo {
      val buffer = Array.ofDim[Byte](4092)
      sink.getParentFile.mkdirs()
      val output = new FileOutputStream(sink)

      def copyNextBlock: Unit = {
        val read = input.read(buffer)
        if (read != -1) {
          output.write(buffer, 0, read)
          copyNextBlock
        }
      }
      copyNextBlock
      output.close()
      input
    }
    def toByteArray = {
      val builder = Vector.newBuilder[Byte]
      def readRec: Array[Byte] =
        input.read match {
          case -1 =>
            builder.result().toArray
          case v =>
            builder += v.toByte
            readRec
        }
      readRec
    }
  }

  case class JarFileOps(jarFile:JarFile) {
    def unzipEntry(entry:ZipEntry, sink:File):Box[Unit] =
      (jarFile.getInputStream(entry) ==> sink).flatMap(is => tryo(is.close()))
  }

  implicit def inputStreamOps(input:InputStream):InputStreamOps = InputStreamOps(input)
  implicit def jarFileOps(jarFile:JarFile):JarFileOps = JarFileOps(jarFile)
}
