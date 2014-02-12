package com.ee.midas.dsl.interpreter.representation

import com.ee.midas.dsl.interpreter.FileExtension
import spock.lang.Specification

class FileExtensionSpecs extends Specification {

    def "extracts change set from directory name that is always 2 levels up"() {
        when: 'a file within changeset directory that begins with a number'
            def file = new File('/deltas/application/0020ChangeSetDir/expansions/01-addNameField.delta')

        then: 'return change set number from the directory'
            use (FileExtension) {
                file.changeSet() == 20
            }
    }

    def "extracts change set from File path"() {
        expect: 'parsed changeset as expected changeset'
            def file = new File(filePath)
            use (FileExtension) {
                def extractedChangeSet = file.changeSet()
                extractedChangeSet == expectedChangeSet
            }

        where:
            expectedChangeSet | filePath
                     2        | '/deltas/application/0002-CustomerFeature/expansions/01-addNameField.delta'
                     5        | '/deltas/application/5.SomeFeature/contractions/02-removeNameField.delta'
                    55        | '/deltas/application/55-CustomerFeature/expansions/03-addNameField.delta'
                   256        | '/deltas/application/000256-SomeFeature/expansions/01-addNameField.delta'
                   555        | '/deltas/application/555.09-SomeFeature/expansions/01-addNameField.delta'
    }

    def "does not extract change set from File "() {
        when: 'a file without changeset number'
        def file = new File('/deltas/application/NewCustomerFeature/expansions/01-addNameField.delta')

        then: 'return -1 to indicate change set is not present'
        use (FileExtension) {
            file.changeSet() == -1
        }
    }

    def "does not extract change set from File with incorrect format"() {
        when: 'a file without changeset number'
        def file = new File('/expansions/01-addNameField.delta')

        then: 'return -1 to indicate change set is not present'
        use (FileExtension) {
            file.changeSet() == -1
        }
    }
}
