package com.ee.midas.soaktest

def cli = new CliBuilder(usage: "SwitchMidasMode -f /deltas/midas.config")
cli.with {
    f args:1, argName: 'midas.config file path', longOpt:'file', 'REQUIRED, Path of midas.config', required: true
}

def options = cli.parse(args)

if(options.s) {
    config.source = options.s == true ? 'localhost' : options.s
}

def configFilePath = options.s

File config = new File(configFilePath)

String EXPANSION_MODE = "mode = expansion"
String CONTRACTION_MODE = "mode = contraction"

String configContents = config.text

String newConfigContents = configContents.contains(EXPANSION_MODE) ? configContents.replace(EXPANSION_MODE, CONTRACTION_MODE)
                                            : configContents.replace(CONTRACTION_MODE, EXPANSION_MODE)

config.write(newConfigContents)
