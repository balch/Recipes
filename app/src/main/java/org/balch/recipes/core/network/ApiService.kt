package org.balch.recipes.core.network

import com.diamondedge.logging.logging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import org.balch.recipes.core.coroutines.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for making HTTP requests using Ktor.
 */
@Singleton
class ApiService @Inject constructor(
    private val httpClientFactory: HttpClientFactory,
    val dispatcherProvider: DispatcherProvider,
) {
    val client: HttpClient by lazy { httpClientFactory.create() }
    val logger = logging("ApiService")

    suspend inline fun <reified T> get(
        url: String,
        parameters: Map<String, String> = emptyMap()
    ): Result<T> =
        try {
            val response: HttpResponse = withContext(dispatcherProvider.io) {
                client.get(url) {
                    parameters.forEach { (key, value) ->
                        parameter(key, value)
                    }
                }
            }

            logger.d { "HTTP ${response.status.value} ${response.status.description}" }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val data: T = response.body()
                    logger.v { "HTTP Response: $data" }
                    Result.success(data)
                }

                else -> {
                    Result.failure(
                        ApiException(
                            httpStatusCode = response.status.value,
                            message = "HTTP ${response.status.value}: ${response.status.description}"
                        ).also {
                            logger.e(it) { "HTTP Error" }
                        }
                    )
                }
            }

        } catch (e: SerializationException) {
            Result.failure(
                ApiException(message = "Failed to parse response: ${e.message}")
                    .also { logger.e(it) { "HTTP SerializationException Error" } }
            )
        } catch (e: Exception) {
            Result.failure(
                ApiException(message = "Network request failed: ${e.message}")
                    .also { logger.e(it) { "HTTP Exception Error" } }
            )
        }

    fun close() {
        logger.d { "Closing HTTP client" }
        client.close()
    }
}

data class ApiException(
    override val message: String,
    val httpStatusCode: Int = -1,
) : Exception(message)