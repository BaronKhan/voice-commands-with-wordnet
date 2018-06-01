package com.khan.baron.vcw;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.util.PorterStemmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.SynsetID;

/**
 * Created by Baron on 20/01/2018.
 */

// Alternative to NictWordNet, which is included in the WS4J library.
// https://github.com/Sciss/ws4j/blob/master/src/main/java/edu/cmu/lti/lexical_db/NictWordNet.java
// NictWordNet does not work because it uses some sqlite methods that do not work in Android.
// This is a custom class that attempts to implement the same thing using MIT's JWI library.
public class CustomLexicalDatabase implements ILexicalDatabase {
    public IDictionary mDict;
    public Map<String, ISynset> synsetMap = new HashMap<>();
    private static PorterStemmer sStemmer;
    private Map<String, Collection<String>> glossCache = new HashMap<>();

    public static POS stringToTag(String posStr) {
        if (posStr.equals("n")) { return POS.NOUN; }
        else if (posStr.equals("v")) { return POS.VERB; }
        else if (posStr.equals("r")) { return POS.ADVERB; }
        else if (posStr.equals("a")) { return POS.ADJECTIVE; }
        else { return POS.NOUN; }
    }

    public CustomLexicalDatabase(IDictionary dict) {
        mDict = dict;
    }

    public Collection<Concept> getAllConcepts(String word, String pos) {
        List<Concept> synsetStrings = new ArrayList<>();
        IIndexWord idxWord = mDict.getIndexWord(word, stringToTag(pos));
        if (idxWord != null) {
            List<IWordID> wordIDs = idxWord.getWordIDs();
            for (IWordID id : wordIDs) {
                String synsetStr = id.getSynsetID().toString().replace("SID-", "").toLowerCase();
                synsetStrings.add(new Concept(synsetStr, edu.cmu.lti.jawjaw.pobj.POS.valueOf(pos)));
                IWord w = mDict.getWord(id);
                synsetMap.put(synsetStr, w.getSynset());
            }
        }
        return synsetStrings;
    }

    public Concept getMostFrequentConcept(String word, String pos) {
        Collection<Concept> concepts = getAllConcepts(word, pos);
        if (concepts.size() > 0) { return concepts.iterator().next(); }
        return null;
    }

    public Collection<String> getHypernyms(String synsetStr) {
        List<String> synsetStrings = new ArrayList<>();
        ISynset synset = synsetMap.get(synsetStr);
        if (synset != null) {
            List<ISynsetID> hypernymsList = synset.getRelatedSynsets(Pointer.HYPERNYM);

            for (ISynsetID id : hypernymsList) {
                String hypernymStr = id.toString().replace("SID-", "").toLowerCase();
                //Need to add this synset to the map
                List<IWord> words = mDict.getSynset(id).getWords();
                for (IWord w : words) {
                    synsetMap.put(hypernymStr, w.getSynset());
                }
                synsetStrings.add(hypernymStr);
            }
        }
        return synsetStrings;
    }

    public Concept findSynsetBySynset( String synset ) {
        return null;
    }

    // To offset.
    public String conceptToString( String synset ) {
        return "";
    }

    // Note: most of this method is similar to the NictWordNet one, but using JWI instead
    // https://github.com/Sciss/ws4j/blob/master/src/main/java/edu/cmu/lti/lexical_db/NictWordNet.java
    public Collection<String> getGloss( Concept synset, String linkString, boolean useFull ) {
        String synsetStr = synset.getSynset();

        char posTag = '*';
        Pattern p = Pattern.compile("-[a-zA-Z]");
        Matcher m = p.matcher(synsetStr.toLowerCase());
        if (m.find()) { posTag = m.group(0).charAt(1); }

        if (posTag == '*') { return new ArrayList<>(); }

        int synsetOffset = Integer.parseInt(synsetStr.replaceAll("-[a-zA-Z]", ""));
        SynsetID synsetID = new SynsetID(synsetOffset, POS.getPartOfSpeech(posTag));
        ISynset synsetJWI = mDict.getSynset(synsetID);

        //Build up pointer to synsets (link = pointer)
        List<ISynsetID> linkedSynsets = new ArrayList<>();
        Link link = null;
        try {
            link = Link.valueOf(linkString);
            if (link.equals(Link.mero) && useFull) {
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets(Pointer.MERONYM_MEMBER));
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets(Pointer.MERONYM_SUBSTANCE));
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets(Pointer.MERONYM_PART));
            } else if (link.equals(Link.holo) && useFull) {
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets(Pointer.HOLONYM_MEMBER));
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets(Pointer.HOLONYM_SUBSTANCE));
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets(Pointer.HOLONYM_PART));
            } else if (link.equals(Link.syns)) {
                linkedSynsets.add(synsetJWI.getID());
            } else {
                linkedSynsets.addAll(synsetJWI.getRelatedSynsets());
            }
        } catch (IllegalArgumentException e) {
            linkedSynsets.add(synsetJWI.getID());
        }

        List<String> glosses = new CopyOnWriteArrayList<>();
        for (ISynsetID linkedSynsetID : linkedSynsets) {
            String gloss = null;
            ISynset linkedSynsetJWI = mDict.getSynset(linkedSynsetID);
            if (Link.syns.equals(link)) {
                gloss = synset.getName();
            } else {
                gloss = linkedSynsetJWI.getGloss();
            }

            if (gloss == null) { continue; }

            if (useFull) {
                gloss = gloss.replaceAll("[.;:,?!(){}\"`$%@<>]", " ");
                gloss = gloss.replaceAll("&", " and ");
                gloss = gloss.replaceAll("_", " ");
                gloss = gloss.replaceAll("[ ]+", " ");
                gloss = gloss.replaceAll("(?<!\\w)'", " ");
                gloss = gloss.replaceAll("'(?!\\w)", " ");
                gloss = gloss.replaceAll("--", " ");
            } /*else {
                gloss = gloss.replaceAll("&", " and ");
                gloss = gloss.replaceAll("[.;:,?!(){}\"`$%@<>]|_|[ ]+|(?<!\\w)'|'(?!\\w)|--", " ");
            }*/

            gloss = gloss.toLowerCase();

            if ( WS4JConfiguration.getInstance().useStem() ) {
                gloss = sStemmer.stemSentence( gloss );
            }

            glosses.add( gloss );
        }
        return glosses;
    }

    public Collection<String> getGloss( Concept synset, String linkString ) {
        return getGloss(synset, linkString, true);
    }

    public Collection<String> getGlossOptimised(Concept synset, String linkString ) {
        return getGloss(synset, linkString, false);
    }

    private List<String> clone( List<String> original ) {
        return new ArrayList<>( original );
    }

}
