package com.sgabhart.gimmeabreak;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UrlBuilder {
    final String WORD_BASE_URL = "http://www.jumblesolver.com/daily-jumble-answers/";
    final String ANSWER_BASE_URL = "http://www.jumblesolver.com/daily/";
    final String IMAGE_BASE_URL = "http://poststar.com/entertainment/puzzles-and-comics/";

    private Date date = new Date();
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    // Default constructor
    public UrlBuilder(){}

    public UrlBuilder(Date newDate){
        date = newDate;
    }

    public String getWordsUrl(){
        return WORD_BASE_URL + df.format(date);
    }

    public String getAnswerUrl(String word){
        return ANSWER_BASE_URL + word;
    }

    public String getFormattedDate(){
        return df.format(date);
    }
}
