package com.sgabhart.gimmeabreak;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Field implements Serializable{

    private Puzzle puzzle;
    private ArrayList<Rect> labelRects, answerRects, finalRects;
    private Position selected = new Position(0, 0);
    private Box[][] boxes;
    private Box[] finalBoxes;
    private String responder;
    private HashMap<String, ArrayList<Integer>> map = new HashMap<>();
    private int width, height, sideLength, leftMargin;
    private Paint labelPaint, boxPaint, circlePaint, textPaint, selectedPaint, correctPaint;
    private Rect imageRect, background;
    private Bitmap cartoon;
    private Bitmap bitmap;

    // Dummy words/answers
    ArrayList<String> words = new ArrayList<>();
    // String[] tempWords = {"PHECR", "BLAFE", "RASPIN", "VIRTHE", "PEBESRNTE"};
    ArrayList<String> answers = new ArrayList<>();
    // String[] tempAnswers = {"PERCH", "FABLE", "SPRAIN", "THRIVE", "BE PRESENT"};
    // String testFormatAnswer = "\"HILL-BILLIES\"";

    public Field(Puzzle puzzle, int width, int height){
        this.puzzle = puzzle;
        selected = new Position(0, 0);

        this.width = width;
        this.height = height;
        leftMargin = (int)(width * .1);

        map = puzzle.getMap();
        boxes = puzzle.getBoxes();

        words = puzzle.getWords();
        answers = puzzle.getAnswers();

        // Create Paint for jumbled word containers
        labelPaint = new Paint();
        labelPaint.setColor(Color.GRAY);
        labelPaint.setAntiAlias(true);
        labelPaint.setStrokeWidth(5.0f);
        labelPaint.setStyle(Paint.Style.STROKE);

        // Create Paint for answer boxes
        boxPaint = new Paint();
        boxPaint.setColor(Color.BLACK);
        boxPaint.setAntiAlias(true);
        boxPaint.setStrokeWidth(5.0f);
        boxPaint.setStyle(Paint.Style.STROKE);

        // Create Paint for highlighted letter
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.parseColor("#FFAE57"));

        // Create Paint for correct boxes
        correctPaint = new Paint();
        correctPaint.setColor(Color.parseColor("#7AF9BE"));

        // Create a Paint for circled boxes
        circlePaint = new Paint();
        circlePaint.setColor(Color.YELLOW);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(5.0f);
        circlePaint.setStyle(Paint.Style.FILL);

        // Create Paint for lettering
        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(50.0f);
        textPaint.setTypeface(Typeface.SANS_SERIF);

        // Instantiate array Rectangles representing word containers
        labelRects = new ArrayList<>();
        double yLabel = .05;
        int startLabelX = leftMargin;
        int endLabelX = (int)(width * .30);

        // Similar, but for answer boxes
        answerRects = new ArrayList<>();
        double yBox = .1;
        int startBox = leftMargin;
        sideLength = (int)(height * .13 - height * .1);
        int endBox = leftMargin + sideLength;

        // Create Rectangle to house cartoon bitmap
        imageRect = new Rect((int)(width * .55), (int)(height * .05), (int)(width * .95), (int)(height * .5));
        byte[] rawImage = puzzle.getImage();
        cartoon = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
        Log.w("From Field constructor", "cartoon config " + cartoon.getConfig());

        // Placeholder Collection to set circled letters
        ArrayList<Integer> circled = new ArrayList<>();

        // Create containers and answer boxes for each word in the puzzle
        for (int i = 0; i < words.size() - 1; i++) {
            labelRects.add(new Rect(startLabelX, (int)(height * yLabel), endLabelX, (int)(height * (yLabel + .03))));

            circled = map.get("Word" + (i + 1));

            String currentWord = answers.get(i);
            for(int j = 0; j < currentWord.length(); j++){
                boxes[i][j] = new Box(currentWord.charAt(j));
                if(circled.contains(j)){
                    boxes[i][j].setCircled(true);
                }
            }


            for (int k = 0; k < boxes[i].length; k++){
                answerRects.add(new Rect(startBox, (int)(height * yBox), endBox, (int)(height * (yBox + .03))));
                startBox += sideLength;
                endBox += sideLength;
            }


            yLabel += .10;
            yBox += .10;
            startBox = leftMargin;
            endBox = startBox + sideLength;
        } // for

        String finalAnswer = answers.get(answers.size() - 1);

        // Format answer if need be
        if(finalAnswer.contains("\'\'")){
            finalAnswer = finalAnswer.replaceAll("\'\'", "\"");
        }
        finalBoxes = new Box[finalAnswer.length()];

        // Finally, for the final answer boxes
        finalRects = new ArrayList<>();
        int answerRightMargin = (int)(width * .95);
        int answerLeftMargin = answerRightMargin - (getFinalBoxes().length * sideLength);
        double yAnswerBox = .55;

        for(int i = 0; i < finalAnswer.length(); i++){
            finalBoxes[i] = new Box(finalAnswer.charAt(i));
            finalBoxes[i].setCircled(true);

            char testChar = finalBoxes[i].getSolution();
            if(testChar == '\"' || testChar == '-' || testChar == ' '){
                finalBoxes[i].setLocked(true);
            }

            finalRects.add(new Rect(answerLeftMargin, (int)(height * yAnswerBox),
                    answerLeftMargin + sideLength, (int)(height * (yAnswerBox + .03))));

            answerLeftMargin += sideLength;
        } // for

        boxes[selected.x][selected.y].setSelected(true);
    } // Constructor

    public Bitmap draw(){

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        int i = 0;
        for (Rect r:
                labelRects) {
            canvas.drawRect(r, labelPaint);
            canvas.drawText(words.get(i).toUpperCase(), r.left + 5, r.bottom - 10, textPaint);
            i++;
        }

        // Draw answer boxes from Box[][] boxes
        for(int j = 0; j < boxes.length; j++){
            for(int k = 0; k < boxes[j].length; k++){

                Box b = boxes[j][k];

                if(b != null){
                    Rect r = answerRects.get((j * 6) + k);

                    if(b.isSelected()){
                        canvas.drawRect(r, selectedPaint);
                    }

                    if(b.isCorrect()){
                        canvas.drawRect(r, correctPaint);
                    }

                    if(b.isCircled()){
                        canvas.drawRect(r, boxPaint);
                        canvas.drawCircle(r.exactCenterX(),
                                r.exactCenterY(), sideLength / 2, boxPaint);
                    }
                    else {
                        canvas.drawRect(r, boxPaint);
                    }

                    if(b.getResponse() != ' '){
                        canvas.drawText("" + b.getResponse(), r.exactCenterX() - (int)(sideLength * .3),
                                r.exactCenterY() + (int)(sideLength * .3), textPaint);
                    }
                }
            }
        }

        // Draw final answer boxes
        for(i = 0; i < finalBoxes.length; i++){
            Box b = finalBoxes[i];
            Rect r = finalRects.get(i);

            if(b.isSelected()){
                canvas.drawRect(r, selectedPaint);
            }

            if(b.isCorrect()){
                canvas.drawRect(r, correctPaint);
            }

            // Determine non-letter characters in final answer
            if(b.getSolution() == '\"'){
                canvas.drawText("\"", r.exactCenterX(),
                        r.exactCenterY(), textPaint);
            } else if(b.getSolution() == '-'){
                canvas.drawText("-", r.exactCenterX(),
                        r.exactCenterY() + 10.0f, textPaint);
            } else if(b.getSolution() != ' '){
                canvas.drawRect(r, boxPaint);
                canvas.drawCircle(r.exactCenterX(), r.exactCenterY(),
                        sideLength / 2, boxPaint);

                if(b.getResponse() != ' '){
                    canvas.drawText("" + b.getResponse(), r.exactCenterX() - (int)(sideLength * .3),
                            r.exactCenterY() + (int)(sideLength * .3), textPaint);
                }
            }

        }

        // Draw cartoon
        canvas.drawBitmap(cartoon, null, imageRect, new Paint());

        return bitmap;
    } // draw

    public Box findBox(int x, int y){
        Box box;
        int l, r, t, b, i = 0;
        boolean located = false;

        // Search main field
        for(Rect rec : answerRects){
            l = (int)(rec.left);
            r = (int)(rec.right);
            t = (int)(rec.top);
            b = (int)(rec.bottom);

            if(x > l && x < r && y > t && y < b){
                located = true;
                break;
            }

            i++;
        } // for each

        // Box was found in first 4 (or 6) words
        if(located) {
            int word = i / 6;
            int letter = i % 6;
            box = boxes[word][letter];

            Position previous = selected;
            if(previous.x == 7){
                finalBoxes[previous.y].setSelected(false);
            } else {
                boxes[previous.x][previous.y].setSelected(false);
            }

            this.setSelected(new Position(word, letter));
            box.setSelected(!(box.isSelected()));
            return box;
        } else {
            i = 0;

            // Search final answer
            for(Rect rec : finalRects){
                l = (int)(rec.left);
                r = (int)(rec.right);
                t = (int)(rec.top);
                b = (int)(rec.bottom);

                if(x > l && x < r && y > t && y < b){
                    located = true;
                    break;
                }

                i++;
            } // for

            // Box was found in final answer
            if(located){
                box = finalBoxes[i];

                if(!box.isLocked()){
                    Position previous = selected;

                    if(previous.x == 7){
                        finalBoxes[previous.y].setSelected(false);
                    } else {
                        boxes[previous.x][previous.y].setSelected(false);
                    }

                    this.setSelected(new Position(7, i));
                    box.setSelected(!(box.isSelected()));
                    return box;
                }

                return new Box();

            } else return new Box();
        }
    } // findBox

    public ArrayList<Rect> getLabelRects() {
        return labelRects;
    }

    public ArrayList<Rect> getAnswerRects() {
        return answerRects;
    }

    public ArrayList<Rect> getFinalRects() {
        return finalRects;
    }

    public Paint getLabelPaint() {
        return labelPaint;
    }

    public Paint getBoxPaint() {
        return boxPaint;
    }

    public Paint getCirclePaint() {
        return circlePaint;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public Paint getSelectedPaint() {
        return selectedPaint;
    }

    public Box[] getFinalBoxes() {
        return finalBoxes;
    }

    public Rect getImageRect() {
        return imageRect;
    }

    public Bitmap getCartoon() {
        return cartoon;
    }

    public int getSideLength() {
        return sideLength;
    }

    public void setLabelRects(ArrayList<Rect> newLabelRects){ this.labelRects = newLabelRects; }

    public void setAnswerRects(ArrayList<Rect> newAnswerRects){ this.answerRects = newAnswerRects; }

    public void setFinalRects(ArrayList<Rect> newFinalRects){ this.finalRects = newFinalRects; }

    public Box getCurrentBox() {
        return this.boxes[this.selected.x][this.selected.y];
    }

    public boolean checkWord(int wordNum){
        String answer;
        StringBuilder word = new StringBuilder();

        if(wordNum == 7){
            answer = answers.get(answers.size() - 1);

            for (int i = 0; i < finalBoxes.length; i++) {
                Box b = finalBoxes[i];

                word.append(b.getResponse());
            } // for
        } else {
            answer = answers.get(wordNum);

            for(int i = 0; i < boxes[wordNum].length; i++){
                Box b = boxes[wordNum][i];

                if(b != null){
                    word.append(b.getResponse());
                }
            } // for
        }

        if(word.toString().equals(answer.toUpperCase())) return true;
        else return false;
    } // checkWord

    public void lockWord(int wordNum){
        Box[] word;

        if(wordNum == 7){
            word = finalBoxes;
        } else {
            word = boxes[wordNum];
        }

        for(Box b : word){
            if(b != null){
                if(!b.isLocked()){
                    b.setLocked(true);
                    b.setCorrect(true);
                }
            }
        }

    }

    public void advance(){

        if(selected.x == 7){

            if(selected.y < finalBoxes.length - 1){
                Box b = finalBoxes[selected.y];
                b.setSelected(false);
                selected.y++;

                while(finalBoxes[selected.y].isLocked() && selected.y < finalBoxes.length){
                    selected.y++;
                }
            }

        } else {
            Box b = boxes[selected.x][selected.y];
            b.setSelected(false);

            if(selected.y < boxes[selected.x].length - 1){

                selected.y++;
                if(boxes[selected.x][selected.y] == null){
                    selected.y = 0;
                    selected.x++;
                }

            } else if(selected.y == boxes[selected.x].length - 1){
                if(selected.x < boxes.length - 1){
                    selected.x++;
                    selected.y = 0;
                } else if (selected.x == boxes.length - 1){
                    selected.x = 7;
                    selected.y = 0;
                }
            }
        }




        if(selected.x == 7){
            finalBoxes[selected.y].setSelected(true);
        } else {
            boxes[selected.x][selected.y].setSelected(true);
        }
    } // advance

    public void space(){
        Box b = new Box();

        if(selected.x == 7){
            b = finalBoxes[selected.y];
        } else {
            b = boxes[selected.x][selected.y];
        }

        if(b.getResponse() != ' '){
            b.setResponse(' ');
            advance();
        } else {
            advance();
        }
    }

    public void deleteLetter(){
        Box b = new Box();

        if(selected.x == 7){
            b = finalBoxes[selected.y];
        } else {
            b = boxes[selected.x][selected.y];
        }

        if(b.getResponse() != ' '){
            b.setResponse(' ');
            selected = moveToPrevious();
        } else {
            selected = moveToPrevious();
        }
    } // deleteLetter

    public Position moveToPrevious(){
        Position current = new Position(selected.x, selected.y);

        if(current.x == 7){
            if(current.y == 0){
                current.x = boxes.length - 1;
                current.y = boxes[current.x].length - 1;

                finalBoxes[selected.y].setSelected(false);
                boxes[current.x][current.y].setSelected(true);
            } else {
                current.y--;

                if(finalBoxes[current.y].isLocked()){
                    current.y--;
                }

                finalBoxes[selected.y].setSelected(false);
                finalBoxes[current.y].setSelected(true);
            }

            return current;
        } else {
            if(current.y == 0){
                if(current.x == 0){
                    return current;
                } else {
                    current.x--;

                    int y = boxes[current.x].length - 1;
                    if(boxes[current.x][y] == null){
                        current.y = boxes[current.x].length - 2;
                    } else {
                        current.y = y;
                    }
                }

                boxes[selected.x][selected.y].setSelected(false);
                boxes[current.x][current.y].setSelected(true);
            } else {
                current.y--;

                boxes[selected.x][selected.y].setSelected(false);
                boxes[current.x][current.y].setSelected(true);
            }
        }

        return current;
    } // moveToPrevious

    public Box[][] getBoxes(){ return boxes; }

    public Word getCurrentWord() {
        Word w = new Word();
        w.start = this.getCurrentWordStart();
        w.length = this.getWordRange();

        return w;
    }

    public Position getCurrentWordStart(){
        return new Position(this.selected.x, 0);
    }

    public int getWordRange(Position start){
        Box[] wordBoxes = this.getBoxes()[start.x];
        int range = 0;

        for(Box b: wordBoxes){
            if(b != null) range++;
        }

        return range;
    }

    public int getWordRange() {
        return getWordRange(this.getCurrentWordStart());
    }

    public Position getSelected(){ return selected; }

    public void setSelected(Position selected){ this.selected = selected; }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getResponder() {
        return responder;
    }

    public Puzzle getPuzzle(){ return  puzzle; }


    public static class Position implements Serializable {
        public int x; // Word #
        public int y; // Letter #

        protected Position(){
        }

        public Position(int x, int y) {
            this.y = y;
            this.x = x;
        }


        @Override
        public boolean equals(Object o) {
            if ((o == null) || (o.getClass() != this.getClass())) {
                return false;
            }

            Position p = (Position) o;

            return ((p.y == this.y) && (p.x == this.x));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new int[] {x, y});
        }

        @Override
        public String toString() {
            return "[" + this.x + " x " + this.y + "]";
        }
    } // Position

    public static class Word implements Serializable {
        public Position start;
        public int length;

        public boolean checkInWord(int x) {
            int ranging = x;
            int startPos = start.x;

            return (startPos <= ranging && ((startPos + length) > ranging));
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != Word.class) {
                return false;
            }

            Word check = (Word) o;

            return check.start.equals(this.start) && (check.length == this.length);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = (29 * hash) + ((this.start != null) ? this.start.hashCode() : 0);
            hash = (29 * hash) + this.length;

            return hash;
        }
    }
}
