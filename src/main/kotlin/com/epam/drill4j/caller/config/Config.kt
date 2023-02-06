package com.epam.drill4j.caller.config

import java.util.*

const val CONFIG_FILE_NAME = "app.properties"

object Config {

    private val properties = Properties()

    init {
        val file = this::class.java.classLoader.getResourceAsStream(CONFIG_FILE_NAME)
        properties.load(file)
    }

    fun getProperty(key: String): String = properties.getProperty(key)
}
