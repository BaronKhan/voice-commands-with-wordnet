package com.khan.baron.vcw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Baron on 12/01/2018.
 */

public abstract class ContextActionMap extends Entity {
    protected GlobalState mState;
    protected List<Entity> mPossibleTargets = new ArrayList<>();
    private Entity mDefaultTarget = null;
    protected List<Entity> mPossibleContexts = new ArrayList<>();
    private List<String> mActionList = new ArrayList<>();
    protected Map<String, Map<String, Action>> mContextActionMapping = new HashMap<>();
    protected Map<String, List<String>> mSynonymMap = new HashMap<>();
    protected Map<String, String> mIgnoreMap = new HashMap<>();
    protected SentenceMapper mSentenceMapper;
    private Self mSelf = new Self();

    private static Map<String, List<String>> sUserSynonyms = new ConcurrentHashMap<>();
    private static boolean sRememberUserSynonyms = true;

    public ContextActionMap(GlobalState state) {
        super("actions", "commands", "options");
        setContext("actions");
        mState = state;
        mPossibleTargets.add(this);
        mPossibleTargets.add(mSelf);
        mSentenceMapper = new SentenceMapper(this);
        mSynonymMap = new HashMap<>(sUserSynonyms);
    }

    public static Map<String, List<String>> getUserSynonyms() {
        return sUserSynonyms;
    }

    public static void setUserSynonyms(Map<String, List<String>> sUserSynonyms) {
        ContextActionMap.sUserSynonyms = sUserSynonyms;
    }

    public static boolean isRememberingUserSynonyms() {
        return sRememberUserSynonyms;
    }

    public static void setRememberUserSynonyms(boolean sRememberUserSynonyms) {
        ContextActionMap.sRememberUserSynonyms = sRememberUserSynonyms;
    }

    public Map<String, Action> get(String action) { return mContextActionMapping.get(action); }

    public List<String> getActions() { return mActionList; }

    public boolean isValidAction(String action) {
        return mActionList.contains(action);
    }

    public void setPossibleContexts(List<Entity> contextList) { mPossibleContexts = contextList; }
    public void addPossibleContext(Entity context) { mPossibleContexts.add(context); }
    public void removePossibleContext(Entity context) { mPossibleContexts.remove(context); }
    public List<Entity> getPossibleContexts() { return mPossibleContexts; }

    public void addPossibleContexts(List<Entity> contextList) {
        mPossibleContexts.addAll(contextList);
    }

    public void setPossibleTargets(List<Entity> targetsList) { mPossibleTargets = targetsList; }
    public void addPossibleTarget(Entity target) { mPossibleTargets.add(target); }
    public void removePossibleTarget(Entity target) { mPossibleTargets.remove(target); }
    public List<Entity> getPossibleTargets() { return mPossibleTargets; }

    public void addPossibleTargets(List<Entity> targetsList) {
        mPossibleTargets.addAll(targetsList);
    }

    public void addContextActions(String context, Action ... actions) {
        if (actions.length != mActionList.size()) { return; }
        Map<String, Action> tempList = new HashMap<>();
        int i=0;
        for (String actionStr: mActionList) {
            tempList.put(actionStr, actions[i]);
            ++i;
        }
        mContextActionMapping.put(context, tempList);
    }

    public void addDefaultContextActions(Action ... actions) {
        addContextActions("default", actions);
    }

    public boolean isValidContext(String context) {
        return mContextActionMapping.keySet().contains(context);
    }

    public void setDefaultTarget(Entity defaultTarget) { mDefaultTarget = defaultTarget; }
    public Entity getDefaultTarget() { return mDefaultTarget; }

    public void addSynonym(String synonym, String action) {
        if(mSynonymMap.containsKey(synonym)) {
            if (!mSynonymMap.get(synonym).contains(action)) {
                mSynonymMap.get(synonym).add(action);
            }
        } else {
            mSynonymMap.put(synonym, new ArrayList<>(Collections.singletonList(action)));
        }
    }

    public void addUserSynonym(String synonym, String action) {
        addSynonym(synonym,action);
        addUserSynonymOnly(synonym, action);
    }

    public static void addUserSynonymOnly(String synonym, String action) {
        if (sRememberUserSynonyms) {
            if (sUserSynonyms.containsKey(synonym)) {
                if (!sUserSynonyms.get(synonym).contains(action)) {
                    sUserSynonyms.get(synonym).add(action);
                }
            } else {
                sUserSynonyms.put(synonym, new ArrayList<>(Collections.singletonList(action)));
            }
        }
    }

    public static void clearUserSynonyms() {
        sUserSynonyms = new ConcurrentHashMap<>();
    }

    public boolean hasSynonym(String synonym) {
        return mSynonymMap.keySet().contains(synonym);
    }

    public void addMatchIgnore(String ignore, String action) {
        mIgnoreMap.put(ignore,action);
    }

    public boolean wordIsIgnored(String ignore, String word) {
        return mIgnoreMap.containsKey(ignore) && mIgnoreMap.get(ignore).equals(word);
    }

    public List<String> getSynonymMapping(String synonym) {
        return (hasSynonym(synonym)) ? mSynonymMap.get(synonym) : Collections.singletonList(synonym);
    }

    public boolean hasPossibleTarget(String name) {
        for (Entity target : mPossibleTargets) {
            if (target.getName().equals(name)) { return true; }
        }
        return false;
    }

    public Entity getPossibleTarget(String name) {
        for (Entity target : mPossibleTargets) {
            if (target.getName().equals(name)) { return target; }
        }
        return null;
    }

    public boolean hasPossibleContext(String name) {
        for (Entity context : mPossibleContexts) {
            if (context.getName().equals(name)) { return true; }
        }
        return false;
    }

    public Entity getPossibleContext(String name) {
        for (Entity context : mPossibleContexts) {
            if (context.getName().equals(name)) { return context; }
        }
        return null;
    }

    public void addSentenceMatch(Action action, String targetName, String ... examples) {
        mSentenceMapper.addSentenceMatch(action, targetName, examples);
    }

    public SentenceMapper getSentenceMapper() {
        return mSentenceMapper;
    }

    public void setSentenceMapper(SentenceMapper mSentenceMapper) {
        this.mSentenceMapper = mSentenceMapper;
    }

    public List<String> getActionList() {
        return mActionList;
    }

    public void setActionList(List<String> mActionList) {
        this.mActionList = mActionList;
    }

    public void setActionList(String ... mActionList) {
        this.mActionList = Arrays.asList(mActionList);
    }

    public Self getSelf() {
        return mSelf;
    }

    public void setSelf(Self mSelf) {
        this.mSelf = mSelf;
    }

    public void setDefaultTargetToSelf() {
        setDefaultTarget(getSelf());
    }

    public void clearPossibleEntities() {
        mPossibleTargets = new ArrayList<>();
        mPossibleContexts = new ArrayList<>();
        mPossibleTargets.add(this);
        mPossibleTargets.add(mSelf);
    }
}
