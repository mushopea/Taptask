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
    private String phoneNum;
    //private final String SMS_URL = "sms:";


    public TapActionSMS(TapPattern pattern, String smsContent, String phoneNum) {
        super(pattern);
        this.smsContent = smsContent;
        this.phoneNum = phoneNum;
    }

    public boolean performAction(Context context) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, smsContent, null, null);
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
