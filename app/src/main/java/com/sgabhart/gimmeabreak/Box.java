package com.sgabhart.gimmeabreak;

import java.io.Serializable;

public class Box implements Serializable {
    private static final char BLANK = ' ';

    private boolean circled, selected, locked, correct;
    private char response = BLANK;
    private char solution;
    private int wordNumber;


    public Box(char newSolution){
        this.solution = newSolution;
    }

    public Box(){ solution = BLANK; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Box other = (Box) obj;

        if (isCircled() != other.isCircled()) {
            return false;
        }

        if (getResponse() != other.getResponse()) {
            return false;
        }

        if (getSolution() != other.getSolution()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return this.getWordNumber() + this.getSolution() + " ";
    }

    /**
     * @return if the box is circled
     */
    public boolean isCircled() {
        return circled;
    }

    public boolean isSelected() { return selected; }

    public boolean isLocked(){ return locked; }

    public boolean isCorrect() { return correct; }

    /**
     * @param circled the circled to set
     */
    public void setCircled(boolean circled) {
        this.circled = circled;
    }

    public void setSelected(boolean selected) { this.selected = selected; }

    public void setLocked(boolean locked){ this.locked = locked; }

    public void setCorrect(boolean correct){ this.correct = correct; }

    /**
     * @return the response
     */
    public char getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(char response) {
        this.response = response;
    }

    /**
     * @return the solution
     */
    public char getSolution() {
        return solution;
    }

    /**
     * @param solution the solution to set
     */
    public void setSolution(char solution) {
        this.solution = solution;
    }

    /**
     * @return the wordNumber
     */
    public int getWordNumber() {
        return wordNumber;
    }

    /**
     * @param wordNumber the wordNumber to set
     */
    public void setwordNumber(int wordNumber) {
        this.wordNumber = wordNumber;
    }

    /**
     * @return if the current box is blank
     */
    public boolean isBlank() { return getResponse() == BLANK; }
}
