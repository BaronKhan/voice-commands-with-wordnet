package call.entities;

import com.khan.baron.vcw.Entity;

public class Contact extends Entity {
    public Contact(String name) {
        super(name);
        setContext("contact");
    }
}