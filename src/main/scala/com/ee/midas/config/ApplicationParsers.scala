/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

package com.ee.midas.config

import scala.util.parsing.combinator.JavaTokenParsers
import java.net.{URL, InetAddress}
import com.ee.midas.transform.TransformType
import scala.util.Try
import java.io.File

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

  def app(configDir: URL): Parser[Application] = ident ~ "{" ~ mode ~  rep1(node) ~ "}" ^^ { case name~"{"~mode~nodes~"}" => new Application(configDir, name, mode, nodes.toSet) }

  def node: Parser[Node] = ident ~ "{" ~ ip ~ changeSet ~ "}" ^^ { case name~"{"~addr~cs~"}" => new Node(name, addr, cs) }

  def ip: Parser[InetAddress] = "ip" ~ "=" ~> (ipv4 | ipv6Full) ^^ (InetAddress.getByName(_))

  def ipv4: Parser[String] = """\b([0-1]?\d{1,2}|2[0-4]\d|25[0-5])(\.([0-1]?\d{1,2}|2[0-4]\d|25[0-5])){3}\b""".r

  def ipv6Full: Parser[String] = """^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$""".r

  def changeSet: Parser[ChangeSet] = "changeSet" ~ "=" ~> wholeNumber ^^ (s => ChangeSet(s.toLong))

  def mode: Parser[TransformType] = "mode" ~ "=" ~> ("expansion" | "contraction") ^^ (s => TransformType.valueOf(s.toUpperCase))

  def parse(input: String, configDir: URL): Application = parseAll(app(configDir), input) match {
    case Success(value, _) => value
    case NoSuccess(message, _) =>
      throw new IllegalArgumentException(s"Parsing Failed: $message")
  }

  final val appConfigFileExtn = ".midas"

  def parse(configDir: URL): Try[Application] = Try {
    val dir = new File(configDir.getFile)
    val configFilename = s"${dir.getName}$appConfigFileExtn"
    val configFile: URL = new URL(s"${configDir}${File.separator}${configFilename}")
    val configText = scala.io.Source.fromURL(configFile).mkString
    parse(configText, configDir)
  }
}

