import com.ee.midas.dsl.Translator

def sortedDeltaFiles(File deltasDir) {
    deltasDir.listFiles(new FilenameFilter() {
        @Override
        boolean accept(File dir, String name) {
            name.endsWith '.delta'
        }
    }).sort().toList()
}

def translate(deltaFiles) {
    def translator = new Translator()
    translator.translate(deltaFiles)
}

// ---- Script starts here -----
if(!args) {
    println "Usage: Translate <deltas directory>"
    return
}

def deltasDir = new File(args[0])
if(!deltasDir.isDirectory()) {
    println("Expected Directory, Given File: ${args[0]}")
    return
}

println translate(sortedDeltaFiles(deltasDir))
