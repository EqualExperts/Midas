###############################################################################
 # Copyright (c) 2014, Equal Experts Ltd
 # All rights reserved.
 #
 # Redistribution and use in source and binary forms, with or without
 # modification, are permitted provided that the following conditions are
 # met:
 #
 # 1. Redistributions of source code must retain the above copyright notice,
 #    this list of conditions and the following disclaimer.
 # 2. Redistributions in binary form must reproduce the above copyright
 #    notice, this list of conditions and the following disclaimer in the
 #    documentation and/or other materials provided with the distribution.
 #
 # THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 # "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 # TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 # PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 # OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 # EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 # PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 # PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 # LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 # NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 # SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 #
 # The views and conclusions contained in the software and documentation
 # are those of the authors and should not be interpreted as representing
 # official policies, either expressed or implied, of the Midas Project.
 ##############################################################################

import unittest
import pymongo
import bson
from bson import objectid
from pymongo import MongoClient
import xmlrunner
import sys

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

    def tearDown(self):
        self.client.close()

MidasWithPythonDriverSuite = unittest.TestSuite()
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("connect"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("insert"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("read"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("update"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("delete"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("drop"))
MidasWithPythonDriverSuite.addTest(MidasWithPythonDriverTest("disconnect"))

reportDir = sys.argv[1]
runner = xmlrunner.XMLTestRunner(output = reportDir)
runner.run(MidasWithPythonDriverSuite)