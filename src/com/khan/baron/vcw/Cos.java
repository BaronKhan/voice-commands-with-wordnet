//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.khan.baron.vcw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.GlossFinder.SuperGloss;
import edu.cmu.lti.ws4j.util.OverlapFinder;
import edu.cmu.lti.ws4j.util.OverlapFinder.Overlaps;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

//Custom Cosine Similarity class (altered from FastLesk code)
public class Cos extends RelatednessCalculator {
    protected static double min = 0.0D;
    protected static double max = 1.7976931348623157E308D;
    private static String[] pairs = new String[]{"    :    ",
            "    :hype", "    :hypo", "    :mero", "    :holo", "hype:    ", "hype:hype",
            "hype:hypo", "hype:mero", "hype:holo", "hypo:    ", "hypo:hype", "hypo:hypo",
            "hypo:mero", "hypo:holo", "mero:    ", "mero:hype", "mero:hypo", "mero:mero",
            "mero:holo", "syns:    ", "syns:hype", "syns:hypo", "syns:mero", "syns:holo"};
    private static List<POS[]> posPairs = new ArrayList<POS[]>() {
        {
            this.add(new POS[]{POS.n, POS.n});
            this.add(new POS[]{POS.v, POS.v});
            this.add(new POS[]{POS.a, POS.a});
            this.add(new POS[]{POS.r, POS.r});
            this.add(new POS[]{POS.n, POS.v});
            this.add(new POS[]{POS.v, POS.n});
        }
    };

    public Cos(ILexicalDatabase db) {
        super(db);
    }

    protected Relatedness calcRelatedness(Concept synset1, Concept synset2) {
        if (synset1 != null && synset2 != null) {
            StringBuilder tracer = new StringBuilder();
            final List<SuperGloss> glosses = this.getSuperGlosses(synset1, synset2);
            int score = 0;

            for(int i = 0; i < min(glosses.size(), 1); ++i) {
                SuperGloss sg = (SuperGloss)glosses.get(i);
                double functionsScore = this.calcFromSuperGloss(sg.gloss1, sg.gloss2);
                functionsScore *= ((SuperGloss)glosses.get(i)).weight;
                score = (int)((double)score + functionsScore);
            }

            return new Relatedness((double)score, tracer.toString(), (String)null);
        } else {
            return new Relatedness(min);
        }
    }

    private double calcFromSuperGloss(List<String> glosses1, List<String> glosses2) {
        double max = 0.0D;
        Iterator i = glosses1.iterator();

        while(i.hasNext()) {
            String gloss1 = (String)i.next();
            Iterator j = glosses2.iterator();

            while(j.hasNext()) {
                String gloss2 = (String)j.next();
                double score = this.calcFromSuperGloss(gloss1, gloss2);
                if (max < score) {
                    max = score;
                }
            }
        }

        return max;
    }

    private double calcFromSuperGloss(String gloss1, String gloss2) {
        return min(calculateCosScore(gloss1, gloss2), 1.0);
    }

    public List<POS[]> getPOSPairs() {
        return posPairs;
    }

    public List<SuperGloss> getSuperGlosses(Concept synset1, Concept synset2) {
        List<SuperGloss> glosses = new ArrayList(min(pairs.length, 1));
        String[] arr = pairs;
        int len = min(arr.length, 1);

        for(int i = 0; i < len; ++i) {
            String pair = arr[i];
            String[] links = pair.split(":");
            SuperGloss sg = new SuperGloss();
            //Get normal gloss
            sg.gloss1 = (List) this.db.getGloss(synset1, links[0]);
            sg.gloss2 = (List) this.db.getGloss(synset2, links[1]);
            sg.link1 = links[0];
            sg.link2 = links[1];
            sg.weight = 1.0D;
            glosses.add(sg);
        }

        return glosses;
    }

    private double calculateCosScore(String words1, String words2) {
        Map<String, Integer> vector1 = getVector(words1);
        Map<String, Integer> vector2 = getVector(words2);

        //Get common keys
        double numerator = 0.0;
        for (Map.Entry<String, Integer> entry1 : vector1.entrySet()) {
            for (Map.Entry<String, Integer> entry2 : vector2.entrySet()) {
                if (entry1.getKey().equals(entry2.getKey())) {
                    numerator += entry1.getValue() * entry2.getValue();
                }
            }
        }

        double sum1 = 0.0;
        for (Map.Entry<String, Integer> entry1 : vector1.entrySet()) {
            sum1 += pow(entry1.getValue(), 2);
        }

        double sum2 = 0.0;
        for (Map.Entry<String, Integer> entry1 : vector2.entrySet()) {
            sum2 += pow(entry1.getValue(), 2);
        }

        double denominator = sqrt(sum1) + sqrt(sum2);

        if (denominator <= 0.0) { return 0.0; }
        else { return numerator / denominator; }
    }

    private Map<String, Integer> getVector(String input) {
        List<String> words = new ArrayList<>(Arrays.asList(input.toLowerCase().split(" ")));
        return getVector(words);
    }

    private Map<String, Integer> getVector(List<String> words) {
        Map<String, Integer> vector = new TreeMap<>();
        for (String word : words) {
            if (vector.containsKey(word)) {
                vector.put(word, vector.get(word)+1);
            } else {
                vector.put(word, 1);
            }
        }
        return vector;
    }
}
