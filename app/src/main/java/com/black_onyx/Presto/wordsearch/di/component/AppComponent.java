package com.black_onyx.Presto.wordsearch.di.component;

import com.black_onyx.Presto.wordsearch.di.modules.AppModule;
import com.black_onyx.Presto.wordsearch.di.modules.DataSourceModule;
import com.black_onyx.Presto.wordsearch.features.FullscreenActivity;
import com.black_onyx.Presto.wordsearch.features.gamehistory.GameHistoryActivity;
import com.black_onyx.Presto.wordsearch.features.gameover.GameOverActivity;
import com.black_onyx.Presto.wordsearch.features.gameplay.GamePlayActivity;
import com.black_onyx.Presto.wordsearch.features.mainmenu.MainMenuActivity;

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
