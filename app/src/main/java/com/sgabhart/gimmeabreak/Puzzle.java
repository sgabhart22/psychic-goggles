package com.sgabhart.gimmeabreak;

import android.content.Intent;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Puzzle {
    private int id;
    private String date;
    private ArrayList<String> words, answers;
    private byte[] image;
    private Box[][] boxes;
    private Box[] finalBoxes;
    private HashMap<String, ArrayList<Integer>> map = new HashMap<>();

    public Puzzle(int newId, String newDate, ArrayList<String> newWords,
                  ArrayList<String> newAnswers, byte[] newImage){
        id = newId;
        date = newDate;
        words = newWords;
        answers = newAnswers;
        image = newImage;

        boxes = new Box[words.size() - 1][6];
        finalBoxes = new Box[answers.get(answers.size() - 1).length()];

        makeMap();
    }

    public Puzzle(int newId, String newDate, ArrayList<String> newWords,
                  ArrayList<String> newAnswers, Bitmap newImage){
        id = newId;
        date = newDate;
        words = newWords;
        answers = newAnswers;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.PNG, 100, os);
        image = os.toByteArray();

        boxes = new Box[words.size() - 1][6];
        finalBoxes = new Box[answers.get(answers.size() - 1).length()];

        makeMap();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    public void setWords(ArrayList<String> words) {
        this.words = words;
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<String> answers) {
        this.answers = answers;
    }

    public String getDate(){
        return date;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] newImage) {
        image = newImage;
    }

    public Box[][] getBoxes(){ return boxes; }

    public Box[] getFinalBoxes(){ return finalBoxes; }

    private void makeMap(){
        // Create an iterator.
        ListIterator<String> it = answers.listIterator();

        // Bookkeeping variables
        String current = it.next();
        int count = 0;

        // This ArrayList will represent the locations of circled letters in each word.
        // That is, it is the "value" portion of each entry in the map.
        ArrayList<Integer> positions;

        // To check against letters in words.
        String finalLetters = words.get(words.size() - 1);

        // Finally, the starting position for our linear letter-checking.
        char testLetter = finalLetters.charAt(0);

        // While there are still circled letters to map
        while(count < finalLetters.length()){
            // Create a refernce to  new object each loop iteration
            positions = new ArrayList<>();

            // Check each letter in each word
            for (int i = 0; i < current.length(); i++) {
                if(current.charAt(i) == testLetter){
                    positions.add(i);

                    if(count + 1 == finalLetters.length()) break;
                    else testLetter = finalLetters.charAt(++count);
                }
            }

            // Create a dynamic key with the populated 'positions' ArrayList
            map.put("Word" + it.nextIndex(), positions);

            // If the iterator still has stuff, keep going. Break out otherwise.
            if(it.hasNext()) current = it.next();
            else break;
        }
    } // makeMap

    public HashMap<String, ArrayList<Integer>> getMap(){ return map; }

    @Override
    public String toString() {
        return "Puzzle{" +
                "id=" + id +
                '}';
    }
}

