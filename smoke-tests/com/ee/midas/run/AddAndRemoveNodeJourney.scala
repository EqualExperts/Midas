package com.ee.midas.run


import org.specs2._
import specification._

class AddAndRemoveNodeJourney extends Specification with Forms {

    sequential
    def is = s2"""
      ${"Add/Remove Node On the fly".title}
      Narration: IncyWincyShoppingApp stores its persistent data on MongoDB. It has 2 nodes in the
                 cluster which are at the new version and are connected to Midas. Schema migration is
                 in process, but they want to take down one node for maintenance for sometime. So,
                 Bob, the business analyst approaches Dave, the developer.

      Bob:  "Hey Dave, I know schema migration is in process but we need to take down NodeX for maintenance
             for sometime."
      Dave: "Ok Bob, thats not a problem. We just need to remove that node from application's config file.
             After that no requests from that node will be accepted. Once the node is up, we can again add
             it to the config and the requests from the node will be entertained like they were before."
      Bob:  "That will be great."
                                                        """

}
