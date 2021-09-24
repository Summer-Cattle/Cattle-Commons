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

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public abstract class AbstractField {

	public abstract String getName();

	public abstract int getSqlType();

	public abstract Class< ? > getReturnedClass();

	public abstract String toString(Object value) throws CommonException;

	public abstract Object get(ResultSet rs, int columnIndex) throws CommonException;

	public abstract Object get(ResultSet rs, String name) throws CommonException;

	public abstract void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException;

	public abstract boolean isEqual(Object x, Object y);

	public final boolean nullSafeIsEqual(Object x, Object y) {
		if (x != null && y != null) {
			return isEqual(x, y);
		}
		else {
			if (x != null || y != null) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	public final String nullSafeToString(Object value) throws CommonException {
		return value == null ? null : toString(value);
	}

	public final Object nullSafeGet(ResultSet rs, int columnIndex) throws CommonException {
		return get(rs, columnIndex);
	}

	public final Object nullSafeGet(ResultSet rs, String name) throws CommonException {
		return get(rs, name);
	}

	public final void nullSafeSet(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			if (value == null) {
				ps.setNull(index, getSqlType());
			}
			else {
				set(dialect, ps, index, value);
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}