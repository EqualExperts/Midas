package com.ee.midas.inject

import java.nio.file.{WatchEvent, FileSystems}
import java.nio.file.StandardWatchEventKinds._
import scala.collection.JavaConverters._

class DirectoryWatcher(dirURL: String) {
  private val fileSystem = FileSystems.getDefault
  private val watcher = fileSystem.newWatchService()
  private val os = System.getProperty("os.name")

  println(s"Dir to Watch = $dirURL, OS = ${os}")
  //Compensate for the bug that causes fileSystem.getPath to crash in Windows for dirURL
  private val path = if (os.contains("Win"))
                        fileSystem.getPath(dirURL.substring(1))
                     else
                        fileSystem.getPath(dirURL)

  path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
  println(s"Will Watch dir ${dirURL} for Creation, Modification and Deletion of Files...")
  
  var isRunning = true
  
   def stop = {
     println(s"Stopping Watch on ${dirURL}")
     isRunning = false
     watcher.close()
   }
  
   def start(callback: WatchEvent[_] => Unit): Unit = {
     var valid = true
     while(isRunning && valid) {
       try {
         println(s"Watching ${dirURL}...")
         val watchKey = watcher.take()
         val events = watchKey.pollEvents().asScala
         events.foreach { e =>
           println(s"Detected ${e.kind()}, Context = ${e.context()}}")
           callback(e)
         }
         valid = watchKey.reset()
       } catch {
         case e: Exception => watcher.close()
       }
     }
     println(s"Completed Watch on ${dirURL}")
   }
}
