package com.khan.baron.vcw;

import com.khan.baron.vcw.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.Triple;

//Handles ambiguous intents
public class AmbiguousHandler {
    private VoiceProcess mVoiceProcess;

    private static boolean sGiveMultipleSuggestions = true;

    //Confirmation checking state
    private boolean mIsAmbiguous = false;
    private boolean mShowAllSuggestions = false;

    private List<Triple<String, String, Double>> mAmbiguousActionCandidates = null;   //synonym first
    private List<Triple<String, Entity, Double>> mAmbiguousTargetCandidates = null;
    private List<Triple<String, Entity, Double>> mAmbiguousContextCandidates = null;
    private Map<Pair<String, String>, Integer> mAmbiguousCount = new HashMap<>();

    //Store the best suggestions in this (ordered by highest confidence)
    private List<Triple<Triple, Triple, Triple>> mAmbiguousPermutations = null;

    //Confirmation execution state
    private boolean mExpectingReply = false;
    private Pair<String, String> mAmbiguousPair = null;
    private String mPendingAction = null;
    private Entity mPendingTarget = null;
    private String mPendingContext = null;

    public static boolean isGivingMultipleSuggestions() {
        return sGiveMultipleSuggestions;
    }

    public static void setGiveMultipleSuggestions(boolean giveMultipleSuggestions) {
        sGiveMultipleSuggestions = giveMultipleSuggestions;
    }

    public AmbiguousHandler(VoiceProcess voiceProcess) { mVoiceProcess = voiceProcess; }

    public void resetState() {
        mIsAmbiguous = false;
        mPendingAction = null;
        mPendingTarget = null;
    }

    public String processPendingIntent(String input, GlobalState state,
                                       ContextActionMap contextActionMap) {
        if (VoiceProcess.replyIsYes(input)) {
            if (canExecutePending(contextActionMap)) {
                mExpectingReply = false;
                Action action = contextActionMap.get(mPendingContext).get(mPendingAction);
                return addAmbiguousSynonyms() + "\n" + action.execute(state, mPendingTarget);
            }
            return "Intent not understood.";
        } else if (AmbiguousHandler.isGivingMultipleSuggestions() && (VoiceProcess.replyIsNo(input))){
            // Try another suggestion until all suggestions are done
            return generateSuggestion();
        } else {
            mExpectingReply = false;
            return "Intent ignored.";
        }
    }

    private boolean canExecutePending(ContextActionMap map) {
        return (mPendingAction != null && map.get(mPendingContext).get(mPendingAction) != null);
    }

    public String initSuggestion(String chosenAction, Entity currentTarget, String chosenContext) {
        mPendingAction = chosenAction;
        mPendingTarget = currentTarget;
        mPendingContext = chosenContext;
        mAmbiguousPermutations = generateAmbiguousPermutations();
        if (mShowAllSuggestions && mAmbiguousPermutations.size() > 3) {
            mExpectingReply = false;
            return "Intent not understood.\n" + generateAllSuggestions();
        } else {
            mExpectingReply = true;
            mShowAllSuggestions = false;
            return "Intent not understood.\n" + generateSuggestion();
        }
    }

