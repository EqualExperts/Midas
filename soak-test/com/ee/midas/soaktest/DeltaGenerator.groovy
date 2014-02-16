package com.ee.midas.soaktest

import groovy.transform.Field

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

expansions.each {
    deltaName, deltaContent ->
        createDeltaFile(deltasDir, "app", currentChangeSet, "expansion", deltaName, deltaContent)
}

contractions.each {
    deltaName, deltaContent ->
        createDeltaFile(deltasDir, "app", currentChangeSet, "contraction", deltaName, deltaContent)
}

def createDeltaFile(deltadir, appName, changeSet, mode, deltaName, content) {
    def pathSeparator = File.separator
    new File("${deltadir}${pathSeparator}${appName}").mkdir()
    new File("${deltadir}${pathSeparator}${appName}${pathSeparator}${changeSet}").mkdir()
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

println(expansions)
println(contractions)