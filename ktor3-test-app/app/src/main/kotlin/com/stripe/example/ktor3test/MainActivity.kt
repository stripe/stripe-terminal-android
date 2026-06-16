package com.stripe.example.ktor3test

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.TerminalApplicationDelegate
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Test app to verify Ktor 3.x + Stripe Terminal SDK coexistence.
 *
 * Reproduces the scenario from https://github.com/stripe/stripe-terminal-android/issues/636:
 * "Build and use an app with both Stripe Terminal SDK and Ktor 3.x.
 *  It will build, but crash on startup with some missing module like HttpTimeout."
 *
 * Run with:
 *   ./gradlew :app:installDebug
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val results = StringBuilder()
        val tag = "Ktor3Test"

        Log.d(tag, "=== Starting Ktor 3 + Terminal SDK compatibility test ===")

        // Test 1: Create Ktor 3 HttpClient with HttpTimeout plugin
        try {
            val client = HttpClient(OkHttp) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 10_000
                    connectTimeoutMillis = 5_000
                    socketTimeoutMillis = 10_000
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            results.appendLine("[PASS] Ktor 3 HttpClient with HttpTimeout created")
            Log.d(tag, "TEST 1 PASS: Ktor 3 HttpClient + HttpTimeout created")
            client.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Ktor 3 HttpClient creation: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 1 FAIL", e)
        }

        // Test 2: Make a real HTTP request with per-request timeout
        try {
            val client = HttpClient(OkHttp) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 15_000
                }
            }
            runBlocking {
                val response = client.get("https://httpbin.org/get") {
                    timeout { requestTimeoutMillis = 15_000 }
                }
                val body = response.bodyAsText()
                results.appendLine("[PASS] HTTP GET with timeout succeeded (${body.length} bytes)")
                Log.d(tag, "TEST 2 PASS: HTTP request succeeded")
            }
            client.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] HTTP request: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 2 FAIL", e)
        }

        // Test 3: Trigger a timeout to verify the timeout machinery works end-to-end
        try {
            val client = HttpClient(OkHttp) {
                install(HttpTimeout)
            }
            runBlocking {
                try {
                    client.get("https://httpbin.org/delay/10") {
                        timeout { requestTimeoutMillis = 100 }
                    }
                    results.appendLine("[FAIL] Timeout did not fire")
                } catch (e: Exception) {
                    results.appendLine("[PASS] Timeout fired: ${e.javaClass.simpleName}")
                    Log.d(tag, "TEST 3 PASS: Timeout exception: ${e.javaClass.simpleName}")
                }
            }
            client.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Timeout test: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 3 FAIL", e)
        }

        // Test 4: Initialize Terminal SDK (exercises the SDK's internal Ktor code paths)
        try {
            TerminalApplicationDelegate.onCreate(application)

            if (!Terminal.isInitialized()) {
                Terminal.init(
                    applicationContext,
                    LogLevel.VERBOSE,
                    object : ConnectionTokenProvider {
                        override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
                            callback.onFailure(
                                ConnectionTokenException("Test only - no backend configured")
                            )
                        }
                    },
                    object : TerminalListener {},
                    null,
                )
            }
            results.appendLine("[PASS] Terminal SDK initialized successfully")
            Log.d(tag, "TEST 4 PASS: Terminal.init() succeeded")
        } catch (e: TerminalException) {
            results.appendLine("[FAIL] Terminal init: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 4 FAIL", e)
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Terminal init: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 4 FAIL", e)
        }

        Log.d(tag, "=== ALL TESTS COMPLETE ===")
        Log.d(tag, results.toString())

        // Show results on screen
        val textView = TextView(this).apply {
            text = "Ktor 3 + Terminal SDK Test\n\n$results"
            textSize = 14f
            setPadding(32, 64, 32, 32)
        }
        setContentView(textView)
    }
}
