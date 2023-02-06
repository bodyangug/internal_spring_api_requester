package com.epam.drill4j.caller

import kotlinx.coroutines.runBlocking

fun main() {
    // NOTE: do not forgot to create folder files if it does not exist.
    runBlocking {
        CallCreator().call("files/endpoints.json")
    }
}
