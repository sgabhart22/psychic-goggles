package com.sgabhart.gimmeabreak;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sgabhart.gimmeabreak.PuzzleContract;
import com.sgabhart.gimmeabreak.UrlBuilder;

import java.lang.reflect.Array;
import java.sql.Blob;
import java.util.ArrayList;

/**
 * Created by Admin on 10/12/2017.
 */

public class PuzzleDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Puzzle.db";

    private static final String SQL_CREATE_DAILIES =
            "CREATE TABLE " + PuzzleContract.DailyPuzzle.TABLE_NAME + " (" +
                    PuzzleContract.DailyPuzzle._ID + " INTEGER PRIMARY KEY," +
                    PuzzleContract.DailyPuzzle.DATE + " TEXT," +
                    PuzzleContract.DailyPuzzle.WORD1 + " TEXT," +
                    PuzzleContract.DailyPuzzle.WORD2 + " TEXT," +
                    PuzzleContract.DailyPuzzle.WORD3 + " TEXT," +
                    PuzzleContract.DailyPuzzle.WORD4 + " TEXT," +
                    PuzzleContract.DailyPuzzle.ANSWER1 + " TEXT," +
                    PuzzleContract.DailyPuzzle.ANSWER2 + " TEXT," +
                    PuzzleContract.DailyPuzzle.ANSWER3 + " TEXT," +
                    PuzzleContract.DailyPuzzle.ANSWER4 + " TEXT," +
                    PuzzleContract.DailyPuzzle.FINAL_WORD + " TEXT," +
                    PuzzleContract.DailyPuzzle.FINAL_ANSWER + " TEXT," +
                    PuzzleContract.DailyPuzzle.IMAGE + " BLOB)";

    private static final String SQL_DELETE_DAILIES =
            "DROP TABLE IF EXISTS " + PuzzleContract.DailyPuzzle.TABLE_NAME;

    public PuzzleDbHelper(Context cx){
        super(cx, DATABASE_NAME, null, DATABASE_VERSION);
    } // constructor

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_DAILIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_DAILIES);
        db.execSQL(SQL_CREATE_DAILIES);
    }

    public Puzzle selectById(int puzzleId){
        Puzzle p;

        String sqlSelect = "SELECT * FROM " + PuzzleContract.DailyPuzzle.TABLE_NAME +
                " WHERE " + PuzzleContract.DailyPuzzle._ID + " = " + puzzleId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sqlSelect, null);
        cursor.moveToFirst();

        int id = cursor.getInt(cursor.getColumnIndex(PuzzleContract.DailyPuzzle._ID));
        String date = cursor.getString(cursor.getColumnIndex(PuzzleContract.DailyPuzzle.DATE));

        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        for (int i = 2; i < 6; i++) {
            words.add(cursor.getString(i));
            answers.add(cursor.getString(i+4));
        }
        words.add(cursor.getString(cursor.getColumnIndex(PuzzleContract.DailyPuzzle.FINAL_WORD)));
        answers.add(cursor.getString(cursor.getColumnIndex(PuzzleContract.DailyPuzzle.FINAL_ANSWER)));

        byte[] image = cursor.getBlob(cursor.getColumnIndex(PuzzleContract.DailyPuzzle.IMAGE));

        p = new Puzzle(id, date, words, answers, image);

        return p;
    }

    public ArrayList<Puzzle> selectAll(){
        ArrayList<Puzzle> puzzles = new ArrayList<>();
        int id;
        String date;
        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();

        String sqlQuery = "select * from " + PuzzleContract.DailyPuzzle.TABLE_NAME;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        while(cursor.moveToNext()) {

            id = Integer.parseInt(cursor.getString(0));
            date = cursor.getString(1);

            for (int i = 2; i < 6; i++) {
                words.add(cursor.getString(i));
                answers.add(cursor.getString(i+4));
            }

            words.add(cursor.getString(10));
            answers.add(cursor.getString(11));
            byte[] image = cursor.getBlob(cursor.getColumnIndex(PuzzleContract.DailyPuzzle.IMAGE));

            Puzzle currentPuzzle = new Puzzle(id, date, words, answers, image);
            puzzles.add(currentPuzzle);

            words.clear();
            answers.clear();
        }

        db.close();
        return puzzles;
    }

    public boolean containsPuzzle(){
        String sqlSelect = "SELECT * FROM " + PuzzleContract.DailyPuzzle.TABLE_NAME + " WHERE " +
                PuzzleContract.DailyPuzzle.DATE + " = '" + new UrlBuilder().getFormattedDate() +
                "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sqlSelect, null);

        if(cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }
    }
}
