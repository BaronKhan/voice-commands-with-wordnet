package call;

import call.actions.*;
import com.khan.baron.vcw.ContextActionMap;
import com.khan.baron.vcw.GlobalState;

public class CallContextActionMap extends ContextActionMap {
    public CallContextActionMap(GlobalState state) {
        super(state);
        setActionList(               "phone",                   "stop",                 "mute");
        addDefaultContextActions(    new PhoneContact(),         new StopCall(),         new Mute());
        addContextActions("video",   new PhoneContactVideo(),    null,                   null);
        addContextActions("audio",   new PhoneContactAudio(),    null,                   null);
        addContextActions("contact", null,                       new StopCallContact(),  null);
    }
}

