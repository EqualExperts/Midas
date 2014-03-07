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
            every = [1, TimeUnit.SECONDS]
            batchSize = 50
        }

        update {
            every = [1, TimeUnit.SECONDS]
            batchSize = 50
        }
    }

    mongoConnection {
        host = 'localhost'
        port = 27020
    }

    app {
        v0 {

            changeSet = 0
            deltas {
                expansion {

                }
                contraction {

                }
            }

            databases {
                users {
                    customers {
                        count = 330000
                        document = { ->
                            """
                            {"entryNo": ${randomNumber()}, "fName": "${randomString(10)}", "lName": "${randomString(5)}" }
                            """.stripMargin()
                        }
                    }

                    projects {
                        count = 330000
                        document = { ->
                            "{ 'projectId' : ${randomNumber()} }"
                        }
                    }
                }

                transactions {
                    orders {
                        count = 330000
                        document = { ->
                            "{ 'entryNo' : ${randomNumber()}}"
                        }
                    }
                }
            }
        }

        v1 {

            changeSet = 1

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
                        count = 400000
                        document = { ->
                            """
                            {"customerID": ${randomNumber()}, "fullName": "${randomString(10)} ${randomString(5)}", "age": ${randomNumber(80)}}
                            """.stripMargin()
                        }
                    }

                    projects {
                        count = 400000
                        document = { ->
                            "{ 'projectId' : ${randomNumber()} }"
                        }
                    }
                }

                transactions {
                    orders {
                        count = 400000
                        document = { ->
                            "{ 'orderID' : 'OD${randomNumber()}'}"
                        }
                    }
                }
            }
        }

        v2 {

        }
    }

}
