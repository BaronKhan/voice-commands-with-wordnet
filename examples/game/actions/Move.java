package game.actions;

import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;
import game.entities.Direction;

public class Move extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        if (currentTarget instanceof Direction) {
            return "MOVE_"+currentTarget.getName().toUpperCase();
        }
        return "ERROR";
    }
}