package call;

import call.entities.Audio;
import call.entities.Contact;
import call.entities.Video;
import com.khan.baron.vcw.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class CallState extends GlobalState {
    private ContextActionMap mMap = new CallContextActionMap(this);
    private VoiceProcess mVoiceProcess = new VoiceProcess(this, mMap);
    private MultipleCommandProcess mCommandProcess = new MultipleCommandProcess(mVoiceProcess);

    public CallState() {
        List<Entity> contacts = new ArrayList<Entity>(Arrays.asList(new Contact("fred"), new Contact("jane")));
        mMap.addPossibleTargets(contacts);
        mMap.addPossibleContexts(contacts);
        mMap.addPossibleTargets(Arrays.asList(new Video(), new Audio()));
        mMap.addPossibleContexts(Arrays.asList(new Video(), new Audio()));
    }

    public void addDictionary(URL url) throws IOException {
        mCommandProcess.addDictionary(url);
    }

    public String updateState(String input) {
        String output = "";
        Queue<String> commandQueue = mCommandProcess.splitInput(input);
        while (!commandQueue.isEmpty()) {
            output += mCommandProcess.executeCommand(commandQueue) + ((commandQueue.isEmpty()) ? "" : " >>> ");
        }
        return output;
    }
}
