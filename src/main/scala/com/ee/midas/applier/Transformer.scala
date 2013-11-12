package com.ee.midas.applier

import org.bson.BSONObject


class Transformer {

  def transform(dbObject : BSONObject ) : BSONObject = {
    dbObject.put("version", "1.0");
    dbObject;
  }

  def transform(documents : List[BSONObject]) : List[BSONObject] = {

    for ( document <- documents) {
       transform(document);
    }
    documents
  }

}
