package kz.tulpartaxi.kandyagash.di

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kz.tulpartaxi.kandyagash.BuildConfig
import kz.tulpartaxi.kandyagash.data.api.TulparApi
import kz.tulpartaxi.kandyagash.data.local.TokenStorage
import kz.tulpartaxi.kandyagash.data.local.db.TulparDatabase
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenStorage: TokenStorage): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val request = tokenStorage.token?.let { token ->
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } ?: chain.request()
            chain.proceed(request)
        }
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideTulparApi(client: OkHttpClient, moshi: Moshi): TulparApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TulparApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TulparDatabase =
        Room.databaseBuilder(context, TulparDatabase::class.java, "tulpar.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCityDao(db: TulparDatabase) = db.cityDao()

    @Provides
    fun provideOrderDao(db: TulparDatabase) = db.orderDao()
}
