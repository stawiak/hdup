package com.os.rest.exchange;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for various date related functions.
 *
 * @author uwe
 */
public class DateHelper {

	public static final long ONE_MIN_MS = 60 * 1000;
	public static final int THIS_MONTH = 0;
	public static final int LAST_MONTH = 1;
	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {

		{
			put("^\\d{8}$", "yyyyMMdd");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
			put("^\\d{12}$", "yyyyMMddHHmm");
			put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
			put("^\\d{14}$", "yyyyMMddHHmmss");
			put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		}
	};

	/**
	 * Determine SimpleDateFormat pattern matching with the given date string.
	 * Returns null if format is unknown. You can simply extend DateUtil with
	 * more formats if needed.
	 *
	 * @param dateString The date string to determine the SimpleDateFormat
	 * pattern for.
	 * @return The matching SimpleDateFormat pattern, or null if format is
	 * unknown.
	 * @see SimpleDateFormat
	 */
	public static String determineDateFormat(String dateString) {
		for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
			if (dateString.toLowerCase().matches(regexp)) {
				return DATE_FORMAT_REGEXPS.get(regexp);
			}
		}
		return null; // Unknown format.
	}

	/**
	 * Get the beginning of this month
	 *
	 * @return the beginning of the month
	 */
	public static long getBeginningOfThisMonth() {
		return (getBeginningOfMonth(THIS_MONTH));
	}

	/**
	 * Get the beginning of last month
	 *
	 * @return the beginning of last month
	 */
	public static long getBeginningOfLastMonth() {
		return (getBeginningOfMonth(LAST_MONTH));
	}

	/**
	 * Get the end of this month
	 *
	 * @return the end of this month
	 */
	public static long getEndOfThisMonth() {
		return (getEndOfMonth(THIS_MONTH));
	}

	/**
	 * Get the end of last month
	 *
	 * @return the end of last month
	 */
	public static long getEndOfLastMonth() {
		return (getEndOfMonth(LAST_MONTH));
	}

	/**
	 * Get the beginning of today
	 *
	 * @return the 00:00:00 of today
	 */
	public static long getBeginningOfToday() {
		Calendar cal = now();
		setToDayBegin(cal);
		return (cal.getTimeInMillis());
	}

	/**
	 * Get the end of today
	 *
	 * @return 23:59:59 of today
	 */
	public static long getEndOfToday() {
		Calendar cal = now();
		setToDayEnd(cal);
		return (cal.getTimeInMillis());
	}

	/**
	 * Get the beginning of yesterday
	 *
	 * @return the 00:00:00 of today
	 */
	public static long getBeginningOfYesterday() {
		Calendar cal = yesterday();
		setToDayBegin(cal);
		return (cal.getTimeInMillis());
	}

	/**
	 * Get the end of today
	 *
	 * @return 23:59:59 of today
	 */
	public static long getEndOfYesterday() {
		Calendar cal = yesterday();
		setToDayEnd(cal);
		return (cal.getTimeInMillis());
	}

	/**
	 * Get the beginning of the month
	 *
	 * @param which indicates the time frame
	 * @return the time since the epoch
	 */
	public static long getBeginningOfMonth(int which) {
		Calendar cal = now(which);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		setToDayBegin(cal);

		return (cal.getTimeInMillis());
	}

	/**
	 * Get the end of the month
	 *
	 * @param which the time frame
	 * @return the time since the epoch
	 */
	public static long getEndOfMonth(int which) {
		Calendar cal = now(which);
		int lastDate = cal.getActualMaximum(Calendar.DATE);
		cal.set(Calendar.DATE, lastDate);
		setToDayEnd(cal);
		return (cal.getTimeInMillis());
	}

	/**
	 * Get date now
	 *
	 * @return now
	 */
	public static Calendar now() {
		return (now(THIS_MONTH));
	}

	/**
	 * Get yesterday's date
	 *
	 * @return yesterday
	 */
	public static Calendar yesterday() {
		Calendar cal = now();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return (cal);
	}

	public final static long round(long millis, long interval) {
		millis += (interval / 2);
		millis /= interval;
		millis *= interval;
		return millis;
	}

	public final static long roundToMinute(long millis) {
		return round(millis, ONE_MIN_MS);
	}

	public static long beginningOfDay(long millis) {
		Calendar ts = Calendar.getInstance();
		ts.setTimeInMillis(millis);
		setToDayBegin(ts);
		return (ts.getTimeInMillis());
	}

	public static long endOfDay(long millis) {
		Calendar ts = Calendar.getInstance();
		ts.setTimeInMillis(millis);
		setToDayEnd(ts);
		return (ts.getTimeInMillis());
	}

	private static Calendar now(int which) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		switch (which) {
			default:
			case THIS_MONTH:
				break;
			case LAST_MONTH:
				cal.add(Calendar.MONTH, -1);
				break;
		}
		return (cal);
	}

	// rewind to the beginning of the day
	private static void setToDayBegin(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
	}
	// forward to the end of the day

	private static void setToDayEnd(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
	}
}
