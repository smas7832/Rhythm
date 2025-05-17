package chromahub.rhythm.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    // Using a valid public API key for Last.fm
    private const val LAST_FM_API_KEY = "e5c0eaf8688d9a576e72ea5e01a23c9e"
    private const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"
    private const val LYRICS_BASE_URL = "https://api.lyrics.ovh/v1/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
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