package sg.edu.nus.taptask.model;

import android.telephony.SmsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionSMS extends TapAction {

    private String smsContent;
    private String targetNum;
    private String targetName;
    //private final String SMS_URL = "sms:";


    public TapActionSMS(TapPattern pattern, String smsContent, String targetNum, String targetName) {
        super(pattern);
        this.smsContent = smsContent;
        this.targetNum = targetNum;
        this.targetName = targetName;
    }

    public String getDetails(){
        return smsContent;
    }

    public String getName() {
        return "SMS " + targetName + " (" + targetNum + ")";
    }

    public String getImage() {
        return "task_icon_message";
    }

    public boolean performAction(Context context) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(targetNum, null, smsContent, null, null);
        return true;
    }

// Another way to send SMS via creating a new activity
//    public boolean performAction(Context context) {
//        String url = SMS_URL + phoneNum;
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        context.startActivity(intent);
//        return true;
//    }

}
