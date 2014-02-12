package com.ee.midas.dsl.interpreter

import groovy.util.logging.Slf4j

import java.util.regex.Pattern

@Slf4j
class FileExtension {
    //Expect File URL: ../deltasDir/app/changeSet#/[expansion | contraction]/filename.delta
    private static Pattern changeSetPattern = Pattern.compile('''.*/([\\d]+).*/.*/.*delta$''')

    static Long changeSet(File self) {
        def absPath = self.absolutePath
        def matcher = changeSetPattern.matcher(absPath)
        if(matcher.matches()) {
            def changeSetString = matcher.group(1)
            return changeSetString as Long
        } else {
          -1
        }
    }
}
