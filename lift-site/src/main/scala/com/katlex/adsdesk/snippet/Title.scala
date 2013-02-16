package com.katlex.adsdesk
package snippet

import net.liftweb.builtin.snippet.{Menu, Loc}
import xml.{NodeSeq, Text, Elem}

class Title {
  def joined = Loc.dispatch("i") andThen append andThen Menu.dispatch("title")

  lazy val append: PartialFunction[NodeSeq, NodeSeq] = {
    case e:Elem => e.copy(child = Seq(Text(e.child.toString + " :: ")))
  }
}
