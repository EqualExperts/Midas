package com.ee.midas.config

import scala.util.parsing.combinator.JavaTokenParsers
import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType
import scala.util.Try

/**
 * Example:  app1.midas
 *
 * app1 {
 *   mode = expansion
 *   siteANode1 {
 *     ip = x.x.x.x
 *     changeSet = 1
 *   }
 *   siteANode2 {
 *     ip = y.y.y.y
 *     changeSet = 1
 *   }
 *   siteBNode1 {
 *     ip = z.z.z.z
 *     changeSet = 1
 *   }
 *   siteBNode2 {
 *     ip = u.u.u.u
 *     changeSet = 1
 *   }
 * }
 *
 * BNF for Application
 * --------------------------------------
 * app  ::=  name "{" mode nodes "}"
 * mode ::= "mode" "=" "expansion" | "contraction"
 * nodes ::= "{" node {node} "}"
 * node ::=  name "{" ip changeSet "}"
 * ip   ::=  "ip" "=" ipv4 | ipv6 | ipv4MappedIpv6
 * ipv6 ::= ipv6Full | ipv6Compressed
 * changeSet ::= wholeNumber
 * name ::=  ident
 *
 */

trait ApplicationParsers extends JavaTokenParsers {

  //Eat Java-Style comments like whitespace
  protected override val whiteSpace = """(\s|//.*|(?m)/\*(\*(?!/)|[^*])*\*/)+""".r

  def app: Parser[Application] = ident ~ "{" ~ mode ~  rep1(node) ~ "}" ^^ { case name~"{"~mode~nodes~"}" => Application(name, mode, nodes) }

  def node: Parser[Node] = ident ~ "{" ~ ip ~ changeSet ~ "}" ^^ { case name~"{"~addr~cs~"}" => Node(name, addr, cs) }

  def ip: Parser[InetAddress] = "ip" ~ "=" ~> (ipv4 | ipv6Full) ^^ (InetAddress.getByName(_))

  def ipv4: Parser[String] = """\b([0-1]?\d{1,2}|2[0-4]\d|25[0-5])(\.([0-1]?\d{1,2}|2[0-4]\d|25[0-5])){3}\b""".r

  def ipv6Full: Parser[String] = """^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$""".r

  def changeSet: Parser[ChangeSet] = "changeSet" ~ "=" ~> wholeNumber ^^ (s => ChangeSet(s.toLong))

  def mode: Parser[TransformType] = "mode" ~ "=" ~> ("expansion" | "contraction") ^^ (s => TransformType.valueOf(s.toUpperCase))

  def parse(input: String): Application = parseAll(app, input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  def parse(appConfig: URL): Try[Application] = Try {
    val configText = scala.io.Source.fromURL(appConfig).mkString
    parse(configText)
  }
}

