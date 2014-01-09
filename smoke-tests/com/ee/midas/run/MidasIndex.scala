package com.ee.midas

import org.specs2.Specification

class MidasIndex extends Specification{

  def is = s"""

  Midas is On-the-fly Schema Migration Tool for MongoDB.

To perform schema migration on the fly, it takes schema changes in the form of delta files.

A sample delta file "0001_DB_Collection.delta" would look like:
    "use <database-name>
    |db.<collection-name>.add('{newField : value}')
    |db.<collection-name>.remove('["oldField"]')"

        Note: A delta file must have extension ".delta".

        Midas operates in 2 modes:
        1. "Expansion" mode:
        In this mode midas will apply only those schema changes which will result in expansion of a document.
        E.g.: adding a new field to a collection.
        Expansion operations are: add, split, mergeInto
        2. "Contraction" mode:
        In this mode midas will apply only those schema changes which will result in contraction of a document.
        E.g.: removing an existing field.
        Contraction operations are: remove.


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


    """

}
