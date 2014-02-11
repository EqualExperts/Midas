package com.ee.midas.config

import scala.util.parsing.combinator.JavaTokenParsers
import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType
import com.ee.midas.utils.Loggable

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

trait Parser extends JavaTokenParsers with Loggable {

  //Eat Java-Style comments like whitespace
  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r

  def configuration: Parser[Configuration] = "apps" ~ "{" ~> rep(app) <~ "}"  ^^ (new Configuration(_))

  def app: Parser[Application] = ident ~ "{" ~ mode ~ "," ~ rep(node) ~ "}" ^^ { case name~"{"~mode~","~nodes~"}" => Application(name, mode, nodes) }

  def node: Parser[Node] = ident ~ "{" ~ ip ~  "," ~ changeSet ~ "}" ^^ { case name~"{"~addr~","~cs~"}" => Node(name, addr, cs) }

  def ip: Parser[InetAddress] = "ip" ~ "=" ~> (ipv4 | ipv6Full) ^^ (InetAddress.getByName(_))

  def ipv4: Parser[String] = """\b([0-1]?\d{1,2}|2[0-4]\d|25[0-5])(\.([0-1]?\d{1,2}|2[0-4]\d|25[0-5])){3}\b""".r

  def ipv6Full: Parser[String] = """^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$""".r

  def changeSet: Parser[ChangeSet] = "changeSet" ~ "=" ~> wholeNumber ^^ (s => ChangeSet(s.toLong))

  def mode: Parser[TransformType] = "mode" ~ "=" ~> ("expansion" | "contraction") ^^ (s => TransformType.valueOf(s.toUpperCase))

  def parse(input: String): TransformType = parseAll(mode, input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  def parse(url: URL): TransformType = {
    logInfo(s"Reading Configuration File from $url")
    val config: String = scala.io.Source.fromURL(url).mkString
    logInfo(s"Read Configuration $config")
    parse(config)
  }
}

final case class ChangeSet(number: Long) {
  require(number >= 0L)
}

final case class Node(name: String, ip: InetAddress, changeSet: ChangeSet)

final case class Application(name: String, mode: TransformType, nodes: List[Node])

final case class Configuration (applications: List[Application])
