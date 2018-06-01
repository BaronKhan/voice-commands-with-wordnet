package com.khan.baron.vcw;

import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;

/**
 * Created by Baron on 11/01/2018.
 */

public abstract class Action {
    private static Entity sCurrentContext = null;

    public abstract String execute(GlobalState state, Entity currentTarget);

    public Object processReply(GlobalState state, String input) {
        return "Intent not understood.";
    }

    protected boolean mWantsReply = false;
    protected Entity mCurrentTarget;

    public boolean wantsReply() {
        return mWantsReply;
    }

    public void setWantsReply(boolean mWantsReply) {
        this.mWantsReply = mWantsReply;
    }

    protected static Entity getCurrentContext() {
        return sCurrentContext;
    }

    protected static void setCurrentContext(Entity currentContext) {
        sCurrentContext = currentContext;
    }
}
