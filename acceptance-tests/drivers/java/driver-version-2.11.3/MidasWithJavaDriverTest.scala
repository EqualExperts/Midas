import com.mongodb._
import org.junit.runner.RunWith
import org.specs2.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MidasWithJavaDriverTest extends Specification {

  var application: MongoClient = null
  var document:DBObject = null

  def is = sequential ^ s2"""
    Narration:
    //TODO: write a story to represent CRUD.
    This is a specification to verify that midas behaves as a proxy

    A client application should
        Step 1: Ensure Midas and mongods are running
            Connect to Midas                 $connect

        Step 2: Perform CRUD operations
            insert documents                 $insert
            read documents                   $read
            update documents                 $update
            delete documents                 $delete
            drop database                    $drop

        Step 3: Close connection to Midas
            Disconnect                       $disconnect
                                                               """

  def connect = {
    application = new MongoClient("localhost", 27020)
    application.getConnector.isOpen
  }

  def insert = {
    document = new BasicDBObject("testName","midas is a proxy")
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def result:WriteResult = collection.insert(document)
    result.getError == null
  }

  def read = {
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def readDocument:DBObject = collection.findOne()
    readDocument == document
  }

  def update = {
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def document = collection.findOne
    document.put("version", 1)
    def result:WriteResult = collection.update(collection.findOne, document)
    result.getError == null
  }

  def delete = {
    def database:DB = application.getDB("midasSmokeTest")
    def collection:DBCollection = database.getCollection("tests")
    def result:WriteResult = collection.remove(document)
    result.getError == null
  }

  def drop = {
    def database:DB = application.getDB("midasSmokeTest")
    database.dropDatabase()
    true
  }


  def disconnect = {
    application.close()
    application.getConnector.isOpen must beFalse
  }
}
