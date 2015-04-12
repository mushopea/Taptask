package sg.edu.nus.taptask.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionCall extends TapAction  {

    private String phoneNum;
    private final String CALL_URL = "tel:";

    public TapActionCall(TapPattern pattern, String phoneNum) {
        super(pattern);
        this.phoneNum = phoneNum;
    }

    public boolean performAction(Context context) {
        String url = CALL_URL + phoneNum;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        context.startActivity(intent);
        return true;
    }

}
