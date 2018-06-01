package cooking.actions;

import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;
import cooking.entities.Food;

public class BoilCooker extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        if (currentTarget instanceof Food) { return "BOIL_"+currentTarget.getName().toUpperCase()+"_COOKER"; }
        return "BOIL_COOKER";
    }
}
