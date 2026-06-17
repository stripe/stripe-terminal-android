package com.stripe.example.ktor3test

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresPermission
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.TerminalApplicationDelegate
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import com.stripe.example.ktor2lib.Ktor2Library
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import io.ktor.client.HttpClient
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
 * Verifies:
 * 1. Integrator's Ktor 3 HttpClient works (with HttpTimeout, ContentNegotiation)
 * 2. Terminal SDK initializes without crash (exercises internal Ktor code paths)
 * 3. SDK's discoverReaders triggers internal networking (serialization, HTTP client)
 *
 * Run: ./gradlew :app:installDebug
 */
class MainActivity : Activity() {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val results = StringBuilder()
        val tag = "Ktor3TestLog"

        Log.d(tag, "=== Starting Ktor 3 + Terminal SDK binary compatibility test ===")

        // Test 1: Ktor 3 HttpClient with HttpTimeout + ContentNegotiation
        try {
            val client = HttpClient {
                install(HttpTimeout) {
                    requestTimeoutMillis = 10_000
                    connectTimeoutMillis = 5_000
                    socketTimeoutMillis = 10_000
                }
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            results.appendLine("[PASS] Ktor 3 HttpClient + HttpTimeout + ContentNegotiation created")
            Log.d(tag, "TEST 1 PASS")
            client.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Ktor 3 client: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 1 FAIL", e)
        }

        // Test 2: Real HTTP request with timeout
        try {
            val client = HttpClient {
                install(HttpTimeout) { requestTimeoutMillis = 15_000 }
            }
            runBlocking {
                val response = client.get("https://api.stripe.com/healthcheck") {
                    timeout { requestTimeoutMillis = 15_000 }
                }
                val body = response.bodyAsText()
                results.appendLine("[PASS] HTTP GET succeeded (${body.length} bytes)")
                Log.d(tag, "TEST 2 PASS")
            }
            client.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] HTTP request: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 2 FAIL" + e.toString(), e)
        }

        // Test 3: Timeout fires correctly
        try {
            val client = HttpClient { install(HttpTimeout) }
            runBlocking {
                try {
                    client.get("https://api.stripe.com/healthcheck") {
                        timeout { requestTimeoutMillis = 100 }
                    }
                    results.appendLine("[FAIL] Timeout did not fire")
                } catch (e: Exception) {
                    results.appendLine("[PASS] Timeout fired: ${e.javaClass.simpleName}")
                    Log.d(tag, "TEST 3 PASS: ${e.javaClass.simpleName}")
                }
            }
            client.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Timeout: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 3 FAIL", e)
        }

        // Test 4: Terminal.init() — exercises SDK startup (class loading of all internal modules)
        try {
            TerminalApplicationDelegate.onCreate(application)
            if (!Terminal.isInitialized()) {
                Terminal.init(
                    applicationContext,
                    LogLevel.VERBOSE,
                    object : ConnectionTokenProvider {
                        override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
                            callback.onFailure(
                                ConnectionTokenException("Test only")
                            )
                        }
                    },
                    object : TerminalListener {},
                    null,
                )
            }
            results.appendLine("[PASS] Terminal.init() succeeded")
            Log.d(tag, "TEST 4 PASS")
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Terminal.init(): ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 4 FAIL", e)
        }

        // Test 5: Internet discovery — triggers SDK's Ktor HTTP client, serialization,
        // readBytes, and IOException catch path (will fail with token error but exercises all code)
        try {
            val config = DiscoveryConfiguration.InternetDiscoveryConfiguration(
                isSimulated = false,
            )
            val cancelable = Terminal.getInstance().discoverReaders(
                config,
                object : DiscoveryListener {
                    override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                        Log.d(tag, "TEST 5: discovered ${readers.size} readers")
                    }
                },
                object : Callback {
                    override fun onSuccess() {
                        Log.d(tag, "TEST 5: discovery completed")
                    }
                    override fun onFailure(e: TerminalException) {
                        // Expected: token provider fails, but by this point the SDK
                        // has already loaded all Ktor classes (serialization, HTTP client, etc.)
                        Log.d(tag, "TEST 5: expected error: ${e.message}")
                    }
                }
            )
            // Wait for the SDK to attempt networking (triggers Ktor class loading)
            Thread.sleep(3000)
            cancelable.cancel(object : Callback {
                override fun onSuccess() {}
                override fun onFailure(e: TerminalException) {}
            })
            results.appendLine("[PASS] Internet discovery ran (no NoClassDefFoundError)")
            Log.d(tag, "TEST 5 PASS")
        } catch (e: Throwable) {
            if (e is TerminalException) {
                results.appendLine("[PASS] Internet discovery: SDK networking ran (${e.message})")
                Log.d(tag, "TEST 5 PASS (TerminalException expected)")
            } else {
                results.appendLine("[FAIL] Internet discovery: ${e.javaClass.simpleName}: ${e.message}")
                Log.e(tag, "TEST 5 FAIL", e)
            }
        }

        // Test 6: Ktor2Library (compiled against Ktor 2, same APIs as Terminal SDK)
        // running with Ktor 3 at runtime — verifies binary compatibility
        try {
            val lib = Ktor2Library()
            val url = lib.buildUrl("httpbin.org", "post", 443)
            results.appendLine("[PASS] Ktor2Library.buildUrl() with URLBuilder: $url")
            Log.d(tag, "TEST 6 PASS: URLBuilder works")
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Ktor2Library URLBuilder: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 6 FAIL", e)
        }

        // Test 7: Ktor2Library makes a POST request (HttpStatement.execute + readBytes + GMTDate)
        try {
            val lib = Ktor2Library()
            val url = lib.buildUrl("httpbin.org", "post", 443)
            val result = runBlocking { lib.makePost(url, "test body".toByteArray()) }
            if (result.success) {
                results.appendLine("[PASS] Ktor2Library POST: ${result.bodySize} bytes, ${result.latencyMs}ms")
            } else {
                results.appendLine("[FAIL] Ktor2Library POST: ${result.error}")
            }
            Log.d(tag, "TEST 7: $result")
            lib.close()
        } catch (e: Throwable) {
            results.appendLine("[FAIL] Ktor2Library POST: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(tag, "TEST 7 FAIL", e)
        }

        Log.d(tag, "=== ALL TESTS COMPLETE ===")
        Log.d(tag, results.toString())

        val textView = TextView(this).apply {
            text = "Ktor 3 + Terminal SDK 5.6.0\n\n$results"
            textSize = 14f
            setPadding(32, 64, 32, 32)
        }
        setContentView(textView)
    }
}
