package cooking;

import com.khan.baron.vcw.ContextActionMap;
import com.khan.baron.vcw.GlobalState;
import cooking.actions.*;

public class CookingContextActionMap extends ContextActionMap {
    public CookingContextActionMap(GlobalState state) {
        super(state);
        setActionList(              "make",                    "stir",                 "boil");
        addDefaultContextActions(   new Make(),                new Stir(),             new Boil());
        addContextActions("spoon",  new Make(),                new StirSpoon(),        null);
        addContextActions("cooker", new Make(),                null,                   new BoilCooker());
        addContextActions("food",   new MakeFood(),            null,                   null);
    }
}
