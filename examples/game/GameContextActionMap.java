package game;

import com.khan.baron.vcw.ContextActionMap;
import com.khan.baron.vcw.GlobalState;
import game.actions.*;

public class GameContextActionMap extends ContextActionMap {
    public GameContextActionMap(GlobalState state) {
        super(state);
        setActionList(                      "attack",           "heal",             "move");
        addDefaultContextActions(           new Attack(),       new Heal(),         new Move());
        addContextActions("weapon",         new AttackWeapon(), null,               new Move());
        addContextActions("healing-item",   null,               new HealPotion(),   new Move());
    }
}
