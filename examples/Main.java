import com.khan.baron.vcw.GlobalState;
import com.khan.baron.vcw.Pair;
import com.khan.baron.vcw.SemanticSimilarity;
import com.khan.baron.vcw.VoiceProcess;
import cooking.CookingState;
import game.GameState;
import call.CallState;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* This example demonstrates the system's accuracy using different methods */
public class Main {
    private static int scoreAllTests;
    private static URL sUrl;

    public static void main(String[] args) throws IOException {

        loadWordNetAndPOSTagger();

        List<SemanticSimilarity.SimilarityMethod> methods = Arrays.asList(
                null,
                SemanticSimilarity.SimilarityMethod.METHOD_WUP,
                SemanticSimilarity.SimilarityMethod.METHOD_LIN,
                SemanticSimilarity.SimilarityMethod.METHOD_LEACOCK,
                SemanticSimilarity.SimilarityMethod.METHOD_PATH,
                SemanticSimilarity.SimilarityMethod.METHOD_JCN,
                SemanticSimilarity.SimilarityMethod.METHOD_RES,
                SemanticSimilarity.SimilarityMethod.METHOD_FASTLESK,
                SemanticSimilarity.SimilarityMethod.METHOD_LESK,
                SemanticSimilarity.SimilarityMethod.METHOD_COS
        );

        for (SemanticSimilarity.SimilarityMethod method: methods) {
            if (method == null) { continue; }
            System.out.println("Running test suite using "+method.name()+" method...");
            runTestSuite(method, null);
            System.out.println("Finished test suite for "+method.name()+" method...");
        }
    }

    private static void loadWordNetAndPOSTagger() throws MalformedURLException {
        VoiceProcess.loadTagger("../models/english-left3words-distsim.tagger");
        File dictFile = new File("../models/dict/");
        if (dictFile.exists()) {
            System.out.println("Found WordNet database");
        } else {
            System.out.println("Could not find WordNet database. Path = "+dictFile);
            System.exit(0);
        }
        sUrl = new URL("file", null, dictFile.getPath());
    }

    private static void runTestSuite(SemanticSimilarity.SimilarityMethod method1,
        SemanticSimilarity.SimilarityMethod method2) throws IOException
    {
        SemanticSimilarity.setSimilarityMethodEnum(1, method1);
        SemanticSimilarity.setSimilarityMethodEnum(2, method2);

        scoreAllTests = 0;

        long testStart = System.currentTimeMillis();

        GameState gameState = new GameState();
        try { gameState.addDictionary(sUrl); }
        catch (Exception e) { System.out.println(e.getMessage()); }
        runTests(gameState, "Game", sGameTests);

        CallState callState = new CallState();
        try { callState.addDictionary(sUrl); }
        catch (Exception e) { System.out.println(e.getMessage()); }
        runTests(callState, "Video Conferencing", sCallTests);

        CookingState cookingState = new CookingState();
        try { cookingState.addDictionary(sUrl); }
        catch (Exception e) { System.out.println(e.getMessage()); }
        runTests(cookingState, "Cooking", sCookingTests);

        long testEnd = System.currentTimeMillis();
        long totalTime = testEnd - testStart;
        int totalTests = sGameTests.size()+sCallTests.size()+sCookingTests.size();
        double avgScore = (100.0* scoreAllTests)/totalTests;

        System.out.println("Final Results");
        System.out.println("Total time = "+totalTime+" ms");
        System.out.println("Average of Scores = "+new DecimalFormat("##.00").format(avgScore)+"%");
    }

    private static void runTests(
        GlobalState state, String testName, List<Pair<String, String>> tests) 
    {
        System.out.println("Running tests for "+testName);

        int scoreAcc = 0;
        long startTime = System.currentTimeMillis();
        for (Pair<String, String> test : tests) {
            String input = test.first;
            String expected = test.second;
            String result = state.updateState(input).replace("\n", " >> ");
            result += " >>> "+state.updateState("yes").replace("\n", " >> ");
            boolean passed = result.contains(expected);
            if (passed) {
                ++scoreAcc;
                ++scoreAllTests;
            }
            System.out.println("Result for "+testName+" test: "+input+" : "+gameRecord[2] +
                    (passed ? "" : " ---> "+result));
        }
        long endTime = System.currentTimeMillis();

        long totalTime = (endTime - startTime);
        double score = (100.0 * scoreAcc / (double)tests.size());
        System.out.println("Finished tests for "+testName);
        System.out.println("Total time = "+totalTime+" ms");
        System.out.println("Score = "+new DecimalFormat("##.00").format(score)+"%");
    }

