package com.epam.drill4j.caller.model

data class AdminPayload(
    val type: String,
    val payload: Payload
)

data class Payload(
    var sessionId: String,
    var isGlobal: Boolean? = null,
    var isRealTime: Boolean? = null,
    var testType: String? = null,
    var testInfo: ArrayList<TestInfo>? = null
)

data class TestInfo(
    var id: String,
    var result: String = "PASSED",
    var startedAt: Int = 0,
    var finishedAt: Int = 0,
    var details: TestDetails
)

data class TestDetails(
    var engine: String = "",
    var path: String = "",
    val testName: String,
    val params: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap()
)
