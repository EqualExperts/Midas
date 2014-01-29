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
    a.) map schema changes into delta files.

    b.) use midas in expansion mode.

    2.) Migration involving removal of existing fields.

    Mechanics:
    a.) map schema changes into delta files

    b.) use midas in contraction mode


    3.) Migration involving both addition of new fields and removal of existing fields.

    Mechanics:
    a.) map schema changes into delta files

    b.) use midas in expansion mode first until all the documents are migrated, and then.

    c.) use midas in contraction mode.

    ${"" ~ ("Rename Operation", new RenameSpecs)}
 """
}