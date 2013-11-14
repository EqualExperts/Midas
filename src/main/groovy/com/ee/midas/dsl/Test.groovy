import com.ee.midas.dsl.Translator

def deltaFiles(File deltasDir) {
    deltasDir.listFiles(new FilenameFilter() {
        @Override
        boolean accept(File dir, String name) {
            name.endsWith '.delta'
        }
    }).sort()
}

// ---- Script starts here -----
if(!args) {
    println "Usage: Compile <deltas directory>"
    return
}

def deltasDirname = new File(args[0])
if(!deltasDirname.isDirectory()) {
    println("Expected Directory, given Filename ${args[0]}")
    return
}

def sortedDeltaFiles = deltaFiles(deltasDirname)
def translator = new Translator()
println translator.translate(sortedDeltaFiles.toList())
