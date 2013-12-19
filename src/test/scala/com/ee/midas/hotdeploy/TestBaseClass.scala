package com.ee.midas.hotdeploy

/**
 * Base class needed by deployer while specifying the Type.
 */
class TestBaseClass extends Deployable[TestBaseClass]{

  var state = "default_state"

  def isInstanceCreated = false

  def injectState(fromT: TestBaseClass): Unit = {
    this.state = fromT.state
  }
}
