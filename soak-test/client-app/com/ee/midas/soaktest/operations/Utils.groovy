package com.ee.midas.soaktest.operations


static def randomString(int length) {
    String alphabet = (('A'..'Z')+('a'..'z')).join()
    new Random().with {
        (1..length).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
    }
}

static def randomNumber(int max) {
    new Random().nextInt(max)
}
