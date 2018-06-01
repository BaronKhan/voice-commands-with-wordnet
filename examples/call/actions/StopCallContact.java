package call.actions;

import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;

public class StopCallContact extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        return "STOP_"+Action.getCurrentContext().getName().toUpperCase();
    }
}
