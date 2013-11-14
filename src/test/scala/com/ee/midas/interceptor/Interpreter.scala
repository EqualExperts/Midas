package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.{BasicBSONObject, BSONObject, BasicBSONEncoder, BSONEncoder}
import com.mongodb.{DBObject, DBCollection, DefaultDBDecoder, DBDecoder}
import java.io.{InputStream, ByteArrayInputStream}
import org.specs2.specification.Scope
import com.ee.midas.interceptor.Interpreter

@RunWith(classOf[JUnitRunner])
class InterpreterSpecs extends Specification {

    "Interpreter" should {

        "Encode and Decode documents" in new setup {
          var documents:List[BSONObject] = List[BSONObject]()
          documents ::= new BasicBSONObject("name", "midas-spike")
          documents ::= new BasicBSONObject("name", "midas-interceptor")
          documents ::= new BasicBSONObject("name", "midas")

          val encodedDocuments : InputStream = new ByteArrayInputStream(interpret.asBytes(documents))
          val decodedDocuments : List[BSONObject] = interpret.documentsFrom(encodedDocuments,documents.length)

          documents mustEqual decodedDocuments
        }
    }
}

trait setup extends Scope {
  val encoder : BSONEncoder = new BasicBSONEncoder()
  val decoder : DBDecoder = new DefaultDBDecoder()
  val interpret : Interpreter =  new Interpreter
}
