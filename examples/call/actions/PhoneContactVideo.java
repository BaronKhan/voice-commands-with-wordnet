package call.actions;

import call.entities.Contact;
import com.khan.baron.vcw.Action;
import com.khan.baron.vcw.Entity;
import com.khan.baron.vcw.GlobalState;

public class PhoneContactVideo extends Action {
    public String execute(GlobalState state, Entity currentTarget) {
        if (currentTarget instanceof Contact) { return "PHONE_"+currentTarget.getName().toUpperCase()+"_VIDEO"; }
        return "PHONE_VIDEO";
    }
}
