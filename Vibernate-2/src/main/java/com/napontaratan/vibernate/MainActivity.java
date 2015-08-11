package com.napontaratan.vibernate;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.napontaratan.vibernate.dispatcher.Dispatcher;
import com.napontaratan.vibernate.model.*;
import com.napontaratan.vibernate.store.TimerSessionStore;

public class MainActivity extends ActionBarActivity {

    public static String FIRST_LAUNCH = "Tutorial";

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    private void setup() {
        VibernateLogger.init();
        setupAnalytics();
        setupTutorial();
        setupDispatcherAndStore(getApplicationContext());
        setupResources();
        setupViews();
    }

    private void setupAnalytics() {
        /* Analytics Code */
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        Tracker tracker = analytics.newTracker("UA-XXXXX-Y");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        analytics.reportActivityStart(this);
    }

    private void setupTutorial() {
        /* Tutorial Code */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstLaunch = prefs.getBoolean(FIRST_LAUNCH, true);

        if(isFirstLaunch) {
            Intent tutorial = new Intent();
            tutorial.setClass(MainActivity.this, VibernateTutorial.class);
            startActivity(tutorial);
        }
    }

    public static void setupDispatcherAndStore(Context ctx) {
        TimerSessionStore timerSessionStore = TimerSessionStore.getInstance();
        timerSessionStore.setupStore(ctx);
        Dispatcher.getInstance().registerStore(timerSessionStore);
    }

    private void setupResources() {
        TimerSessionCommonView.setupImageResources(getResources());
    }

    private void setupViews() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.main);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                final MenuItem calendar_btn = toolbar.getMenu().getItem(0);
                final MenuItem list_btn = toolbar.getMenu().getItem(1);
                switch (i) {
                    case 0:
                        calendar_btn.setIcon(R.drawable.ic_action_go_to_today_selected);
                        list_btn.setIcon(R.drawable.ic_action_view_as_list);
                        break;
                    case 1:
                        calendar_btn.setIcon(R.drawable.ic_action_go_to_today);
                        list_btn.setIcon(R.drawable.ic_action_view_as_list_selected);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        final FloatingActionsMenu floatingAddButton = (FloatingActionsMenu) findViewById(R.id.floating_add_button);
        FloatingActionButton oneTimeAddButton = (FloatingActionButton) findViewById(R.id.onetime_add_button);
        final Intent addTimerIntent = new Intent();
        addTimerIntent.setClass(MainActivity.this, CreateTimerActivity.class);
        oneTimeAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingAddButton.toggle();
                addTimerIntent.putExtra(VibernateSettings.TIMER_ADD_KEY, TimerSession.TimerAddType.ONETIME);
                startActivity(addTimerIntent);
            }
        });
        FloatingActionButton recurringAddButton = (FloatingActionButton) findViewById(R.id.recurring_add_button);
        recurringAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingAddButton.toggle();
                addTimerIntent.putExtra(VibernateSettings.TIMER_ADD_KEY, TimerSession.TimerAddType.RECURRING);
                startActivity(addTimerIntent);
            }
        });

        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                switch (item.getItemId()) {
                    case R.id.action_calendar:
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.action_list:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.action_tutorial:
                        Intent tutorial = new Intent();
                        tutorial.setClass(MainActivity.this, VibernateTutorial.class);
                        startActivity(tutorial);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WeekViewFragment();
                case 1:
                    return new ListViewFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }
}