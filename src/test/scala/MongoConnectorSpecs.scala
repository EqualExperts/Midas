import com.ee.midas.connector.{Proxy, MongoConnector}
import java.io.IOException
import java.net.{UnknownHostException, Socket}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
object MongoConnectorSpecs extends Specification{

    "mongo connector" should {

      "connect to mongo server" in {
        val mongoConnector = new MongoConnector("localhost", 27017)
        val mongoSocket = mongoConnector.connect()
        mongoSocket.isConnected() must beTrue
      }
      "throw exception when incorrect port is given" in {
        val mongoConnector = new MongoConnector("localhost", 1000)
        mongoConnector.connect() must throwAn[IOException]
      }
      "throw exception when unknown host is given" in {
        val mongoConnector = new MongoConnector("some host", 1000)
        mongoConnector.connect() must throwAn[UnknownHostException]
      }
    }

     "proxy" should {
       new Proxy(27020,  "localhost", 27017).start()
       "accept connection" in {
         val clientSocket = new Socket("localhost", 27020)
         clientSocket.isConnected must beTrue
       }
     }
}
