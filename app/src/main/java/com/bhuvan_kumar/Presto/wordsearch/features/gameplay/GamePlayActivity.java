package com.bhuvan_kumar.Presto.wordsearch.features.gameplay;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bhuvan_kumar.Presto.App;
import com.bhuvan_kumar.Presto.R;
import com.bhuvan_kumar.Presto.activity.HomeActivity;
import com.bhuvan_kumar.Presto.util.PreferenceUtils;
import com.bhuvan_kumar.Presto.wordsearch.features.SoundPlayer;
import com.bhuvan_kumar.Presto.wordsearch.features.ViewModelFactory;
import com.bhuvan_kumar.Presto.wordsearch.commons.DurationFormatter;
import com.bhuvan_kumar.Presto.wordsearch.commons.Util;
import com.bhuvan_kumar.Presto.wordsearch.features.gameover.GameOverViewModel;
import com.bhuvan_kumar.Presto.wordsearch.features.settings.Preferences;
import com.bhuvan_kumar.Presto.wordsearch.model.GameData;
import com.bhuvan_kumar.Presto.wordsearch.custom.LetterBoard;
import com.bhuvan_kumar.Presto.wordsearch.custom.StreakView;
import com.bhuvan_kumar.Presto.wordsearch.custom.layout.FlowLayout;
import com.bhuvan_kumar.Presto.wordsearch.features.gameover.GameOverActivity;
import com.bhuvan_kumar.Presto.wordsearch.features.FullscreenActivity;
import com.bhuvan_kumar.Presto.wordsearch.model.UsedWord;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class GamePlayActivity extends FullscreenActivity implements
        AdapterView.OnItemSelectedListener{

    public static final String EXTRA_GAME_ROUND_ID =
            "com.bhuvan_kumar.Presto.wordsearch.features.gameplay.GamePlayActivity.ID";
    public static final String EXTRA_ROW_COUNT =
            "com.bhuvan_kumar.Presto.wordsearch.features.gameplay.GamePlayActivity.ROW";
    public static final String EXTRA_COL_COUNT =
            "com.bhuvan_kumar.Presto.wordsearch.features.gameplay.GamePlayActivity.COL";

    private static final StreakLineMapper STREAK_LINE_MAPPER = new StreakLineMapper();

    private UnifiedNativeAd nativeAd;

    @Inject
    SoundPlayer mSoundPlayer;

    @Inject ViewModelFactory mViewModelFactory;
    private GamePlayViewModel mViewModel;

    @BindView(R.id.letter_board) LetterBoard mLetterBoard;
    @BindView(R.id.flow_layout) FlowLayout mFlowLayout;

    @BindView(R.id.text_sel_layout) View mTextSelLayout;
    @BindView(R.id.text_selection) TextView mTextSelection;

    @BindView(R.id.loading) View mLoading;
    @BindView(R.id.loadingText) TextView mLoadingText;
    @BindView(R.id.content_layout) View mContentLayout;
    ImageView mRefreshView;

    @BindColor(R.color.gray) int mGrayColor;
    int rowCount = 5, colCount = 5;

    @BindArray(R.array.game_round_dimension_values)
    int[] mGameRoundDimVals;

    String[] allWords;

    private ArrayLetterGridDataAdapter mLetterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        ButterKnife.bind(this);
        ((App) getApplication()).getAppComponent().inject(this);

        mLetterBoard.getStreakView().setEnableOverrideStreakLineColor(getPreferences().grayscale());
        mLetterBoard.getStreakView().setOverrideStreakLineColor(mGrayColor);
        mLetterBoard.setOnLetterSelectionListener(new LetterBoard.OnLetterSelectionListener() {
            @Override
            public void onSelectionBegin(StreakView.StreakLine streakLine, String str) {
                streakLine.setColor(Util.getRandomColorWithAlpha(170));
                mTextSelLayout.setVisibility(View.VISIBLE);
                mTextSelection.setText(str);
            }

            @Override
            public void onSelectionDrag(StreakView.StreakLine streakLine, String str) {
                if (str.isEmpty()) {
                    mTextSelection.setText("...");
                } else {
                    mTextSelection.setText(str);
                }
            }

            @Override
            public void onSelectionEnd(StreakView.StreakLine streakLine, String str) {
                mViewModel.answerWord(str, STREAK_LINE_MAPPER.revMap(streakLine), getPreferences().reverseMatching());
                mTextSelection.setText(str);
            }
        });

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(GamePlayViewModel.class);
        mViewModel.getOnGameState().observe(this, this::onGameStateChanged);
        mViewModel.getOnAnswerResult().observe(this, this::onAnswerResult);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(EXTRA_GAME_ROUND_ID)) {
                int gid = extras.getInt(EXTRA_GAME_ROUND_ID);
                mViewModel.loadGameRound(gid);
            } else {
                rowCount = extras.getInt(EXTRA_ROW_COUNT);
                colCount = extras.getInt(EXTRA_COL_COUNT);
                mViewModel.generateNewGameRound(rowCount, colCount);
            }
        }

        if (!getPreferences().showGridLine()) {
            mLetterBoard.getGridLineBackground().setVisibility(View.INVISIBLE);
        } else {
            mLetterBoard.getGridLineBackground().setVisibility(View.VISIBLE);
        }

        mLetterBoard.getStreakView().setSnapToGrid(getPreferences().getSnapToGrid());

        mRefreshView = findViewById(R.id.refresh);
        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame(rowCount, colCount);
            }
        });
        Spinner mGridSizeSpinner = findViewById(R.id.game_template_spinner);
        int position=0;
        if(rowCount == 6) position=1;
        else if(rowCount == 7) position=2;
        else if(rowCount == 8) position=3;
        else if(rowCount == 10) position=4;
        else if(rowCount == 12) position=5;
        mGridSizeSpinner.setSelection(position);
        mGridSizeSpinner.setOnItemSelectedListener(this);

        ImageView hintView = findViewById(R.id.hint);
        hintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showHintView(allWords);
            }
        });


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        refreshAd(null, getString(R.string.game_head_ad_unit_id));


        ImageView volume_full = findViewById(R.id.ic_volume_full);
        ImageView mute = findViewById(R.id.ic_volume_off);
        volume_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volume_full.setVisibility(GONE);
                mute.setVisibility(VISIBLE);
                PreferenceUtils.SetGameMuted(GamePlayActivity.this, true);
            }
        });
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volume_full.setVisibility(VISIBLE);
                mute.setVisibility(GONE);
                PreferenceUtils.SetGameMuted(GamePlayActivity.this, false);
            }
        });

        if(PreferenceUtils.IsGameMuted(this)){
            mute.setVisibility(VISIBLE);
            volume_full.setVisibility(GONE);
        }else{
            volume_full.setVisibility(VISIBLE);
            mute.setVisibility(GONE);
        }

    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);

    }

    private void refreshAd(View view, String adUnit) {

        AdLoader.Builder builder = new AdLoader.Builder(Objects.requireNonNull(this), adUnit);

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                FrameLayout frameLayout;
                UnifiedNativeAdView adView;
                if(view == null){
                    frameLayout = findViewById(R.id.fl_adplaceholder);
                    adView = (UnifiedNativeAdView) getLayoutInflater()
                            .inflate(R.layout.game_page_ad_view, null);
                }else{
                    frameLayout = view.findViewById(R.id.fl_adplaceholder);
                    adView = (UnifiedNativeAdView) getLayoutInflater()
                            .inflate(R.layout.home_page_custom_ad, null);
                }

                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }
        });

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e(getLocalClassName(), "errorCode :"+ errorCode);
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void showHintView(String[] values){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.game_hint_view, null);

        StringBuilder hints = new StringBuilder();
        for (String hint: values){
            hints.append(hint).append("    ");
        }
        TextView hintView = dialogView.findViewById(R.id.hint_text);
        hintView.setText(hints.toString());

        dialogBuilder.setView(dialogView);
        refreshAd(dialogView, getString(R.string.game_hint_ad_unit_id));
        loadNativeAd(dialogView);
        Button ok = dialogView.findViewById(R.id.ok_btnx);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.resumeGame();
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mViewModel.pauseGame();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.stopGame();
    }

    private void onAnswerResult(GamePlayViewModel.AnswerResult answerResult) {
        if (answerResult.correct) {
            TextView textView = findUsedWordTextViewByUsedWordId(answerResult.usedWordId);
            if (textView != null) {
                UsedWord uw = (UsedWord) textView.getTag();

                textView.setBackgroundColor(uw.getAnswerLine().color);
                textView.setText(uw.getString());
                textView.setTextColor(Color.WHITE);
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                Animator anim = AnimatorInflater.loadAnimator(this, R.animator.zoom_in_out);
                anim.setTarget(textView);
                anim.start();
            }

            if(! PreferenceUtils.IsGameMuted(this))
                mSoundPlayer.play(SoundPlayer.Sound.Correct);
        }
        else {
            mLetterBoard.popStreakLine();
            if(! PreferenceUtils.IsGameMuted(this))
                mSoundPlayer.play(SoundPlayer.Sound.Wrong);
        }
    }

    private void onGameStateChanged(GamePlayViewModel.GameState gameState) {
        showLoading(false, null);
        if (gameState instanceof GamePlayViewModel.Generating) {
            GamePlayViewModel.Generating state = (GamePlayViewModel.Generating) gameState;
            String text = "Generating " + state.rowCount + "x" + state.colCount + " grid";
            showLoading(true, text);
        } else if (gameState instanceof GamePlayViewModel.Finished) {
            showFinishGame(((GamePlayViewModel.Finished) gameState).mGameData.getId());
        } else if (gameState instanceof GamePlayViewModel.Paused) {

        } else if (gameState instanceof GamePlayViewModel.Playing) {
            onGameRoundLoaded(((GamePlayViewModel.Playing) gameState).mGameData);
        }
    }

    private void onGameRoundLoaded(GameData gameData) {
        if (gameData.isFinished()) {
            setGameAsAlreadyFinished();
        }

        showLetterGrid(gameData.getGrid().getArray());
        showUsedWords(gameData.getUsedWords());
        allWords = new String[gameData.getUsedWords().size()];
        for (int i=0; i<gameData.getUsedWords().size(); i++) {
            allWords[i] = gameData.getUsedWords().get(i).getString();
        }
        doneLoadingContent();
    }

    private void tryScale() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int boardWidth = mLetterBoard.getWidth();
        int screenWidth = metrics.widthPixels;

        if (getPreferences().autoScaleGrid() || boardWidth > screenWidth) {
            float scale = (float)screenWidth / (float)boardWidth;
            mLetterBoard.scale(scale, scale);
        }
    }

    private void doneLoadingContent() {
        // call tryScale() on the next render frame
        new Handler().postDelayed(this::tryScale, 100);
    }

    private void showLoading(boolean enable, String text) {
        if (enable) {
            mLoading.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mContentLayout.setVisibility(GONE);
            mLoadingText.setText(text);
        } else {
            mLoading.setVisibility(GONE);
            mLoadingText.setVisibility(GONE);
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showLetterGrid(char[][] grid) {
        if (mLetterAdapter == null) {
            mLetterAdapter = new ArrayLetterGridDataAdapter(grid);
            mLetterBoard.setDataAdapter(mLetterAdapter);
        }
        else {
            mLetterAdapter.setGrid(grid);
        }
    }

    private void showUsedWords(List<UsedWord> usedWords) {
        for (UsedWord uw : usedWords) {
            mFlowLayout.addView( createUsedWordTextView(uw) );
        }
    }

    private void showFinishGame(int gameId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.game_complete_layout, null);
        refreshAd(dialogView, getString(R.string.game_won_ad_unit_id));
        loadNativeAd(dialogView);
        dialogBuilder.setView(dialogView);
        Button newGame = dialogView.findViewById(R.id.new_game_btn);
        Button close = dialogView.findViewById(R.id.close_btn);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.stopGame();
                Intent intent = new Intent(GamePlayActivity.this, GamePlayActivity.class);
                intent.putExtra(GamePlayActivity.EXTRA_ROW_COUNT, rowCount);
                intent.putExtra(GamePlayActivity.EXTRA_COL_COUNT, colCount);
                startActivity(intent);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void startNewGame(int rowCount, int colCount) {
        mViewModel.stopGame();
        Intent intent = new Intent(this, GamePlayActivity.class);
        intent.putExtra(GamePlayActivity.EXTRA_ROW_COUNT, rowCount);
        intent.putExtra(GamePlayActivity.EXTRA_COL_COUNT, colCount);
        startActivity(intent);
    }

    private void setGameAsAlreadyFinished() {
        mLetterBoard.getStreakView().setInteractive(false);
    }

    //
    private TextView createUsedWordTextView(UsedWord uw) {
        TextView tv = new TextView(this);
        tv.setPadding(10, 5, 10, 5);
        if (uw.isAnswered()) {
            if (getPreferences().grayscale()) {
                uw.getAnswerLine().color = mGrayColor;
            }
            tv.setBackgroundColor(uw.getAnswerLine().color);
            tv.setText(uw.getString());
            tv.setTextColor(Color.WHITE);
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            mLetterBoard.addStreakLine(STREAK_LINE_MAPPER.map(uw.getAnswerLine()));
        }
        else {
            String str = uw.getString();
            if (uw.isMystery()) {
                int revealCount = uw.getRevealCount();
                String uwString = uw.getString();
                str = "";
                for (int i = 0; i < uwString.length(); i++) {
                    if (revealCount > 0) {
                        str += uwString.charAt(i);
                        revealCount--;
                    }
                    else {
                        str += " ?";
                    }
                }
            }
            tv.setText(str);
        }

        tv.setTag(uw);
        return tv;
    }

    private TextView findUsedWordTextViewByUsedWordId(int usedWordId) {
        for (int i = 0; i < mFlowLayout.getChildCount(); i++) {
            TextView tv = (TextView) mFlowLayout.getChildAt(i);
            UsedWord uw = (UsedWord) tv.getTag();
            if (uw != null && uw.getId() == usedWordId) {
                return tv;
            }
        }

        return null;
    }
    int check = 0;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(++check > 1) {
            startNewGame(mGameRoundDimVals[position], mGameRoundDimVals[position]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(GamePlayActivity.this, HomeActivity.class);
        startActivity(intent);
    }


    //    Facebook ads
    private void loadNativeAd(View view) {
        String TAG = getLocalClassName();
        NativeAd nativeAd = new NativeAd(this, getString(R.string.fb_home_ad_unit));
        Log.e(TAG, nativeAd.getPlacementId());
        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.e(TAG, "FB Ad error code: "+ adError.getErrorCode() + ", FB Ad error message: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                inflateAd(nativeAd, view);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        nativeAd.loadAd(NativeAdBase.MediaCacheFlag.ALL);
    }

    private void inflateAd(NativeAd nativeAd, View view) {
        nativeAd.unregisterView();
        NativeAdLayout nativeAdLayout = view.findViewById(R.id.fb_native_ad_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.facebook_native_ad_view, nativeAdLayout, false);

        AdIconView nativeAdIcon = adView.findViewById(R.id.fb_native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.fb_ad_headline);

        nativeAdTitle.setText(nativeAd.getAdvertiserName());

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdLayout);
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdIcon);

        nativeAd.registerViewForInteraction(
                adView,
                nativeAdIcon,
                clickableViews);

        nativeAdLayout.addView(adView);
    }

}
