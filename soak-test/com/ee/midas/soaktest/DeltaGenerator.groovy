package com.ee.midas.soaktest

def cli = new CliBuilder(usage: "Client --version=<v1>")
cli.with {
    _  args:1, argName: 'version', longOpt:'version', 'REQUIRED, client App version to run', required: true
}

def options = cli.parse(args)

def clientVersion = options.version

def configURL = new File("Config.groovy").toURI().toURL()
def config = new ConfigSlurper().parse(configURL)

def currentChangeSet = config.data.app."$clientVersion".changeSet
def deltasDir = config.data.app."$clientVersion".deltas.baseDir
def expansions = config.data.app."$clientVersion".deltas.expansions
def contractions = config.data.app."$clientVersion".deltas.contractions
def appName = "app"

def pathSeparator = File.separator
new File("${deltasDir}${pathSeparator}${appName}").mkdir()
new File("${deltasDir}${pathSeparator}${appName}${pathSeparator}${currentChangeSet}").mkdir()

expansions.each {
    deltaName, deltaContent ->
        createDeltaFile(deltasDir, appName, currentChangeSet, "expansion", deltaName, deltaContent)
}

contractions.each {
    deltaName, deltaContent ->
        createDeltaFile(deltasDir, appName, currentChangeSet, "contraction", deltaName, deltaContent)
}

def createDeltaFile(deltadir, appName, changeSet, mode, deltaName, content) {
    def pathSeparator = File.separator
    new File("${deltadir}${pathSeparator}${appName}${pathSeparator}${changeSet}${pathSeparator}${mode}").mkdir()
    def absoluteDeltaFileName = "${deltadir}${pathSeparator}${appName}${pathSeparator}${changeSet}${pathSeparator}${mode}${pathSeparator}${deltaName}"
    println("creating delta: ${absoluteDeltaFileName}")
    def deltaFile = new File(absoluteDeltaFileName)
    if(deltaFile.exists()){
        println("The delta $absoluteDeltaFileName already exists!")
    } else {
        deltaFile.createNewFile()
        deltaFile.withWriter { out ->
            content.eachLine {
                out.println(it.stripIndent())
            }
        }
    }
}

def appConfigText = """
                        |app_version2 {
                        |mode = expansion
                        |  nodeA {
                        |    ip = 127.0.0.1
                        |    changeSet = 2
                        |  }
                        |}
                        """.stripMargin()
def appConfigFile = new File("midas/deltas/app/app.midas")
appConfigFile.createNewFile()
appConfigFile.withWriter { it <<
	appConfigText
}

println(expansions)
println(contractions)
