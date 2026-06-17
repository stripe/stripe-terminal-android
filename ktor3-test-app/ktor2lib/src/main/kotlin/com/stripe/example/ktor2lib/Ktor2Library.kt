package com.stripe.example.ktor2lib

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.utils.io.errors.IOException

/**
 * Library compiled against Ktor 2.3.13.
 * Uses the same Ktor APIs as the Terminal SDK's published bytecode:
 * - HttpClient(OkHttp)
 * - URLBuilder (constructor, setProtocol, setHost, setPort, setPathSegments, build)
 * - HttpMethod.Post
 * - HttpRequestBuilder (setBody, setMethod)
 * - HttpStatement (execute)
 * - HttpResponse (readBytes, getRequestTime, getResponseTime)
 * - GMTDate (getTimestamp)
 * - IOException catch
 */
class Ktor2Library {

    private val client = HttpClient(OkHttp)

    fun buildUrl(host: String, path: String, port: Int = 443): Url {
        val builder = URLBuilder(
            protocol = URLProtocol.HTTPS,
            host = host,
            port = port,
        )
        builder.pathSegments = listOf(path)
        return builder.build()
    }

    suspend fun makePost(url: Url, body: ByteArray): Result {
        return try {
            val request = HttpRequestBuilder()
            request.method = HttpMethod.Post
            request.url(url)
            request.setBody(body as Any)

            val statement = HttpStatement(request, client)
            val response = statement.execute()

            val responseBytes = response.readBytes()
            val requestTime = response.requestTime
            val responseTime = response.responseTime
            val latencyMs = responseTime.timestamp - requestTime.timestamp

            Result(
                success = true,
                bodySize = responseBytes.size,
                latencyMs = latencyMs,
            )
        } catch (e: IOException) {
            Result(success = false, error = "IOException: ${e.message}")
        }
    }

    fun close() {
        client.close()
    }

    data class Result(
        val success: Boolean = false,
        val bodySize: Int = 0,
        val latencyMs: Long = 0,
        val error: String? = null,
    )
}
