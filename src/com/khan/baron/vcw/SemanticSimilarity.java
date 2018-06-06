package com.khan.baron.vcw;


import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import static java.lang.Math.max;

public class SemanticSimilarity {
    public enum SimilarityMethod {
        METHOD_WUP,
        METHOD_LIN,
        METHOD_JCN,
        METHOD_LESK,
        METHOD_FASTLESK,
        METHOD_LCH,
        METHOD_PATH,
        METHOD_RES,
        METHOD_COS
    }

    private static final SemanticSimilarity sInstance = new SemanticSimilarity();

    public static SemanticSimilarity getInstance() { return sInstance; }

    //Use all senses, not just most frequent sense (slower but more accurate)
    private SemanticSimilarity() { WS4JConfiguration.getInstance().setMFS(false); }

    private static SimilarityMethod sCurrentMethod1 = SimilarityMethod.METHOD_LCH;
    private static SimilarityMethod sCurrentMethod2 = null;

    private ILexicalDatabase mDb = null;
    private RelatednessCalculator mMethod1 = null;
    private RelatednessCalculator mMethod2 = null;

    public void init(ILexicalDatabase db) {
        mDb = db;
        setSimilarityMethod(1, sCurrentMethod1);
        setSimilarityMethod(2, sCurrentMethod2);
    }

    public static SimilarityMethod getSimilarityMethod(int i) {
        if (i == 1) { return sCurrentMethod1; }
        else { return sCurrentMethod2; }
    }

    public static void setSimilarityMethodEnum(int i, SimilarityMethod chosenMethod) {
        if (i == 1) { sCurrentMethod1 = chosenMethod; }
        else { sCurrentMethod2 = chosenMethod; }
    }

    protected void setSimilarityMethod(int i, SimilarityMethod chosenMethod) {
        RelatednessCalculator method = null;
        if (mDb == null) { return; }
        if (chosenMethod != null) {
            switch (chosenMethod) {
                case METHOD_WUP:
                    method = new WuPalmer(mDb);
                    break;
                case METHOD_LIN:
                    method = new Lin(mDb);
                    break;
                case METHOD_JCN:
                    method = new JiangConrath(mDb);
                    break;
                case METHOD_LESK:
                    method = new Lesk(mDb);
                    break;
                case METHOD_FASTLESK:
                    method = new FastLesk(mDb);
                    break;
                case METHOD_LCH:
                    method = new LeacockChodorow(mDb);
                    break;
                case METHOD_PATH:
                    method = new Path(mDb);
                    break;
                case METHOD_RES:
                    method = new Resnik(mDb);
                    break;
                case METHOD_COS:
                    method = new Cos(mDb);
                    break;
                default:
                    method = null;
            }
        }
        if (i == 1) {
            sCurrentMethod1 = chosenMethod;
            mMethod1 = method;
        } else {
            sCurrentMethod2 = chosenMethod;
            mMethod2 = method;
        }
    }

    public double calculateScore(String word1, String word2) {
        if (mDb == null) {
            return 0.0;
        }

        double score1=0.0, score2=0.0, score;
        try {
            if (mMethod1 != null) { score1 = mMethod1.calcRelatednessOfWords(word1, word2); }
            if (mMethod2 != null) { score2 = mMethod2.calcRelatednessOfWords(word1, word2); }

            //Normalise score
            if (sCurrentMethod1 == SimilarityMethod.METHOD_LESK) { score1 = max(score1 / 80.0, 1.0); }
            if (sCurrentMethod2 == SimilarityMethod.METHOD_LESK) { score2 = max(score2 / 80.0, 1.0); }

            if (score2 <= 0) { score = score1; }
            else if (score1 <= 0) { score = score2; }
            else { score = (score1 * 0.5) + (score2 * 0.5); }



            return score;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
