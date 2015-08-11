package com.napontaratan.vibernate;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

/**
 * Created by napontaratan on 15-04-26.
 */
public class VibernateTutorial extends FragmentActivity{

    private TutorialFragmentAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private TextView titleText;
    private TextView instruction;
    private Button beginButton;

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_parent);

        mAdapter = new TutorialFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        titleText = (TextView) findViewById(R.id.titleText);
        instruction = (TextView) findViewById(R.id.instruction);

        beginButton = (Button) findViewById(R.id.tutorial_finish);
        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(MainActivity.FIRST_LAUNCH, false);
                editor.commit();
                finish();
            }
        });

        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        ViewPager.SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIndicator.onPageSelected(position);
                if(position < 5) {
                    titleText.setText(R.string.tutorial_instruction);
                    beginButton.setVisibility(View.GONE);
                    instruction.setVisibility(View.VISIBLE);
                    instruction.setText(getTutorialInstruction(position));
                    instruction.startAnimation(fadeIn);
                } else if(position == 5) { // get begin button instead
                    titleText.setText(getTutorialInstruction(position));
                    instruction.setVisibility(View.GONE);
                    beginButton.setVisibility(View.VISIBLE);
                } else if(position == 6) {
                    titleText.setText(R.string.tutorial_instruction_credits);

                }
            }
        };

        mPager.setOnPageChangeListener(listener);
    }

    private String getTutorialInstruction(int position) {
        int resId = getResources().getIdentifier("tutorial_instruction_" + position, "string", VibernateTutorial.this.getPackageName());
        return getResources().getString(resId);
    }

    private class TutorialFragmentAdapter extends FragmentPagerAdapter {

        public TutorialFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 7;
        }
    }

    public static class TutorialFragment extends Fragment {

        public static TutorialFragment newInstance(int position) {
            TutorialFragment fragment = new TutorialFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
            View view = inflater.inflate(R.layout.tutorial_layout,container,false);
            ImageView image = (ImageView) view.findViewById(R.id.tutorialImageView);
            int position = getArguments().getInt("position", 0);
            if(position == 6) {
                view = inflater.inflate(R.layout.tutorial_layout_end, container, false);
            } else {
                image.setImageDrawable(getTutorialImage(position));
                if(position == 0) {
                    Animation fade_slide = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein_slide);
                    image.startAnimation(fade_slide);
                }
            }
            return view;
        }

        private Drawable getTutorialImage(int position) {
            int resId = getResources().getIdentifier("tutorial_image_" + position, "drawable", getActivity().getPackageName());
            return getResources().getDrawable(resId);
        }
    }
}
