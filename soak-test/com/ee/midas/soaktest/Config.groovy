package com.ee.midas.soaktest

import groovy.transform.Field

import java.util.concurrent.TimeUnit

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
            every = [2, TimeUnit.MINUTES]
            batchSize = 100
        }

        update {
            every = [3, TimeUnit.MINUTES]
            batchSize = 100
        }
    }

    mongoConnection {
        host = 'localhost'
        port = 27020
    }

    app {
        v1 {

            changeSet = 1
            deltas {
                expansion {

                }
                contraction {

                }
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
        }

        v2 {

            changeSet = 2

            deltas {

                baseDir = "midas\\deltas"
                expansions = [
                        '0001_users_customers.delta' :      """
                                                            use users
                                                            db.customers.add('{"age": 0}')
                                                            db.customers.copy("entryNo", "customerID")
                                                            db.customers.merge('["fName", "lName"]', ' ', 'fullName')
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
                                                            db.customers.remove('["entryNo", "fName", "lName"]')
                                                            use transactions
                                                            db.orders.remove('["entryNo"]')
                                                            """

                ]
            }

            databases {
                users {
                    customers {
                        count = 1000
                        document = { ->
                            """
                            {"customerID": ${randomNumber()}, "fullName": "${randomString(10)} ${randomString(5)}", "age": ${randomNumber(80)}}
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
                            "{ 'orderID' : 'OD${randomNumber()}'}"
                        }
                    }
                }
            }
        }

        v3 {

        }
    }

}
