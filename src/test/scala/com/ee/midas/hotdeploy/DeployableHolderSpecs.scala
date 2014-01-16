package com.ee.midas.hotdeploy

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DeployableHolderSpecs extends Specification with Mockito {
  "Deployable Holder" should {
    "Inject new state in existing deployable" in {

      //given: an old deployable, a new deployable, and a deployable holder
      val oldDeployable = new TestBaseClass()
      val newDeployableMock = mock[TestBaseClass]
      newDeployableMock.state returns "new_state"

      val testHolder = new DeployableHolder[TestBaseClass] {
        def createDeployable: TestBaseClass = oldDeployable
      }

      //when: new deployable is set in the holder
      testHolder.set(newDeployableMock)

      //then: the new state is reflected in the old deployer
      oldDeployable.state mustEqual "new_state"
    }

    "return the deployable instance" in {

      //given: a deployable intance and a deployable holder
      val deployableInstance = new TestBaseClass()

      //when: new deployableHolder is created
      val testHolder = new DeployableHolder[TestBaseClass] {
        def createDeployable: TestBaseClass = deployableInstance
      }

      //then: new deployable instance is returned
      testHolder.get mustEqual deployableInstance
    }

    "stringify the deployable when requested" in {

      //given: a deployable intance and a deployable holder
      val deployableInstance = new TestBaseClass()

      val testHolder = new DeployableHolder[TestBaseClass] {
        def createDeployable: TestBaseClass = deployableInstance
      }

      //when: deployable holder is stringified
      val deployableHolderString = testHolder.toString

      //then: deployable instance is also stringified
      deployableHolderString contains deployableInstance.toString
    }
  }
}
