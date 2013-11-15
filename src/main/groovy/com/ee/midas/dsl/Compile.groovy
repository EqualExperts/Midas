import com.ee.midas.dsl.Translator
import com.ee.midas.dsl.interpreter.Reader
import com.ee.midas.dsl.generator.ScalaGenerator

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
if(!args) {
    println "Usage: Compile <deltas directory>"
    return
}

def deltasDir = new File(args[0])
if(!deltasDir.isDirectory()) {
    println("Expected Directory, Given File: ${args[0]}")
    return
}

println translate(sortedDeltaFiles(deltasDir))
