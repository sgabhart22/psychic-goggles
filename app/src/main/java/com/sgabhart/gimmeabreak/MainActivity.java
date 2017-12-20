package com.sgabhart.gimmeabreak;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
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

    private ScrollView sv;
    private PuzzleDbHelper dbHelper;
    private int buttonWidth, buttonHeight;
    private ArrayList<Puzzle> puzzles;

    private RecyclerView rv;
    private PuzzleAdapter adapter;
    private RecyclerView.LayoutManager lm;
    private Context cx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cx = this;
        dbHelper = new PuzzleDbHelper(cx);
        puzzles = new ArrayList<>();
        puzzles = dbHelper.selectAll();

        // sv = (ScrollView)(findViewById(R.id.scrollView));


        rv = findViewById(R.id.recycle);
        rv.setHasFixedSize(true);

        lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);

        adapter = new PuzzleAdapter(puzzles);
        rv.setAdapter(adapter);

        rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv, new ClickListener() {
            @Override
            public void onClick(View v, int position) {
                int id = puzzles.get(position).getId();
                startPuzzle(id);
            }

            @Override
            public void onLongClick(View v, int position) {
                int id = puzzles.get(position).getId();
                Toast.makeText(cx, "ID of puzzle LONG clicked: " + id,
                        Toast.LENGTH_SHORT).show();
            }
        }));

        /*
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        buttonWidth = size.x;
        buttonHeight = size.y / 10;
        updateView();
        */

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbHelper.containsPuzzle()){
                    Toast.makeText(cx, "Today's puzzle already downloaded!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    new DownloadTask().execute(new UrlBuilder().getWordsUrl());
                }
            }
        });

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
    } // onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_cleanup:
                dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1);
                updateView();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateView(){
        puzzles = dbHelper.selectAll();
        adapter.setDataset(puzzles);
        adapter.notifyDataSetChanged();
    } // updateView


    public void startPuzzle(int id){
        Intent puzzleIntent = new Intent(this, PuzzleActivity.class);
        puzzleIntent.putExtra("id", id);
        startActivity(puzzleIntent);
    }

    /*
    private  class ButtonHandler implements View.OnClickListener{
        public void onClick(View v){
            int id = ((PuzzleButton) v).getId();

            startPuzzle(id);
        }
    }
    */

    public static interface ClickListener{
        public void onClick(View v, int position);
        public void onLongClick(View v, int position);
    } // ClickListener

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private ClickListener cl;
        private GestureDetector gd;

        public RecyclerTouchListener(Context cx, final RecyclerView rv, final ClickListener cl){
            this.cl = cl;
            gd = new GestureDetector(cx, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e){
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e){
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if(child != null && cl != null){
                        cl.onLongClick(child, rv.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e){
            View child = rv.findChildViewUnder(e.getX(), e.getY());

            if(child != null && cl != null && gd.onTouchEvent(e)){
                cl.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e){}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept){}

    } // RecyclerTouchListener

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
            updateView();
        }
    } // DownloadTask
}
