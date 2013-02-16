package com.katlex.adsdesk.service.script

import java.util.UUID
import com.google.javascript.jscomp.SourceFile.Builder
import net.liftweb.common.Loggable

object JsCompiler extends Loggable {
  import com.google.javascript.jscomp._

  lazy val options = {
    type OptionModifier = CompilerOptions => Unit
    implicit def compilationLevelAsModifier(cl:CompilationLevel):OptionModifier = cl.setOptionsForCompilationLevel(_)
    implicit def warningLevelAsModifier(wl:WarningLevel):OptionModifier = wl.setOptionsForWarningLevel(_)

    def make(list:OptionModifier*) =
      (new CompilerOptions /: list) {
        case (ops, modifier) =>
          modifier(ops)
          ops
      }

    make(
      CompilationLevel.SIMPLE_OPTIMIZATIONS,
      WarningLevel.QUIET
    )
  }

  def compile(code:String):String = compile((uuid, code) :: Nil)

  def compile(files:List[(String, String)]):String = {
    import scala.collection.JavaConversions._
    val sourceFiles = files.map {
        case (name, content) =>
          builder.buildFromCode(name, content)
      }
    compileWithMethod(_.compile(List.empty[SourceFile], sourceFiles, options))
  }

  private def compileWithMethod[T](method:Compiler => T) = {
    val c = compiler
    method(c)
    c.toSource
  }

  private def uuid = UUID.randomUUID().toString
  private def builder = new Builder
  private def compiler = new Compiler

}