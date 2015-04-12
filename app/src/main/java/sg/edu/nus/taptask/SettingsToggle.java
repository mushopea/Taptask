/*
>  SettingsToggle.java
>
>  Copyright (c) 2014 Geographic Information Services, Inc
>
>  Released under an MIT License
>
>  https://github.com/gisinc/android-toggle-button
> */
package sg.edu.nus.taptask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
public class SettingsToggle extends RelativeLayout implements View.OnClickListener {

    FrameLayout layout;
    LinearLayout outerLayout;
    View toggleCircle, background_oval_off, background_oval_on;
    int dimen;

    private Boolean _crossfadeRunning = false;
    private SharedPreferences _sp;
    private ObjectAnimator _oaLeft, _oaRight;
    private String _prefName;

    public SettingsToggle(Context context, AttributeSet attrs) {
        super(context, attrs);

        String text, textColor, bgDrawableOff, bgDrawableOn;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.settings_toggle, this, true);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SettingsToggle);
        bgDrawableOff = a.getString(R.styleable.SettingsToggle_oval_background_off);
        bgDrawableOn = a.getString(R.styleable.SettingsToggle_oval_background_on);
        _prefName = a.getString(R.styleable.SettingsToggle_prefName);
        text = a.getString(R.styleable.SettingsToggle_toggle_text);
        textColor = a.getString(R.styleable.SettingsToggle_toggle_textColor);
        a.recycle();

        background_oval_off = findViewById(R.id.background_oval_off);
        background_oval_on = findViewById(R.id.background_oval_on);
        toggleCircle = findViewById(R.id.toggleCircle);
        layout = (FrameLayout)findViewById(R.id.layout);
        outerLayout = (LinearLayout)findViewById(R.id.outer_layout);

        if (bgDrawableOff != null) {
            int id = getResources().getIdentifier(bgDrawableOff, "drawable", context.getPackageName());
            background_oval_off.setBackground(getResources().getDrawable(id));
        }
        if (bgDrawableOn != null) {
            int id = getResources().getIdentifier(bgDrawableOn, "drawable", context.getPackageName());
            background_oval_on.setBackground(getResources().getDrawable(id));
        }


        outerLayout.setOnClickListener(this);

        //get a pixel size for a particular dimension - will differ by device according to screen density
        dimen = getResources().getDimensionPixelSize(R.dimen.settings_toggle_width);
        _oaLeft = ObjectAnimator.ofFloat(toggleCircle, "x", dimen/2, 0).setDuration(250);
        _oaRight = ObjectAnimator.ofFloat(toggleCircle, "x", 0, dimen/2).setDuration(250);

        _sp = context.getSharedPreferences(context.getString(R.string.preferences_key), Context.MODE_PRIVATE);

        setState();
    }

    public SettingsToggle(Context context) {
        this(context, null);
    }

    public void setState() {
        if (isInEditMode()) return; //isInEditMode(): if being rendered in IDE preview, skip code that will break

        if (_sp.getBoolean(_prefName, false)) {
            toggleCircle.setX(dimen/2);
            _crossfadeViews(background_oval_off, background_oval_on, 1);
        }
        else {
            toggleCircle.setX(0);
            _crossfadeViews(background_oval_on, background_oval_off, 1);
        }
    }

    private void _crossfadeViews(final View begin, View end, int duration) {
        _crossfadeRunning = true;

        end.setAlpha(0f);
        end.setVisibility(View.VISIBLE);
        end.animate().alpha(1f).setDuration(duration).setListener(null);
        begin.animate().alpha(0f).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                begin.setVisibility(View.GONE);
                _crossfadeRunning = false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (isAnimating()) {
            return;
        }

        SharedPreferences.Editor editor = _sp.edit();
        boolean pref = isOn();
        editor.putBoolean(_prefName, !pref);
        editor.apply();

        super.callOnClick();

        if (pref) {
            _oaLeft.start();
            _crossfadeViews(background_oval_on, background_oval_off, 110);
        }
        else {
            _oaRight.start();
            _crossfadeViews(background_oval_off, background_oval_on, 400);
        }
    }

    public boolean isOn() {
        return _sp.getBoolean(_prefName, false);
    }

    public boolean isAnimating() {
        return (_oaLeft.isRunning() || _oaRight.isRunning() || _crossfadeRunning);
    }
}
