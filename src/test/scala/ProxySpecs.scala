/**
 * Created with IntelliJ IDEA.
 * User: komal
 * Date: 21/10/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.OutputStream
import java.net.Socket
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import com.ee.midas.connector.{Proxy, MongoConnector}

@RunWith(classOf[JUnitRunner])
object ProxySpecs extends Specification{

  "proxy" should {
    val proxy: Proxy = new Proxy(27020, "localhost", 27017)
    var clientSocket:Socket = null

    "read data from client" in {
      proxy.start()
      clientSocket = new Socket("localhost", 27020)
      val clientOutputStream:OutputStream = clientSocket.getOutputStream();
      clientOutputStream.write("Hello world".getBytes())
      clientSocket.isConnected must beTrue
    }
  }
}
