package com.sgabhart.gimmeabreak;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;

public class PuzzleActivity extends AppCompatActivity {

    private int width, height, puzzleId;
    private Point size;
    private Puzzle puzzle;
    private PuzzleDbHelper dbHelper;
    private Field field;
    private ScrollingImageView fieldView;
    private Keyboard kb;
    private KeyboardView kbView;
    private View.OnKeyListener onKeyListener;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        Intent intent = getIntent();
        puzzleId = intent.getIntExtra("id", 0);

        dbHelper = new PuzzleDbHelper(this);
        puzzle = dbHelper.selectById(puzzleId);

        size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        if(size.x > size.y){
            height = size.x;
            width = size.y;
        } else {
            width = size.x;
            height = size.y;
        }

        handler = new Handler();
        field = new Field(puzzle, width, height);
        fieldView = (ScrollingImageView)(findViewById(R.id.field));

        this.registerForContextMenu(fieldView);
        fieldView.setContextMenuListener(new ScrollingImageView.ClickListener() {
            @Override
            public void onContextMenu(final ScrollingImageView.Point e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Box b = field.findBox(e.x, e.y);
                            fieldView.setBitmap(field.draw());
                            PuzzleActivity.this.openContextMenu(fieldView);
                        } catch(Exception e){
                            Log.e("PuzzleActivity", "From onContextMenu: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onTap(ScrollingImageView.Point e) {
                Box b = field.findBox(e.x, e.y);
                fieldView.setBitmap(field.draw());
            }
        });


        onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return event.getAction() == KeyEvent.ACTION_UP;
            }
        };

        kb = new Keyboard(this, R.xml.keyboard);
        kbView = (KeyboardView)(findViewById(R.id.keyboard));
        kbView.setOnKeyListener(onKeyListener);
        kbView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {}

            @Override
            public void onRelease(int primaryCode) {}

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, primaryCode);
                PuzzleActivity.this.onKeyUp(primaryCode, event);
            }

            @Override
            public void onText(CharSequence text) {}

            @Override
            public void swipeLeft() {}

            @Override
            public void swipeRight() {}

            @Override
            public void swipeDown() {}

            @Override
            public void swipeUp() {}
        });

        kbView.setKeyboard(kb);

        fieldView.setBitmap(field.draw());
    } // onCreate

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        if(size.x > size.y){
            height = size.x;
            width = size.y;
        } else {
            width = size.x;
            height = size.y;
        }

        kb = new Keyboard(this, R.xml.keyboard);
        kbView = (KeyboardView)(findViewById(R.id.keyboard));
        kbView.setOnKeyListener(onKeyListener);
        kbView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {}

            @Override
            public void onRelease(int primaryCode) {}

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, primaryCode);
                PuzzleActivity.this.onKeyUp(primaryCode, event);
            }

            @Override
            public void onText(CharSequence text) {}

            @Override
            public void swipeLeft() {}

            @Override
            public void swipeRight() {}

            @Override
            public void swipeDown() {}

            @Override
            public void swipeUp() {}
        });

        kbView.setKeyboard(kb);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){

        if(keyCode == 67){
            Log.w("Click, PuzzleActivity", "Delete clicked.");
            field.deleteLetter();
        } else if(keyCode == 62){
            Log.w("Click, PuzzleActivity","Space clicked.");
            field.space();
        } else {
            char c = Character.toUpperCase((char)(keyCode));
            Log.w("PuzzleActivity onKeyUp", "Key pressed: " + c);

            Field.Position selected = field.getSelected();

            if(selected.x == 7){
                field.getFinalBoxes()[selected.y].setResponse(c);
            } else {
                field.getBoxes()[selected.x][selected.y].setResponse(c);
            }

            boolean correct = field.checkWord(selected.x);

            if(correct){
                field.lockWord(selected.x);
            }

            field.advance();
        }

        fieldView.setBitmap(field.draw());

        return  super.onKeyUp(keyCode, event);
    } // onKeyUp



}
