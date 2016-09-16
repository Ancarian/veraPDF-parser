package org.verapdf.tools;

import org.apache.log4j.Logger;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Maksim Bezrukov
 */
public class TypeConverter {
	private static final Logger LOGGER = Logger.getLogger(TypeConverter.class);

	public static Calendar parseDate(String toParse) {
		if (toParse != null
				&& toParse.startsWith("D:")
				&& isDigits(toParse, 2, 4)) {
			int year = Integer.parseInt(toParse.substring(2, 6));
			int month = 1;
			int day = 1;
			int hour = 0;
			int minutes = 0;
			int seconds = 0;
			String sign = "Z";
			int timeZoneHours = 0;
			int timeZoneMins = 0;
			boolean isCorrect = true;

			int length = toParse.length();

			if (length > 6) {
				if (isDigits(toParse, 6, 2)) {
					month = Integer.parseInt(toParse.substring(6, 8));
				} else {
					isCorrect = false;
				}
			}

			if (length > 8) {
				if (isDigits(toParse, 8, 2)) {
					day = Integer.parseInt(toParse.substring(8, 10));
				} else {
					isCorrect = false;
				}
			}
			if (length > 10) {
				if (isDigits(toParse, 10, 2)) {
					hour = Integer.parseInt(toParse.substring(10, 12));
				} else {
					isCorrect = false;
				}
			}
			if (length > 12) {
				if (isDigits(toParse, 12, 2)) {
					minutes = Integer.parseInt(toParse.substring(12, 14));
				} else {
					isCorrect = false;
				}
			}
			if (length > 14) {
				if (isDigits(toParse, 14, 2)) {
					seconds = Integer.parseInt(toParse.substring(14, 16));
				} else {
					isCorrect = false;
				}
			}

			if (length > 16) {
				sign = toParse.substring(16, 17);
				if (!sign.matches("^[Z+-]$")) {
					isCorrect = false;
				}
				if ("Z".equals(sign) && length > 17) {
					isCorrect = false;
				}
			}

			if (length > 17) {
				if (isDigits(toParse, 17, 2)) {
					timeZoneHours = Integer.parseInt(toParse.substring(17, 19));
				} else {
					isCorrect = false;
				}
			}

			if (length > 19) {
				if (!"'".equals(toParse.substring(19, 20))) {
					isCorrect = false;
				}
			}

			if (length > 20) {
				if (isDigits(toParse, 20, 2)) {
					timeZoneMins = Integer.parseInt(toParse.substring(20, 22));
				} else {
					isCorrect = false;
				}
			}

			if (length > 22) {
				isCorrect = false;
			}

			if (isCorrect) {
				Calendar res = new GregorianCalendar(year, month - 1, day, hour, minutes, seconds);
				if (!sign.equals("Z")) {
					res.setTimeZone(TimeZone.getTimeZone("GMT" + sign + timeZoneHours + ":" + timeZoneMins));
				} else {
					res.setTimeZone(TimeZone.getTimeZone("GMT"));
				}
				return res;
			}
		}

		LOGGER.debug("Parsed string is not complies pdf date format");
		return null;
	}

	private static boolean isDigits(String toCheck, int offset, int length) {
		int maxOffset = Math.min(offset + length, toCheck.length());
		for (int i = offset; i < maxOffset; ++i) {
			if (!Character.isDigit(toCheck.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static double[] getRealArray(COSObject array, int estimatedSize, String arrayName) {
		if (arrayName == null) {
			throw new IllegalArgumentException("Array object can not be null");
		}

		if (array != null && array.getType() == COSObjType.COS_ARRAY) {
			int size = array.size();

			if (size != estimatedSize) {
				LOGGER.debug(arrayName + " array doesn't consist of " + estimatedSize + " elements");
			}

			double[] res = new double[size];
			for (int i = 0; i < size; ++i) {
				COSObject number = array.at(i);
				if (number == null || number.getReal() == null) {
					LOGGER.debug(arrayName + " array contains non number value");
					return null;
				} else {
					res[i] = number.getReal();
				}
			}
			return res;
		}
		return null;
	}
}
