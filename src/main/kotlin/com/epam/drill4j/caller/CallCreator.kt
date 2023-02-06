package com.epam.drill4j.caller

import com.epam.drill4j.caller.config.Config
import com.epam.drill4j.caller.model.AdminPayload
import com.epam.drill4j.caller.model.Payload
import com.epam.drill4j.caller.model.TestDetails
import com.epam.drill4j.caller.model.TestInfo
import com.epam.drill4j.metadata.model.MetadataModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.util.*
import kotlin.io.path.Path
import kotlin.random.Random

class CallCreator(
    private val apiEndpoint: String = Config.getProperty("api.endpoint"),
    private val token: String = Config.getProperty("api.authorization"),
    private val batchSize: Int = Config.getProperty("api.batch.size").toInt(),
    private val externalApiURL: String = Config.getProperty("api.external.url")
) {


    suspend fun call(path: String) {
        val readString = withContext(Dispatchers.IO) {
            Files.readString(Path(path))
        }
        val models = Gson().fromJson(readString, Array<MetadataModel>::class.java)
        // call to admin to start session
        val sessionID = startAdminSession()

        val list: ArrayList<TestInfo> = ArrayList()
        for ((counter, model) in models.withIndex()) {
            // call external api
            val drillTestId = callExternalApi(model, sessionID)
            list.add(TestInfo(id = "$drillTestId", details = TestDetails(testName = UUID.randomUUID().toString())))

            // call admin api to add tests
            if (counter == batchSize) {
                addTests(sessionID, list)
                list.clear()
            }
        }
        // send remaining tests
        if (list.isNotEmpty()) {
            addTests(sessionID, list)
        }

        // call admin to stop session
        stopAdminSession(sessionID)
    }

    private fun callExternalApi(model: MetadataModel, sessionID: String): Int {
        val pathToApi = "$externalApiURL${model.url}?${getParams(model.numberOfParams)}"
        val drillTestId = model.url.hashCode()
        makeRequest(
            request = HttpRequest.newBuilder()
                .getHeaders(
                    mapOf(
                        Pair("drill-test-id", drillTestId),
                        Pair("drill-session-id", sessionID)
                    )
                )
                .uri(URI.create(pathToApi))
                .method(model.method, HttpRequest.BodyPublishers.noBody())
                .build()
        )
        return drillTestId
    }

    private fun stopAdminSession(sessionID: String) {
        val jsonStopPayload = Gson().toJson(
            AdminPayload(
                type = "STOP",
                Payload(sessionId = sessionID)
            )
        )
        makeRequest(
            HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .method("POST", HttpRequest.BodyPublishers.ofString(jsonStopPayload))
                .header("Authorization", token)
                .build()
        )
    }

    private fun addTests(sessionID: String, list: ArrayList<TestInfo>) {
        val jsonAddTestsPayload = Gson().toJson(
            AdminPayload(
                type = "ADD_TESTS",
                Payload(sessionId = sessionID, testInfo = list)
            )
        )
        makeRequest(
            HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .method("POST", HttpRequest.BodyPublishers.ofString(jsonAddTestsPayload))
                .header("Authorization", token)
                .build()
        )
    }

    private fun startAdminSession(): String {
        val sessionID = UUID.randomUUID().toString()
        val jsonStartPayload = Gson().toJson(
            AdminPayload(
                type = "START",
                Payload(sessionId = sessionID, isGlobal = true, isRealTime = true, testType = "unit")
            )
        )
        makeRequest(
            HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .method("POST", HttpRequest.BodyPublishers.ofString(jsonStartPayload))
                .header("Authorization", token)
                .build()
        )
        return sessionID
    }

    private fun makeRequest(request: HttpRequest) {
        val client = HttpClient.newBuilder().build()
        val send = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        println(send)
    }

    private fun HttpRequest.Builder.getHeaders(headers: Map<String, Any>): HttpRequest.Builder = run {
        for (entry in headers.entries) {
            this.header(entry.key, "${entry.value}")
        }
        this
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
