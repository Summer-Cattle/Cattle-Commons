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
import java.sql.Types;

import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class BooleanField extends AbstractField {

	@Override
	public String getName() {
		return "boolean";
	}

	@Override
	public int getSqlType() {
		return Types.BIT;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return Boolean.class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		return value.toString();
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			return rs.getBoolean(name) ? Boolean.TRUE : Boolean.FALSE;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			return rs.getBoolean(columnIndex) ? Boolean.TRUE : Boolean.FALSE;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			ps.setBoolean(index, (boolean) ReflectUtils.convertValue(ClassType.Boolean, value));
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public boolean isEqual(Object x, Object y) {
		return x == y || (x != null && y != null && ((Boolean) x).compareTo((Boolean) y) == 0);
	}
}