    private String buildIntent(String action, Entity target, Entity context, boolean usingAltSFS) {
        Entity defaultTarget = mVoiceProcess.getContextActionMap().getDefaultTarget();
        if (usingAltSFS) {
            return (context == null || context.getName().equals("default")
                    ? ""
                    : "use "+context.getName()+" to ")
                    +action
                    +((target != defaultTarget) ? " "+target.getName() : "");
        } else {
            return action
                    +((target != defaultTarget) ? " "+target.getName() : "")
                    +(context == null || context.getName().equals("default")
                    ? ""
                    : " with "+context.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Triple<Triple, Triple, Triple>> generateAmbiguousPermutations() {
        List<Triple<Triple, Triple, Triple>> perms = new ArrayList<>();
        List<Double> scores = new ArrayList<>();   //Separating the score so no need to pass around
        if (mAmbiguousActionCandidates.size() > 0) {
            for (Triple actionCandidate : mAmbiguousActionCandidates) {
                if (mAmbiguousTargetCandidates.size() > 0) {
                    for (Triple targetCandidate : mAmbiguousTargetCandidates) {
                        if (mAmbiguousContextCandidates.size() > 0) {
                            for (Triple contextCandidate : mAmbiguousContextCandidates) {
                                double score = (Double) (actionCandidate.third) +
                                        (Double) (targetCandidate.third) +
                                        (Double) (contextCandidate.third);
                                Triple triple =
                                        new Triple(actionCandidate, targetCandidate, contextCandidate);
                                if (actionCandidate.first != targetCandidate.first &&
                                        targetCandidate.first != contextCandidate.first &&
                                        actionCandidate.first != contextCandidate.first)
                                {
                                    addAmbiguousPermutation(perms, scores, score, triple);
                                }
                            }
                        } else {
                            double score = (Double) (actionCandidate.third) +
                                    (Double) (targetCandidate.third);
                            Triple triple = new Triple(actionCandidate, targetCandidate, null);
                            if (actionCandidate.first != targetCandidate.first) {
                                addAmbiguousPermutation(perms, scores, score, triple);
                            }
                        }
                    }
                } else {
                    if (mAmbiguousContextCandidates.size() > 0) {
                        for (Triple contextCandidate : mAmbiguousContextCandidates) {
                            double score = (Double) (actionCandidate.third) +
                                    (Double) (contextCandidate.third);
                            Triple triple = new Triple(actionCandidate, null, contextCandidate);
                            if (actionCandidate.first != contextCandidate.first) {
                                addAmbiguousPermutation(perms, scores, score, triple);
                            }
                        }
                    } else {
                        double score = (Double) (actionCandidate.third);
                        Triple triple = new Triple(actionCandidate, null, null);
                        addAmbiguousPermutation(perms, scores, score, triple);
                    }
                }
            }
        } else {
            if (mAmbiguousTargetCandidates.size() > 0) {
                for (Triple targetCandidate : mAmbiguousTargetCandidates) {
                    if (mAmbiguousContextCandidates.size() > 0) {
                        for (Triple contextCandidate : mAmbiguousContextCandidates) {
                            double score = (Double) (targetCandidate.third) +
                                    (Double) (contextCandidate.third);
                            Triple triple = new Triple(null, targetCandidate, contextCandidate);
                            if (targetCandidate.first != contextCandidate.first) {
                                addAmbiguousPermutation(perms, scores, score, triple);
                            }
                        }
                    } else {
                        double score = (Double) (targetCandidate.third);
                        Triple triple = new Triple(null, targetCandidate, null);
                        addAmbiguousPermutation(perms, scores, score, triple);
                    }
                }
            } else {
                //Context candidates MUST exist then
                for (Triple contextCandidate : mAmbiguousContextCandidates) {
                    double score = (Double) (contextCandidate.third);
                    Triple triple = new Triple(null, null, contextCandidate);
                    addAmbiguousPermutation(perms, scores, score, triple);
                }
            }
        }
        return perms;
    }

    @SuppressWarnings("unchecked")
    private void addAmbiguousPermutation(List<Triple<Triple, Triple, Triple>> perms,
                                         List<Double> scores, double score, Triple triple) {
        for (int i = 1; i < scores.size(); ++i) {
            if (score > scores.get(i)) {
                scores.add(i, score);
                perms.add(i, triple);
                return;
            }
        }
        //Add to end of lists
        scores.add(score);
        perms.add(triple);
    }

    @SuppressWarnings("unchecked")
    private String generateSuggestion() {
        if (mAmbiguousPermutations.size() > 0) {
            Triple<Triple, Triple, Triple> phraseCandidate = mAmbiguousPermutations.get(0);
            mAmbiguousPermutations.remove(0);
            if (phraseCandidate.first != null) { getAmbiguousAction(phraseCandidate.first); }
            if (phraseCandidate.second != null) { getAmbiguousTarget(phraseCandidate.second); }
            if (phraseCandidate.third != null) { getAmbiguousContext(phraseCandidate.third); }
            String pendingIntent =
                    buildIntent(mPendingAction, mPendingTarget, mVoiceProcess.getActionContext(),
                            mVoiceProcess.isUsingAltSFS());
            return "Did you mean, \"" + pendingIntent + "\"? (yes/no)";
        } else {
            mExpectingReply = false;
            return "No more suggestions. Intent ignored.";
        }
    }

    private String generateAllSuggestions() {
        mShowAllSuggestions = false;
        String output = "Multiple matches found:\n";
        int count = 1;
        for (Triple<Triple, Triple, Triple> phraseCandidate : mAmbiguousPermutations) {
            if (phraseCandidate.first != null) { getAmbiguousAction(phraseCandidate.first); }
            if (phraseCandidate.second != null) { getAmbiguousTarget(phraseCandidate.second); }
            if (phraseCandidate.third != null) { getAmbiguousContext(phraseCandidate.third); }
            String currentIntent = "   "+count+". \""+
                    buildIntent(mPendingAction, mPendingTarget, mVoiceProcess.getActionContext(),
                            mVoiceProcess.isUsingAltSFS()) +"\"";
            output += currentIntent+"\n";
            ++count;
        }
        return output+"Please try again.";
    }

    private void getAmbiguousContext(Triple<String, Entity, Double> candidate) {
        String synonym = candidate.first;
        Entity context = candidate.second;
        mAmbiguousPair = new Pair<>(synonym, context.getName());
        mVoiceProcess.setActionContext(context);
        if (mVoiceProcess.getContextActionMap().isValidContext(context.getContext())) {
            if (mVoiceProcess.getContextActionMap().get(context.getContext())
                    .get(mPendingAction) == null)
            {
                mPendingContext = "default";
            } else { mPendingContext = context.getContext(); }
        } else {
            mPendingContext = "default";
        }
    }

    private void getAmbiguousTarget(Triple<String, Entity, Double> candidate) {
        String synonym = candidate.first;
        Entity target = candidate.second;
        mAmbiguousPair = new Pair<>(synonym, target.getName());
        mPendingTarget = target;
    }

    private void getAmbiguousAction(Triple<String, String, Double> candidate) {
        String synonym = candidate.first;
        String action = candidate.second;
        mAmbiguousPair = new Pair<>(synonym, action);
        mPendingAction = mAmbiguousPair.second;
    }

    private String addAmbiguousSynonyms() {
        String output = "";
        if (mAmbiguousPair != null) {
            if (mAmbiguousCount.containsKey(mAmbiguousPair)) {
                output += mVoiceProcess.addSynonym(mAmbiguousPair.first, mAmbiguousPair.second);
            } else {
                mAmbiguousCount.put(mAmbiguousPair, 1);
            }
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    private void addAmbiguousCandidate(
            List candidates, Triple triple, double bestScore) {
        if ((Double)(triple.third) > bestScore) {
            candidates.add(0, triple);
        } else {
            //Find insertion position
            for (int i = 1; i < candidates.size(); ++i) {
                Triple candidate = (Triple)(candidates.get(i));
                if ((Double)(triple.third) > (Double)candidate.third) {
                    candidates.add(i, triple);
                    return;
                }
            }
            //Add to the end of the list
            candidates.add(triple);
        }
    }

    public void addAmbiguousActionCandidate(Triple triple, double bestScore) {
        addAmbiguousCandidate(mAmbiguousActionCandidates, triple, bestScore);
    }

    public void addAmbiguousTargetCandidate(Triple triple, double bestScore) {
        addAmbiguousCandidate(mAmbiguousTargetCandidates, triple, bestScore);
    }

    public void addAmbiguousContextCandidate(Triple triple, double bestScore) {
        addAmbiguousCandidate(mAmbiguousContextCandidates, triple, bestScore);
    }

    public void initAmbiguousActionCandidates() { mAmbiguousActionCandidates = new ArrayList<>(); }

    public void initAmbiguousTargetCandidates() { mAmbiguousTargetCandidates = new ArrayList<>(); }

    public void initAmbiguousContextCandidates() { mAmbiguousContextCandidates = new ArrayList<>(); }

    public void clearAmbiguousActionCandidates() { mAmbiguousActionCandidates.clear(); }

    public void clearAmbiguousTargetCandidates() { mAmbiguousTargetCandidates.clear(); }

    public void clearAmbiguousContextCandidates() { mAmbiguousContextCandidates.clear(); }

    public boolean isExpectingReply() {
        return mExpectingReply;
    }

    public void setExpectingReply(boolean mExpectingReply) {
        this.mExpectingReply = mExpectingReply;
    }

    public Pair<String, String> getAmbiguousPair() {
        return mAmbiguousPair;
    }

    public void setAmbiguousPair(Pair<String, String> mAmbiguousPair) {
        this.mAmbiguousPair = mAmbiguousPair;
    }

    public String getPendingAction() {
        return mPendingAction;
    }

    public void setPendingAction(String mPendingAction) {
        this.mPendingAction = mPendingAction;
    }

    public Entity getPendingTarget() {
        return mPendingTarget;
    }

    public void setPendingTarget(Entity mPendingTarget) {
        this.mPendingTarget = mPendingTarget;
    }

    public String getPendingContext() {
        return mPendingContext;
    }

    public void setPendingContext(String mPendingContext) {
        this.mPendingContext = mPendingContext;
    }

    public boolean isAmbiguous() {
        return mIsAmbiguous;
    }

    public void setIsAmbiguous(boolean mIsAmbiguous, boolean showAllSuggestions) {
        this.mIsAmbiguous = mIsAmbiguous;
        mShowAllSuggestions = mShowAllSuggestions || showAllSuggestions;
    }

    public void setIsAmbiguous(boolean mIsAmbiguous) {
        setIsAmbiguous(mIsAmbiguous, false);
    }
}
