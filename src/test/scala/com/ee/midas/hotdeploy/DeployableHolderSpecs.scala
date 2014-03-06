/******************************************************************************
* Copyright (c) 2014, Equal Experts Ltd
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are
* met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
* TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
* PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
* OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation
* are those of the authors and should not be interpreted as representing
* official policies, either expressed or implied, of the Midas Project.
******************************************************************************/

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
