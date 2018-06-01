package game.entities;

import com.khan.baron.vcw.Entity;

public class Direction extends Entity {
    public Direction(String dir) {
        super(dir);
        setContext("direction");
    }
}
