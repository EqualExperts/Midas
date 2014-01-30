import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBObject
import com.mongodb.Mongo
import groovy.transform.Field

@Field
def host = "localhost", port = 27020

@Field
def mongo = new Mongo(host, port)

def generateData(String dbName, String collectionName) {
    DB db = mongo.getDB(dbName)
    DBCollection collection = db.getCollection(collectionName)
    (1..10**6).each {
        def document = new BasicDBObject("entryNo", it.intValue())
        collection.insert(document)
    }
}

generateData(dbName = "users", collectionName = "customers")
//generateData(dbName = "transactions", collectionName = "orders")

def updateAge = {
    document, fieldToUpdate ->
    def value = new Random().nextInt(100)
    document.put(fieldToUpdate, value)
}

viewAndUpdateData("users", "customers", "age", updateAge)
viewAndUpdateData("users", "customers", "customerID", {document, field -> document})


def viewAndUpdateData(String dbName, String collectionName, String fieldToUpdate,
                      Closure updateFunction) {
    DB db = mongo.getDB(dbName)
    DBCollection collection = db.getCollection(collectionName)
    def documents = collection.find()
    while(documents.hasNext()) {
        def document = documents.next()
        displayDocument(document)
        updateFunction(document, fieldToUpdate)
        def findQuery = new BasicDBObject("_id", document.get("_id"))
        def updateQuery = new BasicDBObject()
                                .append("\$set", new BasicDBObject(fieldToUpdate, document.get(fieldToUpdate)))
        collection.update(findQuery, updateQuery)
    }
}

/**
 * deltas:
 * use users
 * db.customers.add("{'age': 18, 'name':'firstName lastName'}")
 * db.customers.copy("entryNo","customerID")
 *
 * use transactions
 * db.orders.copy("entryNo", "orderID")
 * db.orders.transform("orderID", "{$concat: ['OD', '$orderID']}")
 **/
def displayDocument(DBObject dbObject) {
    println(dbObject)
}