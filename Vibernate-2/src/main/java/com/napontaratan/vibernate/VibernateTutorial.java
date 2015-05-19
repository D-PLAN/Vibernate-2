package com.napontaratan.vibernate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.napontaratan.vibernate.tutorials.*;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.Random;

/**
 * Created by napontaratan on 15-04-26.
 */
public class VibernateTutorial extends FragmentActivity{
    private static final Random RANDOM = new Random();

    TutorialFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_parent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(MainActivity.FIRST_LAUNCH, false);
        editor.commit();

        mAdapter = new TutorialFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
    }

    private class TutorialFragmentAdapter extends FragmentPagerAdapter {


        public TutorialFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Tutorial1();
                case 1:
                    return new Tutorial2();
                case 2:
                    return new Tutorial3();
                case 3:
                    return new Tutorial4();
                case 4:
                    return new Tutorial5();
                case 5:
                    return new Tutorial6();
                case 6:
                    return new Tutorial7();
                default:
                    return new Tutorial1();
            }

        }

        @Override
        public int getCount() {
            return 7;
        }
    }


}
