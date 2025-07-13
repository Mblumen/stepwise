package de.hd.fitbittracks;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import de.hd.fitbittracks.daos.AchievementDao;
import de.hd.fitbittracks.daos.UserProgressDao;
import de.hd.fitbittracks.database.AppDatabase;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public static AchievementDao provideAchievementDao(AppDatabase db) {
        return db.achievementDao();
    }

    @Provides
    public static UserProgressDao provideUserProgressDao(AppDatabase db) {
        return db.userProgressDao();
    }
}