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

package com.ee.midas.run

import org.specs2._
import runner.SpecificationsFinder._
import org.specs2.specification.Fragments

class Index extends Specification { def is = "Midas Index".title ^ s2"""
   Midas is On-the-fly Schema Migration Tool for MongoDB.
    To perform schema migration on the fly, it takes schema changes in the form of delta files.

    A sample delta file "0001_DB_Collection.delta" would look like:
    "use <database-name>
     db.<collection-name>.add('{newField : value}')

    Note: A delta file must have extension ".delta".

    Midas operates in 2 modes:
    1. "Expansion" mode:
        Apply changes to the documents safely that do not break backwards compatibility with
        existing version of the application.
        e.g Adding, copying, merging, splitting fields in a document.
        Expansion operations are: add, split, merge, transform and copy.

    2. "Contraction" mode:
        Clean up any database schema that is not needed after the upgrade.
        E.g.: removing an existing field.
        Contraction operation is: remove.

    Midas can be useful in following scenarios:
    Scenario 1: Migration involving addition of new fields

    Mechanics:
    a.) Express schema changes into delta files.

    b.) use midas in expansion mode.

    2.) Migration involving removal of existing fields.

    Mechanics:
    a.) Express schema changes into delta files

    b.) use midas in contraction mode


    3.) Migration involving both addition of new fields and removal of existing fields.

    Mechanics:
    a.) Express schema changes into delta files

    b.) use midas in expansion mode first until all the documents are migrated, and then.

    c.) use midas in contraction mode.

    ${"" ~ ("Rename Operation", new RenameJourney)}
    ${"" ~ ("Add/Remove Node on the fly", new AddAndRemoveNodeJourney)}
    ${"" ~ ("Add/Remove ChangeSet on the fly", new AddAndRemoveChangeSetJourney)}
    ${"" ~ ("Add/Remove Application on the fly", new AddAndRemoveApplicationJourney)}
 """
}