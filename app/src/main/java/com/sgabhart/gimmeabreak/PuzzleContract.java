package com.sgabhart.gimmeabreak;

import android.provider.BaseColumns;

/**
 * Created by Admin on 10/12/2017.
 */

public final class PuzzleContract {

    private PuzzleContract() {}

    public static class DailyPuzzle implements BaseColumns{
        public static final String TABLE_NAME = "daily";
        public static final String DATE = "date";
        public static final String WORD1 = "word1";
        public static final String WORD2 = "word2";
        public static final String WORD3 = "word3";
        public static final String WORD4 = "word4";
        public static final String ANSWER1 = "answer1";
        public static final String ANSWER2 = "answer2";
        public static final String ANSWER3 = "answer3";
        public static final String ANSWER4 = "answer4";
        public static final String FINAL_WORD = "finalWord";
        public static final String FINAL_ANSWER = "finalAnswer";
        public static final String IMAGE = "image";
    }

    public static class SundayPuzzle extends DailyPuzzle implements BaseColumns{
        public static final String TABLE_NAME = "sunday";
        private static final String WORD5 = "word5";
        private static final String WORD6 = "word6";
        private static final String ANSWER5 = "answer5";
        private static final String ANSWER6 = "answer6";
    }
}