    private static List<Pair<String, String>> sGameTests = new ArrayList<Pair<String, String>>(Arrays.asList(
            new Pair<String, String>("attack", "ATTACK"),
            new Pair<String, String>("charge", "ATTACK"),
            new Pair<String, String>("hit", "ATTACK"),
            new Pair<String, String>("tackle", "ATTACK"),
            new Pair<String, String>("fight", "ATTACK"),
            new Pair<String, String>("assault", "ATTACK"),
            new Pair<String, String>("battle", "ATTACK"),
            new Pair<String, String>("launch an assault", "ATTACK"),
            new Pair<String, String>("attack with a sword", "ATTACK_WEAPON"),
            new Pair<String, String>("attack with something sharp", "ATTACK_WEAPON"),
            new Pair<String, String>("attack with something metallic", "ATTACK_WEAPON"),
            new Pair<String, String>("heal", "HEAL"),
            new Pair<String, String>("recover", "HEAL"),
            new Pair<String, String>("regenerate", "HEAL"),
            new Pair<String, String>("heal with a potion", "HEAL_POTION"),
            new Pair<String, String>("heal with an healing drink", "HEAL_POTION"),
            new Pair<String, String>("move forwards", "MOVE_FORWARDS"),
            new Pair<String, String>("move backwards", "MOVE_BACKWARDS"),
            new Pair<String, String>("continue forwards", "MOVE_FORWARDS"),
            new Pair<String, String>("run forwards", "MOVE_FORWARDS"),
            new Pair<String, String>("dash forwards", "MOVE_FORWARDS")
    ));

    private static List<Pair<String, String>> sCallTests = new ArrayList<Pair<String, String>>(Arrays.asList(
            new Pair<String, String>("phone", "PHONE"),
            new Pair<String, String>("phone fred", "PHONE_FRED"),
            new Pair<String, String>("phone jane", "PHONE_JANE"),
            new Pair<String, String>("ring jane", "PHONE_JANE"),
            new Pair<String, String>("phone jane with video", "PHONE_JANE_VIDEO"),
            new Pair<String, String>("phone jane with webcam", "PHONE_JANE_VIDEO"),
            new Pair<String, String>("call jane with audio", "PHONE_JANE_AUDIO"),
            new Pair<String, String>("phone jane with sound", "PHONE_JANE_AUDIO"),
            new Pair<String, String>("stop call", "STOP"),
            new Pair<String, String>("stop call with fred", "STOP_FRED"),
            new Pair<String, String>("end call with fred", "STOP_FRED"),
            new Pair<String, String>("finish call with fred", "STOP_FRED"),
            new Pair<String, String>("halt call with fred", "STOP_FRED"),
            new Pair<String, String>("mute video", "MUTE_VIDEO"),
            new Pair<String, String>("mute screen", "MUTE_VIDEO"),
            new Pair<String, String>("mute jane", "MUTE_JANE")
    ));

    private static List<Pair<String, String>> sCookingTests = new ArrayList<Pair<String, String>>(Arrays.asList(
            new Pair<String, String>("make an egg", "MAKE_EGG"),
            new Pair<String, String>("make eggs", "MAKE_EGG"),
            new Pair<String, String>("make soup", "MAKE_SOUP"),
            new Pair<String, String>("create soup", "MAKE_SOUP"),
            new Pair<String, String>("produce soup", "MAKE_SOUP"),
            new Pair<String, String>("cook egg", "MAKE_EGG"),
            new Pair<String, String>("boil egg", "BOIL_EGG"),
            new Pair<String, String>("heat egg", "BOIL_EGG"),
            new Pair<String, String>("boil soup with cooker", "BOIL_SOUP_COOKER"),
            new Pair<String, String>("boil soup with boiler", "BOIL_SOUP_COOKER"),
            new Pair<String, String>("stir soup", "STIR_SOUP"),
            new Pair<String, String>("stir chowder", "STIR_SOUP"),
            new Pair<String, String>("stir pottage", "STIR_SOUP"),
            new Pair<String, String>("stir soup with spoon", "STIR_SOUP_SPOON"),
            new Pair<String, String>("stir soup with cutlery", "STIR_SOUP_SPOON"),
            new Pair<String, String>("stir soup with utensil", "STIR_SOUP_SPOON"),
            new Pair<String, String>("stir soup with tablespoon", "STIR_SOUP_SPOON"),
            new Pair<String, String>("stir soup with teaspoon", "STIR_SOUP_SPOON"),
            new Pair<String, String>("stir soup with soupspoon", "STIR_SOUP_SPOON"),
            new Pair<String, String>("blend chowder using soupspoon", "STIR_SOUP_SPOON")
    ));
}
