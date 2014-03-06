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

package com.ee.midas.dsl.grammar;

import com.ee.midas.dsl.grammar.validator.JSONValidator;
import com.ee.midas.dsl.grammar.validator.RegexValidator;
import com.ee.midas.dsl.grammar.validator.Validator;

import java.util.regex.Pattern;

public enum ArgType {
    JSON(new JSONValidator("%s is an invalid json: %s")),
    Identifier(new RegexValidator(Pattern.compile("[a-zA-Z_][a-zA-Z0-9_. ]*",
                              Pattern.UNICODE_CASE),
                              "%s needs to be a valid identifier!")),
    //Letters = \p{L}
    //Numbers = \p{N}
    //Symbols = \p{S}
    //Punctuation = \p{P}
    //Any Whitespace or Invisible character = \p{Z}
    String(new RegexValidator(
            Pattern.compile("[\\p{L}\\p{N}\\p{S}\\p{P}\\p{Z}]*",
            Pattern.UNICODE_CASE),
            "%s needs to be a valid string!"));

    private final Validator validator;

    ArgType(final Validator validator) {
        this.validator = validator;
    }

    public void validate(final String arg) {
        validator.validate(arg);
    }
}
