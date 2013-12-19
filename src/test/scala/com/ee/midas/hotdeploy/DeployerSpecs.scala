package com.ee.midas.hotdeploy

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import com.ee.midas.utils.ScalaCompiler
import com.ee.midas.{Main}
import java.io.{FileWriter, File}
import com.ee.midas.transform.Transforms

@RunWith(classOf[JUnitRunner])
object DeployerSpecs extends Specification with Deployer with ScalaCompiler {

  sequential

  val loader = Main.getClass.getClassLoader

  val srcScalaDirURI = "generated/scala/"
  val srcScalaFilename = "TestDeployer.scala"
  val binDirURI = "generated/scala/bin/"
  val classpathURI = "."

  val classpathDir = loader.getResource(classpathURI)
  val binDir = loader.getResource(binDirURI)
  val srcScalaDir = loader.getResource(srcScalaDirURI)
  val srcScalaFile = new File(srcScalaDir.getPath + srcScalaFilename)

  "Deployer" should {
    "create instance of the compiled classes" in {
      //given: a source scala compiled file
      if(srcScalaFile.exists()) srcScalaFile.delete()
      srcScalaFile.createNewFile()
      val writer = new FileWriter(srcScalaFile)
      writer.write(s"""package com.ee.midas.hotdeploy
        class TestDeployerClass extends TestBaseClass {
        override def isInstanceCreated = true
        }""")
      writer.close()
      val clazzName = "com.ee.midas.hotdeploy.TestDeployerClass"
      compile(classpathDir.getPath, binDir.getPath, srcScalaFile.getPath)

      //when
      val instance = deploy[TestBaseClass](getClass.getClassLoader, Array(binDir), clazzName)

      //then
      instance.getClass.getSimpleName mustEqual "TestDeployerClass"
      instance.isInstanceCreated must beTrue
    }

    "throw exception if a class is not found" in {
      //given: a random class name
      val clazzName = "com.ee.midas.hotdeploy.DeployerExceptionClass"

      //when
      deploy[TestBaseClass](getClass.getClassLoader, Array(binDir), clazzName) must throwA[RuntimeException]

    }
  }
}
