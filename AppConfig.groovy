/*******************************************************************************
 * Copyright (c) 2013, Equal Experts Ltd
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
 * official policies, either expressed or implied, of the Tayra Project.
 ******************************************************************************/
def toCompleteVersion() {
	def completeVersion = new StringBuilder()
	completeVersion << configuration.product.version.major
	completeVersion << '.'
	completeVersion << configuration.product.version.minor
	completeVersion << '.'
	completeVersion << configuration.product.version.micro
	completeVersion << '.'
	completeVersion << configuration.product.version.qualifier.alphaNumeric
	completeVersion.toString()
}

configuration {
	product {
		name = 'Midas'

		vendor {
			name = 'Equal Experts Labs'
		}
        //JBoss Versioning Convention
		version {
			major = 1 //number related to production release
			minor = 0 //changes or feature additions
			micro = 0 //patches and bug fixes
			qualifier {
				alphaNumeric = 'Alpha1' //Alpha# or Beta# or CR# or GA, or SP#
			}
            previous = '0.0.0.Alpha0'
			complete = toCompleteVersion()
		}

		distribution {
			name = product.name + '-' + toCompleteVersion()

            previousArchiveName = product.name + '-' + product.version.previous

			jar {
				name = product.name
				manifest {
					details = [
						'Manifest-Version' : '1.0',
						'Sealed' : 'true',
						'Specification-Title' : product.name,
						'Specification-Version': toCompleteVersion(),
						'Specification-Vendor':  product.vendor.name,
						'Implementation-Version': toCompleteVersion(),
						'Implementation-Vendor': product.vendor.name
					]
				}
			}
		}
	}
}
