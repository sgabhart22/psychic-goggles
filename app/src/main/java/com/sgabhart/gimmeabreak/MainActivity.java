package com.sgabhart.gimmeabreak;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class MainActivity extends AppCompatActivity {

    private TextView display;
    private ScrollView sv;
    private Context cx;
    private PuzzleDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = (TextView)(findViewById(R.id.text));
        cx = this;
        dbHelper = new PuzzleDbHelper(cx);

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException re) {
            System.err.println(re);
        } catch (GooglePlayServicesNotAvailableException nae) {
            System.err.println(nae);
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            SSLEngine engine = sslContext.createSSLEngine();
        } catch (NoSuchAlgorithmException nsa) {
            System.err.println(nsa);
        } catch (KeyManagementException km) {
            System.err.println(km);
        }

        if(dbHelper.containsPuzzle()){
            Toast.makeText(cx, "This puzzle already in database.", Toast.LENGTH_SHORT).show();
        } else {
            new DownloadTask().execute(new UrlBuilder().getWordsUrl());
        }
    }

    public void updateView(String s){
        display.setText(s);
    }

    private class DownloadTask extends AsyncTask<String, Void, ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(String... urls){
            ArrayList<String> words = new ArrayList<>();
            ArrayList<String> answers = new ArrayList<>();
            String url = urls[0];

            try {
                org.jsoup.nodes.Document wordDoc = Jsoup.connect(url).get();
                org.jsoup.select.Elements wordElements = wordDoc.select("button.word");

                // Collect jumbled words
                for (int i = 0; i < 5; i++) {
                    words.add(wordElements.get(i).text());
                }

            } catch (IOException e) {
                System.err.println();
            }

            // Now get solved words
            for (String s:
                    words) {
                url = new UrlBuilder().getAnswerUrl(s);
                try{
                    Document answerDoc = Jsoup.connect(url).get();
                    Element answerElement = answerDoc.select("button.word").first();
                    answers.add(answerElement.text());
                } catch (IOException e) {
                    System.err.println(e);
                }
            }

            // Finally, get cartoon image
            try {
                Document openingDoc = Jsoup.connect(new UrlBuilder().IMAGE_BASE_URL).get();
                Elements linkElements = openingDoc.select("a");
                String cartoonUrl = "No link", srcUrl = "No source";
                Map<String, String> cookies = new HashMap<>();

                for (Element e :
                        linkElements) {
                    if (e.text().equals("jumble")) {
                        cartoonUrl = e.attr("abs:href");
                        break;
                    }
                }

                Document finalDoc = Jsoup.connect(cartoonUrl).get();
                Elements picElements = finalDoc.select("img.img-responsive");

                for (Element e :
                        picElements) {
                    if (e.attr("alt").equals("jumble")) {
                        srcUrl = e.attr("src");
                        break;
                    }
                }

                // Cartoon image as byte array
                byte[] rawBytes = Jsoup.connect(srcUrl).ignoreContentType(true).execute().bodyAsBytes();


                // Crop cartoon
                Bitmap bmp = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);
                Bitmap cropped = Bitmap.createBitmap(bmp, 245, 50, 240, 368);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                cropped.compress(Bitmap.CompressFormat.PNG, 100, os);
                byte[] bytes = os.toByteArray();

                // Put information in db
                PuzzleDbHelper dbHelper = new PuzzleDbHelper(cx);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues cv = new ContentValues();
                cv.put(PuzzleContract.DailyPuzzle.DATE, new UrlBuilder().getFormattedDate());
                cv.put(PuzzleContract.DailyPuzzle.WORD1, words.get(0));
                cv.put(PuzzleContract.DailyPuzzle.WORD2, words.get(1));
                cv.put(PuzzleContract.DailyPuzzle.WORD3, words.get(2));
                cv.put(PuzzleContract.DailyPuzzle.WORD4, words.get(3));
                cv.put(PuzzleContract.DailyPuzzle.ANSWER1, answers.get(0));
                cv.put(PuzzleContract.DailyPuzzle.ANSWER2, answers.get(1));
                cv.put(PuzzleContract.DailyPuzzle.ANSWER3, answers.get(2));
                cv.put(PuzzleContract.DailyPuzzle.ANSWER4, answers.get(3));
                cv.put(PuzzleContract.DailyPuzzle.FINAL_WORD, words.get(4));
                cv.put(PuzzleContract.DailyPuzzle.FINAL_ANSWER, answers.get(4));
                cv.put(PuzzleContract.DailyPuzzle.IMAGE, bytes);

                long newRowId = db.insert(PuzzleContract.DailyPuzzle.TABLE_NAME, null, cv);
                Log.w("New row ID", "" + newRowId);


            } catch (IOException e){
                System.err.println(e);
            }


            return answers;
        }

        @Override
        protected void onPostExecute(ArrayList<String> answers){
            updateView(answers.get(0));
        }
    } // DownloadTask
}
