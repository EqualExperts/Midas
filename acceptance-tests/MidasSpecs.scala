import org.specs2.Specification
import com.mongodb._

// TODO : Write specs for connection failure scenaro

//@RunWith(classOf[JUnitRunner])
class MidasSpecs extends Specification {

    var application: MongoClient = null
    var document:DBObject = null

  def is = s2"""
  Narration:
  This is a specification to verify that midas behaves as a proxy
  Assuming that mongods and midas is running
  A client application should be able to
      Step 1: Connect to midas             $connect

      Step 2: Perform CRUD operations
      insert documents                     $insert
      read documents                       $read
      delete documents                     $delete

      Step 3: Disconnect from midas        $disconnect
                                                              """

  val connect = {
   application = new MongoClient("localhost", 27020)
   if(application.getConnector.isOpen) "success" else "failure"
  }
  val insert = {
    document = new BasicDBObject("testName","midas is a proxy")
    val database:DB = application.getDB("midasSmokeTest")
    val collection:DBCollection = database.getCollection("tests")
    val result:WriteResult = collection.insert(document)
    if(result.getError == null) "success" else "failure"
  }

  val read = {
    val database:DB = application.getDB("midasSmokeTest")
    val collection:DBCollection = database.getCollection("tests")
    val readDocument:DBObject = collection.findOne()
    if(readDocument == document) "success" else "failure"
  }

  def delete = {
    val database:DB = application.getDB("midasSmokeTest")
    val collection:DBCollection = database.getCollection("tests")
    val result:WriteResult = collection.remove(document)
    if(result.getError == null) "success" else "failure"
  }

  def disconnect = {
    application.close()
    if(application.getConnector.isOpen) "failure" else "success"
  }

}
