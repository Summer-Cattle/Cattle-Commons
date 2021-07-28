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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

import io.github.summercattle.commons.db.constants.DatabaseType;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.DateUtils;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class TimestampField extends AbstractField {

	private final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

	@Override
	public String getName() {
		return "timestamp";
	}

	@Override
	public int getSqlType() {
		return Types.TIMESTAMP;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return Date.class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		return DateFormatUtils.format((Date) value, TIMESTAMP_FORMAT);
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			return rs.getTimestamp(name);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			return rs.getTimestamp(columnIndex);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			Timestamp ts = (Timestamp) ReflectUtils.convertValue(ClassType.Timestamp, value);
			ps.setTimestamp(index, convertTimestamp(dialect, ts));
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Timestamp convertTimestamp(Dialect dialect, Timestamp timestamp) throws CommonException {
		Timestamp result = timestamp;
		if (dialect.getType() == DatabaseType.Sybase) {
			int year = DateUtils.getYear(timestamp);
			if (year < 1753) {
				result = new Timestamp(DateUtils.getDate(1753, 1, 1).getTime());
			}
			else if (year > 9999) {
				result = new Timestamp(DateUtils.getDate(9999, 12, 31, 23, 59, 59, 999).getTime());
			}
		}
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
		long xTime = ((java.util.Date) x).getTime();
		long yTime = ((java.util.Date) y).getTime();
		boolean xts = x instanceof Timestamp;
		boolean yts = y instanceof Timestamp;
		int xNanos = xts ? ((Timestamp) x).getNanos() : 0;
		int yNanos = yts ? ((Timestamp) y).getNanos() : 0;
		Timestamp t = new Timestamp(0);
		t.setNanos(5 * 1000000);
		boolean jvmHasJDK14Timestamp = t.getTime() == 5;
		if (!jvmHasJDK14Timestamp) {
			xTime += xNanos / 1000000;
			yTime += yNanos / 1000000;
		}
		if (xTime != yTime) {
			return false;
		}
		if (xts && yts) {
			// both are Timestamps
			int xn = xNanos % 1000000;
			int yn = yNanos % 1000000;
			return xn == yn;
		}
		else {
			// at least one is a plain old Date
			return true;
		}
	}
}