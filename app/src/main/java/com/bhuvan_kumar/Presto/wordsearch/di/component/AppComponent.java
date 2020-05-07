package com.bhuvan_kumar.Presto.wordsearch.di.component;

import com.bhuvan_kumar.Presto.wordsearch.di.modules.AppModule;
import com.bhuvan_kumar.Presto.wordsearch.di.modules.DataSourceModule;
import com.bhuvan_kumar.Presto.wordsearch.features.FullscreenActivity;
import com.bhuvan_kumar.Presto.wordsearch.features.gamehistory.GameHistoryActivity;
import com.bhuvan_kumar.Presto.wordsearch.features.gameover.GameOverActivity;
import com.bhuvan_kumar.Presto.wordsearch.features.gameplay.GamePlayActivity;
import com.bhuvan_kumar.Presto.wordsearch.features.mainmenu.MainMenuActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by abdularis on 18/07/17.
 */

@Singleton
@Component(modules = {AppModule.class, DataSourceModule.class})
public interface AppComponent {

    void inject(GamePlayActivity activity);

    void inject(MainMenuActivity activity);

    void inject(GameOverActivity activity);

    void inject(FullscreenActivity activity);

    void inject(GameHistoryActivity activity);

}
