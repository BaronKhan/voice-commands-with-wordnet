package cooking.entities;

import com.khan.baron.vcw.Entity;

public class Food extends Entity {
    public Food(String name) {
        super(name);
        setContext("food");
    }
}
