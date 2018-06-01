package game.actions;

import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;

public class Attack extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        return "ATTACK";
    }
}
