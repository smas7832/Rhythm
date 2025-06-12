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
import chromahub.rhythm.app.network.LRCLibApiService

object NetworkClient {
    private const val TAG = "NetworkClient"
    
    private const val SPOTIFY_API_KEY = "acd7746756msh38770eb0ec2ea68p15c583jsn5ac26c448f24"
    private const val SPOTIFY_BASE_URL = "https://spotify23.p.rapidapi.com/"
    private const val LRCLIB_BASE_URL = "https://lrclib.net/"
    
    // Connection timeouts
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    private const val MAX_RETRIES = 3
    
    private val connectionPool = ConnectionPool(5, 30, TimeUnit.SECONDS)
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val retryInterceptor = Interceptor { chain ->
        var currentRetry = 0
        var response: Response? = null
        var exception: IOException? = null
        
        while (currentRetry < MAX_RETRIES) {
            try {
                Log.d(TAG, "Attempting request (attempt ${currentRetry + 1}/${MAX_RETRIES}): ${chain.request().url}")
                response = chain.proceed(chain.request())
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Request successful: ${chain.request().url}")
                    return@Interceptor response
                } else {
                    Log.w(TAG, "Request failed with code ${response.code}: ${chain.request().url}")
                    response.close()
                }
            } catch (e: IOException) {
                exception = e
                Log.e(TAG, "Request error (attempt ${currentRetry + 1}): ${e.message}")
                
                if (e is SocketTimeoutException || e is UnknownHostException) {
                    currentRetry++
                    if (currentRetry < MAX_RETRIES) {
                        val backoffDelay = (2.0.pow(currentRetry.toDouble()) * 1000).toLong()
                        Log.d(TAG, "Retrying after ${backoffDelay}ms delay")
                        Thread.sleep(backoffDelay)
                        continue
                    }
                }
                throw e
            }
            currentRetry++
        }
        
        throw exception ?: IOException("Request failed after $MAX_RETRIES retries")
    }
    
    private val spotifyHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .build()
    
    private val spotifyRetrofit = Retrofit.Builder()
        .baseUrl(SPOTIFY_BASE_URL)
        .client(spotifyHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val lrclibHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .build()
    
    private val lrclibRetrofit = Retrofit.Builder()
        .baseUrl(LRCLIB_BASE_URL)
        .client(lrclibHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val spotifyApiService: SpotifyApiService = spotifyRetrofit.create(SpotifyApiService::class.java)
    val lrclibApiService: LRCLibApiService = lrclibRetrofit.create(LRCLibApiService::class.java)
    
    fun getSpotifyApiKey(): String = SPOTIFY_API_KEY
} 