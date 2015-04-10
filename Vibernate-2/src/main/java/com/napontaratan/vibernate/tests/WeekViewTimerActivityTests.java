package com.napontaratan.vibernate.tests;

import android.graphics.Color;
import android.test.ActivityInstrumentationTestCase2;
import com.napontaratan.vibernate.MainActivity;
import com.napontaratan.vibernate.model.TimerConflictException;
import com.napontaratan.vibernate.model.TimerSession;
import com.napontaratan.vibernate.model.TimerSessionHolder;

import java.util.*;

/**
 * Created by daniel on 2015-04-02.
 * https://developer.android.com/training/activity-testing/activity-basic-testing.html
 */
public class WeekViewTimerActivityTests extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActiivty;
    private static final String MOCK_TIMER_NAME = "MOCK TIMER";
    private static final int MOCK_TIMER_COLOR =Color.rgb(106, 125, 137);

    public WeekViewTimerActivityTests() {
        super(MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActiivty = getActivity();
        assertNotNull("mActiivty is null", mActiivty);
    }


    public void testAddTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();
        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);
        start = createCalendar(8,0,0,0);
        end = createCalendar(9, 0, 0, 0);
        TimerSession two = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR, 2);
        start = createCalendar(15,0,0,0);
        end = createCalendar(17, 0, 0, 0);
        TimerSession three = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, false, false, false, false, true, false}, MOCK_TIMER_COLOR, 3);
        start = createCalendar(12,0,0,0);
        end = createCalendar(15, 0, 0, 0);
        TimerSession four = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, false, true, false, false, false, false}, MOCK_TIMER_COLOR, 4);
        start = createCalendar(17,0,0,0);
        end = createCalendar(18, 0, 0, 0);
        TimerSession five = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[]{true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 5);
        try {
            timerSessionHolder.addTimer(one, two, three, four, five);
        } catch (TimerConflictException e) {
            fail();
        }
    }

    public void testAddNonConflictTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        start = createCalendar(12 ,0,0,0);
        end = createCalendar(17, 0, 0, 0);
        TimerSession two = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 2);

        start = createCalendar(17 ,0,0,0);
        end = createCalendar(18, 0, 0, 0);
        TimerSession three = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 3);

        // irrelevant timers on different day
        start = createCalendar(8 ,0,0,0);
        end = createCalendar(12, 0, 0, 0);
        TimerSession four = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR, 4);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(two);
            timerSessionHolder.addTimer(three);
            timerSessionHolder.addTimer(four);
        } catch (TimerConflictException e) {
            fail();
        }
    }

    public void testAddDuplicateTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        // same time
        start = createCalendar(8,0,0,0);
        end = createCalendar(12, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }
    }


    public void testAddDuplicateStartTimeTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        // same start time
        start = createCalendar(8,0,0,0);
        end = createCalendar(9, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR,2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }
    }

    public void testAddDuplicateEndTimeTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        // same end time
        start = createCalendar(11,0,0,0);
        end = createCalendar(12, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR,2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }
    }

    public void testAddStartTimeOverlappedTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        // start time overlapped
        start = createCalendar(7,0,0,0);
        end = createCalendar(9, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR,2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }
    }

    public void testAddEndTimeOverlappedTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        // end time overlapped
        start = createCalendar(11,0,0,0);
        end = createCalendar(13, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }
    }

    public void testAddOverlappedTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        // longer duration but time overlapped
        start = createCalendar(7,0,0,0);
        end = createCalendar(13, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }
    }

    public void testAddMultiDaysConflictTimers() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, true, false, false, true, false}, MOCK_TIMER_COLOR, 1);

        start = createCalendar(9,0,0,0);
        end = createCalendar(11, 0, 0, 0);
        TimerSession timer = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, true, true, false, false, false, true}, MOCK_TIMER_COLOR ,2);

        try {
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(timer);
            fail("Should have thrown TimerConflictException");
        } catch(TimerConflictException e) {

        }

    }

    public void testGetTimersByDay() {
        // make sure we get all the correct timers for a particular day in a sorted order
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR,  1);

        start = createCalendar(8,0,0,0);
        end = createCalendar(9, 0, 0, 0);
        TimerSession two = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR,  2);

        start = createCalendar(9,0,0,0);
        end = createCalendar(10, 0, 0, 0);
        TimerSession three = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR, 3);

        start = createCalendar(11,0,0,0);
        end = createCalendar(12, 0, 0, 0);
        TimerSession four = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR, 4);

        start = createCalendar(13,0,0,0);
        end = createCalendar(14, 0, 0, 0);
        TimerSession five = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR, 5);


        try {
            List<TimerSession> empty = new ArrayList<TimerSession>();
            assertEquals(empty, timerSessionHolder.timerOnThisDay(0));

            timerSessionHolder.addTimer(one);
            List<TimerSession> result = new ArrayList<TimerSession>();
            result.add(one);
            assertEquals(result, timerSessionHolder.timerOnThisDay(0));
            assertEquals(empty, timerSessionHolder.timerOnThisDay(1));

            timerSessionHolder.removeAll();
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(two);
            result = new ArrayList<TimerSession>();
            result.add(one);
            assertEquals(result, timerSessionHolder.timerOnThisDay(0));
            result = new ArrayList<TimerSession>();
            result.add(two);
            assertEquals(result, timerSessionHolder.timerOnThisDay(1));
            assertEquals(empty, timerSessionHolder.timerOnThisDay(3));

            // make sure it's sorted
            timerSessionHolder.removeAll();
            timerSessionHolder.addTimer(two,three,four,five);
            result = new ArrayList<TimerSession>();
            result.add(five);
            result.add(four);
            result.add(three);
            result.add(two);
            assertNotSame(result, timerSessionHolder.timerOnThisDay(1));
            // manually add timers in correct order
            result = new ArrayList<TimerSession>();
            result.add(two);
            result.add(three);
            result.add(four);
            result.add(five);
            assertEquals(result, timerSessionHolder.timerOnThisDay(1));

        } catch (TimerConflictException e) {
           fail();
        }

    }

    public void testGetTimerById() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();
        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        try {
            timerSessionHolder.addTimer(one);
            assertEquals(one, timerSessionHolder.getTimerById(1));
            assertEquals(null, timerSessionHolder.getTimerById(2));
        } catch (TimerConflictException e) {
            e.printStackTrace();
        }
    }

    public void testRemoveTimer() {
        TimerSessionHolder timerSessionHolder = TimerSessionHolder.getInstance();
        boolean removed = timerSessionHolder.removeTimer(null);
        assertEquals(false, removed);

        Calendar start = createCalendar(8,0,0,0);
        Calendar end = createCalendar(12, 0, 0, 0);
        TimerSession one = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { true, false, false, false, false, false, false}, MOCK_TIMER_COLOR, 1);

        start = createCalendar(8,0,0,0);
        end = createCalendar(9, 0, 0, 0);
        TimerSession two = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.VIBRATE, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR,  2);

        start = createCalendar(9,0,0,0);
        end = createCalendar(10, 0, 0, 0);
        TimerSession three = new TimerSession(MOCK_TIMER_NAME, TimerSession.TimerSessionType.SILENT, start, end, new boolean[] { false, true, false, false, false, false, false}, MOCK_TIMER_COLOR, 3);

        try {
            timerSessionHolder.addTimer(one);
            boolean isRemoved = timerSessionHolder.removeTimer(one);
            assertTrue(isRemoved);
            // check that timer has been removed from both list and map
            removedFromAll(one, timerSessionHolder);

            // remove the correct timer
            timerSessionHolder.addTimer(one);
            timerSessionHolder.addTimer(two);
            isRemoved = timerSessionHolder.removeTimer(two);
            assertTrue(isRemoved);
            removedFromAll(two, timerSessionHolder);
            getFromAll(one, timerSessionHolder);

            // doesn't remove a non existing timer which isn't null
            timerSessionHolder.addTimer(two);  // two was removed previously
            isRemoved = timerSessionHolder.removeTimer(three); // three wasn't added
            assertFalse(isRemoved);
            getFromAll(one, timerSessionHolder);
            getFromAll(two, timerSessionHolder);
        } catch (TimerConflictException e) {
            fail("Timer should be removed");
        }

    }

    private void getFromAll(TimerSession timer, TimerSessionHolder timerSessionHolder) {
        TimerSession retrievedTimer = timerSessionHolder.getTimerById(timer.getId());
        assertEquals(timer, retrievedTimer);
        boolean [] days = timer.getDays();
        for(int i = 0; i < days.length; i++) {
            if(days[i]) {
                boolean hasTimer = timerSessionHolder.timerOnThisDay(i).contains(timer);
                assertTrue(hasTimer);
            }
        }
        TimerSession containedTimer = null;
        for(TimerSession eachTimer: timerSessionHolder) {
            if(eachTimer.getId() == timer.getId()) {
                containedTimer = eachTimer;
                break;
            }
        }
        assertEquals(timer, containedTimer);
    }

    private void removedFromAll(TimerSession timer, TimerSessionHolder timerSessionHolder) {
        TimerSession retrievedTimer = timerSessionHolder.getTimerById(timer.getId()); // from map
        assertNull(retrievedTimer);
        boolean [] days = timer.getDays();
        for(int i = 0; i < days.length; i++) {
            if(days[i]) {
                boolean hasTimer = timerSessionHolder.timerOnThisDay(i).contains(timer);
                assertFalse(hasTimer);
            }
        }
        for(TimerSession eachTimer : timerSessionHolder) {
            assertNotSame(eachTimer.getId(), timer.getId());
        }
    }

    private Calendar createCalendar(int hour, int min, int second, int millis) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millis);
        return cal;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
