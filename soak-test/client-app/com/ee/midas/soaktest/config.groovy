package com.ee.midas.soaktest

soakdata {

    host = 'localhost'
    port = 27020
    databases {
        users {
            customers {
                fields {
                    entryNo = Integer
                    fName = String
                    lName = String
                }
                numOfDocuments = 10
            }
            projects {
                fields {
                    projectId = Integer
                }
                numOfDocuments = 02
            }
        }

        transactions {
            orders {
                fields {
                    entryNo = String
                }
                numOfDocuments = 10
            }
        }
    }

    deltas {

        baseDir = "deltas"
        expansions = [
            '0001_users_customers.delta' :      """
                                                use users
                                                db.customers.add('{"age": 0}')
                                                db.customers.copy("entryNo", "customerID")
                                                db.customer.mergeInto("fullName", " ", "['fName', 'lName']")
                                                """,

            '0002_transactions_orders.delta' :  """
                                                use transactions
                                                db.orders.copy("entryNo", "orderID")
                                                db.orders.transform("orderID", "{\$concat: ['OD', '\$orderID']}")
                                                """
        ]

        contractions = [
            '0001_usersAndTransactions.delta' : """
                                                use users
                                                db.customers.remove('["entryNo"]')
                                                use transactions
                                                db.orders.remove('["entryNo"]')
                                                """

        ]
    }

}