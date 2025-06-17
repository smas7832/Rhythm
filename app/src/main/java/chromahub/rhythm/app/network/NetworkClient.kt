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
import chromahub.rhythm.app.network.MusicBrainzApiService
import chromahub.rhythm.app.network.SpotifyApiService
import chromahub.rhythm.app.network.CoverArtArchiveService
import chromahub.rhythm.app.network.LastFmApiService

object NetworkClient {
    private const val TAG = "NetworkClient"
    
    private const val SPOTIFY_API_KEY = "acd7746756msh38770eb0ec2ea68p15c583jsn5ac26c448f24"
    private const val SPOTIFY_BASE_URL = "https://spotify23.p.rapidapi.com/"
    private const val LRCLIB_BASE_URL = "https://lrclib.net/"
    private const val MUSICBRAINZ_BASE_URL = "https://musicbrainz.org/"
    private const val COVERART_BASE_URL = "https://coverartarchive.org/"
    private const val LASTFM_API_KEY = "ba62d1b3203307dcbfc78a5cb2be4888"
    private const val LASTFM_BASE_URL = "https://ws.audioscrobbler.com/"
    
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
    
    private fun musicBrainzHeadersInterceptor() = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "RhythmApp/1.0 (contact@chromahub.dev)")
            .header("Accept", "application/json")
            .build()
        chain.proceed(request)
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
    
    private val musicBrainzHttpClient = OkHttpClient.Builder()
        .addInterceptor(musicBrainzHeadersInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .build()

    private val musicBrainzRetrofit = Retrofit.Builder()
        .baseUrl(MUSICBRAINZ_BASE_URL)
        .client(musicBrainzHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val coverArtHttpClient = OkHttpClient.Builder()
        .addInterceptor(musicBrainzHeadersInterceptor()) // same UA rules
        .addInterceptor(loggingInterceptor)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .build()
    
    private val coverArtRetrofit = Retrofit.Builder()
        .baseUrl(COVERART_BASE_URL)
        .client(coverArtHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val spotifyApiService: SpotifyApiService = spotifyRetrofit.create(SpotifyApiService::class.java)
    val lrclibApiService: LRCLibApiService = lrclibRetrofit.create(LRCLibApiService::class.java)
    val musicBrainzApiService: MusicBrainzApiService = musicBrainzRetrofit.create(MusicBrainzApiService::class.java)
    val coverArtArchiveService: CoverArtArchiveService = coverArtRetrofit.create(CoverArtArchiveService::class.java)
    private val lastFmRetrofit = Retrofit.Builder()
        .baseUrl(LASTFM_BASE_URL)
        .client(loggingInterceptor.let { OkHttpClient.Builder().addInterceptor(it).build() })
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val lastFmApiService: LastFmApiService = lastFmRetrofit.create(LastFmApiService::class.java)
    
    // Generic OkHttp client for one-off requests (e.g., Wikidata JSON). Reuses header interceptor.
    val genericHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(musicBrainzHeadersInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()
    
    fun getSpotifyApiKey(): String = SPOTIFY_API_KEY
    fun getLastFmApiKey(): String = LASTFM_API_KEY
} 