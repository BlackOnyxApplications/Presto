package com.black_onyx.Presto.wordsearch.data;

import com.black_onyx.Presto.wordsearch.model.Word;

import java.util.List;

/**
 * Created by abdularis on 18/07/17.
 */

public interface WordDataSource {

    List<Word> getWords();

}
