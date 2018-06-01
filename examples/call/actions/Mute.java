package call.actions;

import call.entities.Audio;
import call.entities.Contact;
import call.entities.Video;
import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;

public class Mute extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        if (currentTarget instanceof Video) { return "MUTE_VIDEO"; }
        if (currentTarget instanceof Audio) { return "MUTE_AUDIO"; }
        if (currentTarget instanceof Contact) { return "MUTE_"+currentTarget.getName().toUpperCase(); }
        return "MUTE";
    }
}
