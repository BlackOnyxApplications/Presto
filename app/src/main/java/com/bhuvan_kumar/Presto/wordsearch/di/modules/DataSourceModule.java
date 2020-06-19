package com.bhuvan_kumar.Presto.wordsearch.di.modules;

import android.content.Context;

import com.bhuvan_kumar.Presto.wordsearch.data.sqlite.DbHelper;
import com.bhuvan_kumar.Presto.wordsearch.data.sqlite.GameDataSQLiteDataSource;
import com.bhuvan_kumar.Presto.wordsearch.data.xml.WordXmlDataSource;
import com.bhuvan_kumar.Presto.wordsearch.data.GameDataSource;
import com.bhuvan_kumar.Presto.wordsearch.data.WordDataSource;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DataSourceModule {

    @Provides
    @Singleton
    DbHelper provideDbHelper(Context context) {
        return new DbHelper(context);
    }

    @Provides
    @Singleton
    GameDataSource provideGameRoundDataSource(DbHelper dbHelper) {
        return new GameDataSQLiteDataSource(dbHelper);
    }

//    @Provides
//    @Singleton
//    WordDataSource provideWordDataSource(DbHelper dbHelper) {
//        return new WordSQLiteDataSource(dbHelper);
//    }

    @Provides
    @Singleton
    WordDataSource provideWordDataSource(Context context) {
        return new WordXmlDataSource(context);
    }

}
