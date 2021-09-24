/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitlab.summercattle.commons.utils.auxiliary;

import java.util.Calendar;
import java.util.Date;

import com.gitlab.summercattle.commons.exception.CommonException;

public class DateUtils {

	public static int getYear(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static Date getDate(int year, int month, int day) throws CommonException {
		return getDate(year, month, day, 0, 0, 0, 0);
	}

	public static Date getDate(int year, int month, int day, int hour, int minute, int second) throws CommonException {
		return getDate(year, month, day, hour, minute, second, 0);
	}

	public static Date getDate(int year, int month, int day, int hour, int minute, int second, int millisecond) throws CommonException {
		int calendarMonth;
		switch (month) {
			case 1:
				calendarMonth = Calendar.JANUARY;
				break;
			case 2:
				calendarMonth = Calendar.FEBRUARY;
				break;
			case 3:
				calendarMonth = Calendar.MARCH;
				break;
			case 4:
				calendarMonth = Calendar.APRIL;
				break;
			case 5:
				calendarMonth = Calendar.MAY;
				break;
			case 6:
				calendarMonth = Calendar.JUNE;
				break;
			case 7:
				calendarMonth = Calendar.JULY;
				break;
			case 8:
				calendarMonth = Calendar.AUGUST;
				break;
			case 9:
				calendarMonth = Calendar.SEPTEMBER;
				break;
			case 10:
				calendarMonth = Calendar.OCTOBER;
				break;
			case 11:
				calendarMonth = Calendar.NOVEMBER;
				break;
			case 12:
				calendarMonth = Calendar.DECEMBER;
				break;
			default:
				throw new CommonException("无效的月份:" + month);
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, calendarMonth);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millisecond);
		return calendar.getTime();
	}

	public static Date getNextDate(Date date) {
		if (null == date) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTime();
	}

	public static Date getNextWeekDate(Date date) {
		if (null == date) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 7);
		return calendar.getTime();
	}

	public static Date getNextMonthDate(Date date) {
		if (null == date) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);
		return calendar.getTime();
	}

	public static Date getNextYearDate(Date date) {
		if (null == date) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, 1);
		return calendar.getTime();
	}

	public static int getMonthDays(int year, int month) throws CommonException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getDate(year, month, 1));
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
}