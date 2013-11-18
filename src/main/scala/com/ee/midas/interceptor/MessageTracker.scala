package com.ee.midas.interceptor


class MessageTracker {
  val requestInterceptable = new RequestInterceptor(this)
  val responseInterceptable = new ResponseInterceptor(this)
  val ids = scala.collection.mutable.Map[Int, String]()

  def track(id: Int, fullCollectionName: String) = ids.put(id, fullCollectionName)

  def fullCollectionNameFor(id: Int) = ids.get(id)

  def untrack(id: Int) = ids.remove(id)
}
