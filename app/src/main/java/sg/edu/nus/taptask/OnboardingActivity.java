package sg.edu.nus.taptask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import sg.edu.nus.taptask.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class OnboardingActivity extends Activity {

    ViewPager mViewPager;
    SlideShowPagerAdapter mSlideShowPagerAdapter;

    int[] mResources = {
            R.drawable.task_icon_volume,
            R.drawable.task_icon_call,
            R.drawable.task_icon_call_reject,
            R.drawable.task_icon_message,
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
    }

    // The "Got it" button goes to the dashboard
    public void goToMainActivity(View v) {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
