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
package io.github.summercattle.commons.db.field;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateFormatUtils;

import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class DateField extends AbstractField {

	private final String DATE_FORMAT = "dd MMMM yyyy";

	@Override
	public String getName() {
		return "date";
	}

	@Override
	public int getSqlType() {
		return Types.DATE;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return java.util.Date.class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		return DateFormatUtils.format((java.util.Date) value, DATE_FORMAT);
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			Date date = rs.getDate(name);
			return get(date);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			Date date = rs.getDate(columnIndex);
			return get(date);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Object get(Date date) {
		return date != null ? new java.util.Date(date.getTime()) : null;
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			Date sqlDate = (Date) ReflectUtils.convertValue(ClassType.SqlDate, value);
			ps.setDate(index, convertDate(dialect, sqlDate));
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Date convertDate(Dialect dialect, Date date) throws CommonException {
		Date result = date;
//		if (dialect.getType() == Database.Sybase) {
//			int year = DateUtils.getYear(date);
//			if (year < 1753) {
//				result = new Date(DateUtils.getDate(1753, 1, 1).getTime());
//			}
//			else if (year > 9999) {
//				result = new Date(DateUtils.getDate(9999, 12, 31, 23, 59, 59, 999).getTime());
//			}
//		}
		return result;
	}

	@Override
	public boolean isEqual(Object x, Object y) {
		if (x == y) {
			return true;
		}
		if (x == null || y == null) {
			return false;
		}
		java.util.Date xdate = (java.util.Date) x;
		java.util.Date ydate = (java.util.Date) y;
		if (xdate.getTime() == ydate.getTime()) {
			return true;
		}
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(xdate);
		calendar2.setTime(ydate);
		return calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
				&& calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
	}
}