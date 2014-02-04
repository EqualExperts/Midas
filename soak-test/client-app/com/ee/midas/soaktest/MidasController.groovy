package com.ee.midas.soaktest

def configURL = new File("Config.groovy").toURI().toURL()
def config = new ConfigSlurper().parse(configURL)

def midasDir = config.soakdata.midasDir
def deltasDir = config.soakdata.deltas.baseDir

def command = "cmd /c $midasDir\\midas.bat --deltasDir $deltasDir"

def process = command.execute(null, new File(midasDir))
def errStream = process.errorStream
def inStream = process.inputStream

println("error stream: ${errStream.text}")
println("input stream: ${inStream.text}")

println(command)