package com.ee.midas.config

import scala.util.parsing.combinator.JavaTokenParsers
import java.net.InetAddress
import com.ee.midas.transform.TransformType

/**
 * BNF
 * --------------------------------------
 * apps ::=  "apps" "{" {app} "}"
 * app  ::=  name "{" mode, nodes "}"
 * mode ::= "mode" "=" "expansion" | "contraction"
 * nodes ::= "{" {node} "}"
 * node ::=  name "{" ip "," changeSet "}"
 * ip   ::=  "ip" "=" ipv4 | ipv6 | ipv4MappedIpv6
 * ipv6 ::= ipv6Full | ipv6Compressed
 * changeSet ::= wholeNumber
 * name ::=  unquotedStringLiteral
 */

trait Parser extends JavaTokenParsers {

  def apps: Parser[List[Application]] = "apps" ~ "{" ~> rep(app) <~ "}"

  def app: Parser[Application] = ident ~ "{" ~ mode ~ "," ~ rep(node) ~ "}" ^^ { case name~"{"~mode~","~nodes~"}" => Application(name, mode, nodes) }

  def node: Parser[Node] = ident ~ "{" ~ ip ~  "," ~ changeSet ~ "}" ^^ { case name~"{"~addr~","~cs~"}" => Node(name, addr, cs) }

  def ip: Parser[InetAddress] = "ip" ~ "=" ~> (ipv4 | ipv6Full) ^^ (InetAddress.getByName(_))

  def ipv4: Parser[String] = """\b([0-1]?\d{1,2}|2[0-4]\d|25[0-5])(\.([0-1]?\d{1,2}|2[0-4]\d|25[0-5])){3}\b""".r

  def ipv6Full: Parser[String] = """^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$""".r

  def changeSet: Parser[ChangeSet] = "changeSet" ~ "=" ~> wholeNumber ^^ (s => ChangeSet(s.toLong))

  def mode: Parser[TransformType] = "mode" ~ "=" ~> ("expansion" | "contraction") ^^ (s => TransformType.valueOf(s.toUpperCase))
}

final case class ChangeSet(number: Long) {
  require(number >= 0L)
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet)
final case class Application(name: String, mode: TransformType, nodes: List[Node])