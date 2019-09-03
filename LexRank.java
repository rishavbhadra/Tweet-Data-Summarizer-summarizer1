package com.example.summarizer3.LexRankSummarizer;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LexRank implements SummaryExtractor {
    private static final int MAX_ITERATIONS = 30;
    private static final double DAMPING_FACTOR = 0.85;
    private static final double ERROR_THRESHOLD = 0.0001;
    private static int SUMMARY_LENGTH;

    private final Sentence[] sentences;

    private final Graph<Sentence> graph = new Graph<>();
    private final Map<Sentence, Map<String, Integer>> tfs = new HashMap<>();

    public LexRank(Sentence[] sentences,int sum_size) {
        this.sentences = sentences;
        SUMMARY_LENGTH=sum_size;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Sentence[] getSummary() {
        init(sentences);

        for (Sentence sentence : sentences) {
            graph.add(sentence, new SentenceNode(sentence));
        }
        graph.linkNodes();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // check if rank calculation has converged
            if (graph.getNodeStream().reduce(false, (a, n) -> a || n.calculateRank(),
                    Boolean::logicalOr)) {
                break;
            }
        }

        return graph.getRankedNodes().limit(SUMMARY_LENGTH).map(n -> n.getContent())
                .toArray(Sentence[]::new);
    }

    private void init(Sentence[] sentences) {
        for (Sentence sentence : sentences) {
            Map<String, Integer> tf = new HashMap<>();
            for (String word : sentence.getWords()) {
                Integer frequency = tf.get(word);
                tf.put(word, frequency == null ? 1 : frequency + 1);
            }
            tfs.put(sentence, tf);
        }
    }

    private class SentenceNode extends Graph<Sentence>.Node {

        public SentenceNode(Sentence s) {
            graph.super(s);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public boolean calculateRank() {
            double rank = neighbors.stream().reduce(
                    0.0,
                    (aj, j) -> aj
                            + j.getWeight()
                            * j.getTarget().getRank()
                            / j.getTarget().getNeighbors().stream()
                            .reduce(0.0, (ak, k) -> ak + k.getWeight(), Double::sum),
                    Double::sum);
            rank *= DAMPING_FACTOR;
            rank += 1 - DAMPING_FACTOR;

            boolean converged = Math.abs(this.rank - rank) < ERROR_THRESHOLD;

            this.rank = rank;

            return converged;
        }

        @Override
        public double calculateRelationScore(Graph<Sentence>.Node other) {
            Map<String, Integer> xtf = tfs.get(getContent());
            Map<String, Integer> ytf = tfs.get(other.getContent());

            Set<String> union = new HashSet<>();
            union.addAll(xtf.keySet());
            union.addAll(ytf.keySet());

            double x2 = 0.0;
            double y2 = 0.0;
            double xy = 0.0;

            for (String word : union) {
                Integer xfreq = xtf.get(word);
                Integer yfreq = ytf.get(word);
                double x = xfreq != null ? xfreq : 0.0;
                double y = yfreq != null ? yfreq : 0.0;

                x2 += x * x;
                y2 += y * y;
                xy += x * y;
            }

            return xy / Math.sqrt(x2 * y2);
        }
    }
}
