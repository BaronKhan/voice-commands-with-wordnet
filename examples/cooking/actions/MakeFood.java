package cooking.actions;

import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;
import cooking.entities.Food;

public class MakeFood extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        if (currentTarget instanceof Food) {
            return "MAKE_"+currentTarget.getName().toUpperCase()+"_"+Action.getCurrentContext().getName().toUpperCase();
        }
        return "ERROR";
    }
}
