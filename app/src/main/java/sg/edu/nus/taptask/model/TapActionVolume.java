package sg.edu.nus.taptask.model;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by Yiwen on 12/4/2015.
 */
public class TapActionVolume extends TapAction {

    public TapActionVolume(TapPattern pattern) {
        super(pattern);
    }

    public String getName() {
        return "TapActionVol";
    }

    public String getImage() {
        return "task_icon_volume";
    }

    public boolean performAction(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentMode = audio.getRingerMode();
        if(currentMode != audio.RINGER_MODE_SILENT){
            audio.setRingerMode(audio.RINGER_MODE_SILENT);
        } else {
            audio.setRingerMode(audio.RINGER_MODE_NORMAL);
        }
        return true;
    }

}
