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
package com.gitlab.summercattle.commons.db.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.reflect.ClassType;
import com.gitlab.summercattle.commons.utils.reflect.ReflectUtils;

public class TimeField extends AbstractField {

	private static final String TIME_FORMAT = "HH:mm:ss";

	@Override
	public String getName() {
		return "time";
	}

	@Override
	public int getSqlType() {
		return Types.TIME;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return Date.class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		return DateFormatUtils.format((Date) value, TIME_FORMAT);
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			return rs.getTime(name);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			return rs.getTime(columnIndex);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			Time time = (Time) ReflectUtils.convertValue(ClassType.Time, value);
			ps.setTime(index, time);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public boolean isEqual(Object x, Object y) {
		if (x == y) {
			return true;
		}
		if (x == null || y == null) {
			return false;
		}
		Date xdate = (Date) x;
		Date ydate = (Date) y;
		if (xdate.getTime() == ydate.getTime()) {
			return true;
		}
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(xdate);
		calendar2.setTime(ydate);
		return calendar1.get(Calendar.HOUR_OF_DAY) == calendar2.get(Calendar.HOUR_OF_DAY)
				&& calendar1.get(Calendar.MINUTE) == calendar2.get(Calendar.MINUTE)
				&& calendar1.get(Calendar.SECOND) == calendar2.get(Calendar.SECOND)
				&& calendar1.get(Calendar.MILLISECOND) == calendar2.get(Calendar.MILLISECOND);
	}
}