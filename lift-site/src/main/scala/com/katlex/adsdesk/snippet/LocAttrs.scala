package com.katlex.adsdesk.snippet

import xml.{Text, Attribute, Elem, NodeSeq}
import net.liftweb.http.S

object LocAttrs {
  def render(ns:NodeSeq) = ns.collect {
    case e:Elem => e.attributes.map {
      case Attribute(name, value, next) =>
        val original = value.toString()
        val translated = S ? original
        if (translated != original)
          Attribute(name, Text(translated), next)
        else
          Attribute(name, value, next)
      case a => a
    } .foldLeft(e) { _ % _ }
  }
}
