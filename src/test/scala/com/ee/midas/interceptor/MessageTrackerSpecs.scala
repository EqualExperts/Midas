package com.ee.midas.interceptor

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MessageTrackerSpecs extends Specification {

  "Message Tracker" should {
     "Track Id with Collection Name" in {
       //given
       val tracker = new MessageTracker()
       val ids = tracker.ids

       //when
       tracker.track(id = 1 , fullCollectionName = "midas")

       //then
       ids.get(key = 1) mustEqual Some("midas")
     }

    "Give Full Collection Name For Given ID" in {
      //given
      val tracker = new MessageTracker()

      //when
      tracker.track(id = 1 , fullCollectionName = "midas")

      //then
      tracker.fullCollectionName(id = 1) mustEqual Some("midas")
    }

    "Untrack a given ID" in {
      //given
      val tracker = new MessageTracker()
      val ids = tracker.ids
      tracker.track(id = 1 , fullCollectionName = "midas")

      ids.get(key = 1) mustEqual Some("midas")

      //when
      tracker.untrack(id = 1)

      //then
      ids.get(key = 1) mustEqual None
    }
  }

}
