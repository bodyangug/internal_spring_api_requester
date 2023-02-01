package com.epam.drill4j.caller

import com.epam.drill4j.metadata.model.MetadataModel
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.random.Random

class CallCreator {

    fun call(path: String) {
        val readString = Files.readString(Path(path))
        val models = Gson().fromJson(readString, Array<MetadataModel>::class.java)

        for (model in models) {
            //call to local API
            val pathToApi = "http://localhost:8080/${model.url}?${getParams(model.numberOfParams)}"
            makeRequest(pathToApi, model.method)

            //TODO add call to Admin API
            println("Make call to admin")
        }
    }

    private fun makeRequest(path: String, method: String) {
        val url = URL(path)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        BufferedReader(InputStreamReader(conn.inputStream))
    }

    private fun getParams(number: Int): String {
        var result = ""
        for (i in 0..number) {
            result += if (i == number) {
                "param$i=${Random.nextBoolean()}"
            } else {
                "param$i=${Random.nextBoolean()}&"
            }
        }
        return result
    }
}
