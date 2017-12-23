package com.sgabhart.gimmeabreak;

import org.junit.Test;

/**
 * Created by spencer on 12/22/17.
 */

public class FormatAnswerTest {
    @Test
    public void apostrophes_as_quotes() throws Exception{
        String testAnswer = "\'\'KOALA\'\'-TY TIME";
        String result = "";

        if(testAnswer.contains("\'\'")){
            result = testAnswer.replaceAll("\'\'", "\"");
        }

        System.out.println(result);
    } // apostrophes_as_quotes
}
