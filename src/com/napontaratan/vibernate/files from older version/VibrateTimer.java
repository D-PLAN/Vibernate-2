package com.napontaratan.vibratetimer.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * VibrateTimer model
 * @author Paul, Amelia
 */
public final class VibrateTimer implements Serializable{
	private static final long serialVersionUID = 2881690379292284022L;
	private final Calendar startTime;
	private final Calendar endTime;
	private final int id;
	private final boolean[] days;

	// Constructor
	public VibrateTimer (Calendar startTime, Calendar endTime, boolean[] days, int id) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.days = days;
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public boolean[] getDays() {
		return days;
	}

	public List<Calendar> getStartAlarmCalendars(){
		List<Calendar> calendars = new ArrayList<Calendar>();
		for (int i = 0; i < 7; i++) {
			if (days[i]) {
				Calendar day = Calendar.getInstance();
				day.set(Calendar.DAY_OF_WEEK, getDayOfWeekFromInt(i));
				day.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
				day.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
				day.set(Calendar.SECOND, startTime.get(Calendar.SECOND));
				calendars.add(day);
			}
		}
		return calendars;
	}

	public List<Calendar> getEndAlarmCalendars() {
		List<Calendar> calendars = new ArrayList<Calendar>();
		for (int i = 0; i < 7; i++) {
			if (days[i]) {
				Calendar day = Calendar.getInstance();
				day.set(Calendar.DAY_OF_WEEK, getDayOfWeekFromInt(i));
				day.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
				day.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
				day.set(Calendar.SECOND, endTime.get(Calendar.SECOND));
				calendars.add(day);
			}
		}
		return calendars;
	}

	private int getDayOfWeekFromInt(int day) {
		int dayOfWeek = 0;
		switch(day) {
		case 0:
			dayOfWeek = Calendar.SUNDAY;
			break;
		case 1:
			dayOfWeek = Calendar.MONDAY;
			break;
		case 2:
			dayOfWeek = Calendar.TUESDAY;
			break;
		case 3:
			dayOfWeek = Calendar.WEDNESDAY;
			break;
		case 4:
			dayOfWeek = Calendar.THURSDAY;
			break;
		case 5:
			dayOfWeek = Calendar.FRIDAY;
			break;
		case 6:
			dayOfWeek = Calendar.SATURDAY;
			break;
		}
		return dayOfWeek;
	}

	@Override
	public String toString() {  // for testing purposes
		String response = "VibrateTimer id: " + getId() + " startTime: " + getStartTime() + " endTime: " + getEndTime();
		return response;   
	}

	// ==== need the following to work with the database =====

	/**
	 * Convert a VibrateTimer object into an array of Bytes to be stored into the Database
	 * @param obj - (Object) VibrateTimer object
	 * @return byte[]
	 * @throws IOException
	 * @author Napon
	 */
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}
	/**
	 * Convert an array of Bytes back to its object form
	 * @param byte[] - data
	 * @return (Object) VibrateTimer
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @author Napon
	 */
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}
}