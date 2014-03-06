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

package com.ee.midas.pipes

import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import java.io.{OutputStream, InputStream, ByteArrayOutputStream, ByteArrayInputStream}
import org.mockito.runners.MockitoJUnitRunner
import org.specs2.matcher.JUnitMustMatchers
import org.junit.Test

@RunWith(classOf[MockitoJUnitRunner])
class SimplexPipeSpecs extends JUnitMustMatchers with Mockito {

  val pipeName = "test-pipe"

  @Test
  def transferDataFromSourceToDestination() {
    //given
    val data = "Hello World".getBytes()
    val source = new ByteArrayInputStream(data)
    val destination = new ByteArrayOutputStream()
    val simplexPipe = new SimplexPipe(pipeName, source, destination)

    //when
    simplexPipe.run()

    source.close()
    destination.close()

    //then
    destination.toByteArray() must beEqualTo(data)
  }

  @Test
  def closeOnForceStop() {
    //given
    val mockInputStream = mock[InputStream]
    val mockOutputStream = mock[OutputStream]
    val pipe = new SimplexPipe(pipeName, mockInputStream, mockOutputStream)

    //when
    pipe.forceStop

    //then
    there was one(mockInputStream).close()
    there was one(mockOutputStream).close()
    pipe.isActive must beFalse
  }

  @Test
  def stopGracefully() {
    //given
    val mockInputStream = mock[InputStream]
    val mockOutputStream = mock[OutputStream]
    val pipe = new SimplexPipe(pipeName, mockInputStream, mockOutputStream)
    scheduleStopToRunAfter(pipe, 550)

    //when
    pipe.run

    //then
    pipe.isActive must beFalse
  }

  def scheduleStopToRunAfter(pipe: SimplexPipe, time: Int):Unit = {
    new Thread  {
      Thread.sleep(time)
      pipe.stop
    }
  }
}
