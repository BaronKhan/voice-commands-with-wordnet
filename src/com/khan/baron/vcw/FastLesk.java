//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.khan.baron.vcw;

import com.khan.baron.vcw.CustomLexicalDatabase;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.GlossFinder;
import edu.cmu.lti.ws4j.util.OverlapFinder;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.cmu.lti.ws4j.util.GlossFinder.SuperGloss;
import edu.cmu.lti.ws4j.util.OverlapFinder.Overlaps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.min;

//Custom Lesk class (altered from decompiled Lesk bytecode)
public class FastLesk extends RelatednessCalculator {
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

    public FastLesk(ILexicalDatabase db) {
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
        Overlaps overlaps = OverlapFinder.getOverlaps(gloss1, gloss2);
        double functionsScore = 0.0D;
        if (enableTrace) {
        }

        Iterator i = overlaps.overlapsHash.keySet().iterator();

        while(i.hasNext()) {
            String key = (String)i.next();
            String[] tempArray = key.split("\\s+");
            int value = tempArray.length * tempArray.length * (Integer)overlaps.overlapsHash.get(key);
            functionsScore += (double)value;
            if (enableTrace) {
            }
        }

        if (enableTrace) {
        }

        if (WS4JConfiguration.getInstance().useLeskNomalizer()) {
            int denominator = overlaps.length1 + overlaps.length2;
            if (denominator > 0) {
                functionsScore /= (double)denominator;
            }

            if (enableTrace) {
            }
        }

        return functionsScore;
    }

    public List<POS[]> getPOSPairs() {
        return posPairs;
    }

    public List<GlossFinder.SuperGloss> getSuperGlosses(Concept synset1, Concept synset2) {
        List<GlossFinder.SuperGloss> glosses = new ArrayList(min(pairs.length, 1));
        String[] arr = pairs;
        int len = min(arr.length, 1);

        for(int i = 0; i < len; ++i) {
            String pair = arr[i];
            String[] links = pair.split(":");
            GlossFinder.SuperGloss sg = new GlossFinder.SuperGloss();
            if (db instanceof CustomLexicalDatabase) {
                sg.gloss1 = (List) ((CustomLexicalDatabase)this.db).getGlossOptimised(synset1, links[0]);
                sg.gloss2 = (List) ((CustomLexicalDatabase)this.db).getGlossOptimised(synset2, links[1]);
            } else {
                //Get normal gloss
                sg.gloss1 = (List) this.db.getGloss(synset1, links[0]);
                sg.gloss2 = (List) this.db.getGloss(synset2, links[1]);
            }
            sg.link1 = links[0];
            sg.link2 = links[1];
            sg.weight = 1.0D;
            glosses.add(sg);
        }

        return glosses;
    }
}
