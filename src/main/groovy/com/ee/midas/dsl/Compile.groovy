import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.generator.ScalaGenerator
import com.ee.midas.dsl.interpreter.Reader

def sortedDeltaFiles(File deltasDir) {
    deltasDir.listFiles(new FilenameFilter() {
        @Override
        boolean accept(File dir, String name) {
            name.endsWith '.delta'
        }
    }).sort().toList()
}

def translate(deltaFiles) {
    def generator = new ScalaGenerator()
    def reader = new Reader()
    def translator = new Translator(reader, generator)
    translator.translate(deltaFiles)
}

// ---- Script starts here -----
def scalaTemplateURI = 'templates/Transformations.scala.template'
ClassLoader loader = Thread.currentThread().getContextClassLoader()
def scalaTemplateURL = loader.getResource(scalaTemplateURI)
println ("Template URL = $scalaTemplateURL")
if(args.length < 1) {
    println "Usage: Compile <deltas-dir> [scala-template-url=$scalaTemplateURL] <output-uri>"
    return
}

def deltasDir = new File(args[0])
if(!deltasDir.isDirectory()) {
    println("Expected Directory, Given File: ${args[0]}")
    return
}


if(args.length == 2) {
  scalaTemplateURL = args[1]
}


def scalaFileTemplate = new File(scalaTemplateURL.toURI())
def scalaTemplateContents = scalaFileTemplate.text
def translations = translate(sortedDeltaFiles(deltasDir))

def scalaFileContents = scalaTemplateContents.replaceAll('###EXPANSIONS-CONTRACTIONS###', translations)
println("SCALA FILE CONTENTS = \n$scalaFileContents")
def outputURI = 'generated/scala'
def outputURL = loader.getResource(outputURI)
println("Writing generated output to $outputURL")
def scalaFile = new File(outputURL.getPath() + '/Transformations.scala')
scalaFile.withWriter { writer ->
    writer << scalaFileContents
}
println("Completed writing generated output to $scalaFile.name")

