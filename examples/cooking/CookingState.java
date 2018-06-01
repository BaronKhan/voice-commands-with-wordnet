package cooking;

import com.khan.baron.vcw.*;
import cooking.entities.Cooker;
import cooking.entities.Food;
import cooking.entities.Spoon;
import game.GameContextActionMap;
import game.entities.Direction;
import game.entities.Potion;
import game.entities.Sword;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Queue;

public class CookingState extends GlobalState {
    private ContextActionMap mMap = new CookingContextActionMap(this);
    private VoiceProcess mVoiceProcess = new VoiceProcess(this, mMap);
    private MultipleCommandProcess mCommandProcess = new MultipleCommandProcess(mVoiceProcess);

    public CookingState() {
        Entity egg = new Food("egg");
        Entity soup = new Food("soup");
        Entity spoon = new Spoon();
        Entity cooker = new Cooker();
        mMap.addPossibleTargets(Arrays.asList(egg, soup));
        mMap.addPossibleContexts(Arrays.asList(egg, soup, spoon, cooker));
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
