package chromahub.rhythm.app.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.pow

object NetworkClient {
    private const val TAG = "NetworkClient"
    
    // Using a valid public API key for Last.fm
    private const val LAST_FM_API_KEY = "e5c0eaf8688d9a576e72ea5e01a23c9e"
    private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"
    private const val LYRICS_BASE_URL = "https://api.lyrics.ovh/v1/"
    
    // Configurable timeouts
    private const val CONNECT_TIMEOUT_SECONDS = 20L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 20L
    private const val MAX_RETRIES = 3
    
    // Connection pool configuration
    private const val MAX_IDLE_CONNECTIONS = 5
    private const val KEEP_ALIVE_DURATION_MINUTES = 5L
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    
    // Custom retry interceptor
    private val retryInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            var retryCount = 0
            
            while (response == null && retryCount < MAX_RETRIES) {
                if (retryCount > 0) {
                    Log.d(TAG, "Retrying request (attempt ${retryCount + 1}): ${request.url}")
                    // Exponential backoff
                    val backoffMs = (2.0.pow(retryCount.toDouble()) * 1000).toLong()
                    Thread.sleep(backoffMs)
                }
                
                try {
                    response = chain.proceed(request)
                    
                    // If response is server error (5xx), retry
                    if (response.code >= 500 && retryCount < MAX_RETRIES - 1) {
                        Log.w(TAG, "Server error ${response.code}, retrying: ${request.url}")
                        response.close()
                        response = null
                    }
                } catch (e: IOException) {
                    exception = e
                    when (e) {
                        is SocketTimeoutException -> {
                            Log.w(TAG, "Timeout, retrying: ${request.url}", e)
                        }
                        is UnknownHostException -> {
                            Log.e(TAG, "Unknown host, retrying: ${request.url}", e)
                        }
                        else -> {
                            Log.e(TAG, "Network error, retrying: ${request.url}", e)
                        }
                    }
                }
                
                retryCount++
            }
            
            // If we exhausted retries and still have no response, throw the last exception
            return response ?: throw exception ?: IOException("Unknown error during request")
        }
    }
    
    private val connectionPool = ConnectionPool(
        MAX_IDLE_CONNECTIONS,
        KEEP_ALIVE_DURATION_MINUTES,
        TimeUnit.MINUTES
    )
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            try {
                val request = chain.request()
                val response = chain.proceed(request)
                
                // Log failed responses
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Error: ${response.code} for ${request.url}")
                }
                
                response
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                throw e
            }
        }
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val lyricsRetrofit = Retrofit.Builder()
        .baseUrl(LYRICS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val lastFmApiService: LastFmApiService by lazy {
        retrofit.create(LastFmApiService::class.java)
    }
    
    val lyricsApiService: LyricsApiService by lazy {
        lyricsRetrofit.create(LyricsApiService::class.java)
    }
    
    fun getLastFmApiKey(): String = LAST_FM_API_KEY
} 