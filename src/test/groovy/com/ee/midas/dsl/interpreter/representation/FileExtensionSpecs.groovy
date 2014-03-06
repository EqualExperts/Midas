/******************************************************************************
 * Copyright (c) 2014, Equal Experts Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the Midas Project.
 ******************************************************************************/

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
                   1001       | '/deltas/application/1001Top10Feature/expansions/01-addNameField.delta'
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
