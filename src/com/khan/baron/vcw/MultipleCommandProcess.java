package com.khan.baron.vcw;


import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import edu.mit.jwi.IDictionary;

public class MultipleCommandProcess {
    private VoiceProcess mVoiceProcess;
    private Queue<String> mPartialQueue;    //Used for ambiguous chain of commands

    public MultipleCommandProcess(VoiceProcess voiceProcess) { mVoiceProcess = voiceProcess; }

    public MultipleCommandProcess(GlobalState state, ContextActionMap contextActionMap) {
        mVoiceProcess = new VoiceProcess(state, contextActionMap);
    }

    public Queue<String> splitInput(String input) {
        return new LinkedList<>(Arrays.asList(input.split(" and then | and | then ")));
    }

    public Object executeCommand(Queue<String> queue) {
        if (queue == null || queue.size() <= 0) { return null; }

        String command = queue.peek();
        if (command != null) {
            if (mVoiceProcess.isExpectingReply()) {
                queue.clear();  //ignore the rest of the input
                String result = "";
                result += mVoiceProcess.processInput(command);    //process confirmation
                if (!mVoiceProcess.isExpectingReply()) {
                    String nextCommand = mPartialQueue.peek();
                    if (nextCommand != null) {
                        mPartialQueue.remove();
                        result += "\n\n---\n\n" + "Found command: \"" + nextCommand + "\"\n"
                                + mVoiceProcess.processInput(nextCommand);
                        mVoiceProcess.setExpectingMoreInput(mPartialQueue.size() > 0);
                    }
                }
                return result;
            }
            else {
                queue.remove();
                Object result = "Found command: \"" + command + "\"\n"
                        + mVoiceProcess.processInput(command);
                if (mVoiceProcess.isExpectingReply()) {
                    mPartialQueue = new LinkedList<>(queue);
                    queue.clear();
                }
                mVoiceProcess.setExpectingMoreInput(queue.size() > 0
                        || (mPartialQueue != null && mPartialQueue.size() > 0));
                return result;
            }
        } else { return null; }
    }

    public void addDictionary(URL url) throws IOException {
        if (mVoiceProcess != null) { mVoiceProcess.addDictionary(url); }
    }

    public Entity getActionContext() {
        if (mVoiceProcess != null) { return mVoiceProcess.getActionContext(); }
        else { return null; }
    }

    public IDictionary getDictionary() {
        if (mVoiceProcess != null) { return mVoiceProcess.getDictionary(); }
        else { return null; }
    }
}
