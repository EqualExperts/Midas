import unittest
import pymongo
import bson
from bson import objectid
from pymongo import MongoClient

class MidasWithPythonDriverTest(unittest.TestCase):
    def setUp(self):
        self.client = MongoClient('localhost', 27020)
        self.document = {"_id":objectid.ObjectId() ,"name":"pythonTest"}

    def connect(self):
        assert self.client.alive() , 'Mongo Client is not connected'

    def insert(self):
        db = self.client['testDatabase']
        collection = db['testCollection']
        id = collection.insert(self.document)
        assert id != None , 'Document is not inserted'

    def read(self):
        db = self.client['testDatabase']
        collection = db['testCollection']
        readDocument = collection.find_one()
        assert readDocument != None , 'Cannot read document'

    def update(self):
        db = self.client['testDatabase']
        collection = db['testCollection']
        result = collection.update({"name":"pythonTest"}, {"name":"test1"})
        assert result['err'] != 'None' , 'Update failed'

    def delete(self):
        db = self.client['testDatabase']
        collection = db['testCollection']
        result = collection.remove({"name":"test1"})
        assert result['err'] != 'None' , 'Deletion of document failed'

    def drop(self):
        result = self.client.drop_database('testDatabase')
        assert result != 'None' , 'Cannot drop database'

    def disconnect(self):
        result = self.client.disconnect()
        assert result != 'None' , 'Cannot disconnect from mongo'



MidasWithPythonDriverSuite = unittest.TestSuite()
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("connect"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("insert"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("read"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("update"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("delete"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("drop"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("disconnect"))

runner = unittest.TextTestRunner()
runner.run(MidasWithPythonDriverSuite)