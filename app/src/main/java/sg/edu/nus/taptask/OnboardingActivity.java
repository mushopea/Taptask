package sg.edu.nus.taptask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import sg.edu.nus.taptask.model.TapAction;
import sg.edu.nus.taptask.model.TapPattern;
import sg.edu.nus.taptask.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class OnboardingActivity extends Activity implements AccelerometerSamplerListener{

    ViewPager mViewPager;
    SlideShowPagerAdapter mSlideShowPagerAdapter;
    mehdi.sakout.fancybuttons.FancyButton gotItButton;

    int[] mResources = {
            R.drawable.t1,
            R.drawable.t2,
            R.drawable.t3,
            R.drawable.t4,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_onboarding);

        // Instantiate a ViewPager and a PagerAdapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSlideShowPagerAdapter = new SlideShowPagerAdapter(this);
        mViewPager.setAdapter(mSlideShowPagerAdapter);
        refreshCircleIndicator(0);

        // Set event for view pager
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                refreshCircleIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
        });

        // Customize circle indicator
        TextView circleText = (TextView) findViewById(R.id.circleText);
        Typeface fontawesome = Typeface.createFromAsset(this.getAssets(), "fonts/fontawesome-webfont.ttf");
        circleText.setTypeface(fontawesome);


        // Disable got it button
        gotItButton = (mehdi.sakout.fancybuttons.FancyButton) findViewById(R.id.btn_gotit);
        gotItButton.setText("Calibrating...");
        gotItButton.setEnabled(false);

        // Start accelerometer calibration
        final AccelerometerSampler accelerometerSampler = new AccelerometerSampler(this.getBaseContext());
        accelerometerSampler.setAccelerometerSamplerListener(this);

        // Run calibration on new thread
        new Thread(new Runnable() {
            public void run(){
                accelerometerSampler.calibrateSamplingRate();
            }
        }).start();

    }

    // Slide show adapter
    class SlideShowPagerAdapter  extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public SlideShowPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mResources.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.image_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            imageView.setImageResource(mResources[position]);
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + (position + 1);
        }
    }

    // The "Got it" button goes to the dashboard
    public void goToMainActivity(View v) {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    // draws the circle indicators that indicates current page of the tutorial slider
    public void refreshCircleIndicator(int currPage) {
        Log.e("Refreshing circles", "Onboarding activity page: " + currPage);
        // declare variables
        TextView circleText = (TextView) findViewById(R.id.circleText);
        int totalPages = mSlideShowPagerAdapter.getCount();

        // clear current circle text
        circleText.setText("");

        // draw the circles
        for (int i = 0; i < totalPages; i++) {
            if (i == currPage) {
                circleText.append(getString(R.string.filledcircle) + "  ");
            } else {
                circleText.append(getString(R.string.circle) + "  ");
            }
        }
    }

    @Override
    public void onCalibrationDone() {
        runOnUiThread(new Runnable() {
            public void run() {
                gotItButton.setText("GOT IT!");
                gotItButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onRecordingDelayOver() {}

    @Override
    public void onSamplingStart() {}

    @Override
    public void onSamplingStop() {}

    @Override
    public void onRecordingDone() {}

    @Override
    public void onMatchFound(TapAction tapAction, TapPattern signalPattern, double matchPct) {}
}
