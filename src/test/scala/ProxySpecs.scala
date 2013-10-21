/**
 * Created with IntelliJ IDEA.
 * User: komal
 * Date: 21/10/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */

import com.mongodb.MongoClient
import java.io.{InputStream, OutputStream}
import java.net.Socket
import java.util.Scanner
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import com.ee.midas.connector.{Proxy, MongoConnector}

@RunWith(classOf[JUnitRunner])
object ProxySpecs extends Specification{

  "proxy" should {
    val proxy: Proxy = new Proxy(27020, "localhost", 27017)
    var mongoClient:MongoClient = null

    "read data from client" in {
      proxy.start()
      mongoClient = new MongoClient("localhost", 27020)
      println("getting dbs")
      val databases = mongoClient.getDatabaseNames()
      println("show dbs: " + databases)
      val database = mongoClient.getDB("midas")
      println("show collections: " + database.getCollectionNames())
      val collection = database.getCollection("demo1k")
      var cursor = collection.find()
      var count: Int = 0
      while(cursor.hasNext()){
        count = count + 1
        println(cursor.next())
      }
      println("total docs were: " + count)
      mongoClient.close()
      database must_!=null
    }
  }
}
