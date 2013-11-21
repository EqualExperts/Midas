package com.ee.midas.interceptor

class MessageTracker {
  val ids = scala.collection.mutable.Map[Int, String]()

  def track(id: Int, fullCollectionName: String) = ids.put(id, fullCollectionName)

  def fullCollectionName(id: Int) = ids.get(id)

  def untrack(id: Int) = ids.remove(id)
}
