package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MessageTrackerSpecs extends Specification {

  "Message Tracker" should {
     "Track Id with Collection Name" in {
         val tracker = new MessageTracker()
         val ids = tracker.ids

         tracker.track(id = 1 , fullCollectionName = "midas")

         ids.get(key = 1) mustEqual Some("midas")
     }

    "Give Full Collection Name For Given ID" in {
      val tracker = new MessageTracker()

      tracker.track(id = 1 , fullCollectionName = "midas")

      tracker.fullCollectionNameFor(id = 1) mustEqual Some("midas")
    }

    "Untrack a given ID" in {
      val tracker = new MessageTracker()
      val ids = tracker.ids
      tracker.track(id = 1 , fullCollectionName = "midas")

      ids.get(key = 1) mustEqual Some("midas")

      tracker.untrack(id = 1)

      ids.get(key = 1) mustEqual None
    }
  }

}
