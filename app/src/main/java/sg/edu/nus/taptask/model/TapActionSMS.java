package sg.edu.nus.taptask.model;

import android.telephony.SmsManager;
import android.content.Context;

/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionSMS extends TapAction {

    private String smsContent;
    private String phoneNum;


    public TapActionSMS(TapPattern pattern, String smsContent, String phoneNum) {
        super(pattern);
        this.smsContent = smsContent;
        this.phoneNum = phoneNum;
    }

    public boolean performAction(Context context) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNum, null, smsContent, null, null);
//        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        return true;
    }

}
