package sg.edu.nus.taptask.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionCall extends TapAction  {

    private String targetNum;
    private String targetName;
    private final String CALL_URL = "tel:";

    public TapActionCall(TapPattern pattern, String targetNum, String targetName) {
        super(pattern);
        this.targetNum = targetNum;
        this.targetName = targetName;
    }

    public boolean performAction(Context context) {
        String url = CALL_URL + targetNum;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

}
