package cooking.actions;

import call.entities.Audio;
import call.entities.Contact;
import call.entities.Video;
import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;
import cooking.entities.Food;

public class Stir extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        if (currentTarget instanceof Food) { return "STIR_"+currentTarget.getName().toUpperCase(); }
        return "STIR";
    }
}
