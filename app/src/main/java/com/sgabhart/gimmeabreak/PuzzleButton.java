package com.sgabhart.gimmeabreak;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;

public class PuzzleButton extends AppCompatButton {
    private Puzzle puzzle;

    public PuzzleButton(Context cx){
        super(cx);
    }

    public PuzzleButton (Context cx, Puzzle newCandy){
        super(cx);
        puzzle = newCandy;
    }

    public int getId(){
        return puzzle.getId();
    }
}
