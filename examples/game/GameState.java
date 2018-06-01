package game;

import com.khan.baron.vcw.*;
import game.entities.Direction;
import game.entities.Potion;
import game.entities.Sword;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Queue;

public class GameState extends GlobalState {
    private ContextActionMap mMap = new GameContextActionMap(this);
    private VoiceProcess mVoiceProcess = new VoiceProcess(this, mMap);
    private MultipleCommandProcess mCommandProcess = new MultipleCommandProcess(mVoiceProcess);

    public GameState() {
        mMap.addPossibleContexts(Arrays.asList(new Sword(), new Potion()));
        mMap.addPossibleTargets(Arrays.asList((Entity) new Direction("forwards"), new Direction("backwards")));
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
