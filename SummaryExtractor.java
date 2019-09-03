package com.example.summarizer3.LexRankSummarizer;

public interface SummaryExtractor {
    /**
     * @return Sentence[] the summary
     * @see Sentence
     */
    public Sentence[] getSummary();
}
