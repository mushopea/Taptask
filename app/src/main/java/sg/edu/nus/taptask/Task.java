package sg.edu.nus.taptask;

import java.util.Date;

/**
 * Created by musho on 24/3/2015.
 */
public class Task {
    public String name;
    public int type;
    public Date lastTriggeredTime;
/*
    public int getImageResourceId() {
        try {
            return context.getResources().getIdentifier(this.type, "drawable", context.getPackageName());

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
*/
}
