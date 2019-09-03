package com.example.summarizer3.LexRankSummarizer;

import android.os.Build;
import android.support.annotation.RequiresApi;

public class LexSummary {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String LexSummarygetter(String s,int size)
    {
        String s1="";
        LexRank l=new LexRank(Parser.parseSentences(s),size);
        Sentence[] summary=l.getSummary();

        for(Sentence s2:summary)
        {
            s1=s1+"\n\n"+s2;
        }

        return s1;


    }
}
