package com.ee.midas.soaktest

import groovy.transform.Field

@Field
Random random = new Random()

def randomString(int size) {
    String alphabet = (('A'..'Z')+('a'..'z')).join()
    random.with {
        (1..size).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
    }
}

def randomNumber(int max = 9999) {
    random.nextInt(max)
}


data {
    pushFrequency {
        insert {
            interval = 2000
            batchSize = 100
        }

        update {
            interval = 3000
            batchSize = 100
        }
    }
    mongoConnection {
        host = 'localhost'
        port = 27020
    }
    databases {
        users {
            customers {
                count = 1000
                document = { ->
                    """
                    {"entryNo": ${randomNumber()}, "fName": "${randomString(10)}", "lName": "${randomString(5)}" }
                    """.stripMargin()
                }
            }

            projects {
                count = 500
                document = { ->
                    "{ 'projectId' : ${randomNumber()} }"
                }
            }
        }

        transactions {
            orders {
                count = 800
                document = { ->
                    "{ 'entryNo' : ${randomNumber()}}"
                }
            }
        }
    }

    deltas {

        baseDir = "C:\\Users\\Vivek-EE\\Desktop\\Midas-1.0.0.Alpha1\\deltas"
        expansions = [
            '0001_users_customers.delta' :      """
                                                use users
                                                db.customers.add('{"age": 0}')
                                                db.customers.copy("entryNo", "customerID")
                                                db.customers.mergeInto('fullName', ' ', '["fName", "lName"]')
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