package com.synosoftware.battery.di

import android.content.Context
import androidx.room.Room
import com.synosoftware.battery.data.session.BatteryDatabase
import com.synosoftware.battery.data.session.ChargeSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): BatteryDatabase {
        return Room.databaseBuilder(
            context,
            BatteryDatabase::class.java,
            "open_battery.db",
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChargeSessionDao(database: BatteryDatabase): ChargeSessionDao {
        return database.chargeSessionDao()
    }
}

