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

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.field.impl.NClobImpl;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class NClobField extends AbstractField {

	@Override
	public String getName() {
		return "nclob";
	}

	@Override
	public int getSqlType() {
		return Types.NCLOB;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return String.class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		throw new CommonException("不支持此方法");
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			NClob nclob = rs.getNClob(name);
			return get(nclob);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			NClob nclob = rs.getNClob(columnIndex);
			return get(nclob);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Object get(NClob value) throws CommonException {
		if (value == null) {
			return null;
		}
		try (BufferedReader bufferedReader = new BufferedReader(value.getCharacterStream())) {
			StringBuffer sb = new StringBuffer();
			int c;
			while ((c = bufferedReader.read()) != -1) {
				sb.append((char) c);
			}
			return sb.toString();
		}
		catch (SQLException | IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		NClobImpl nclob = new NClobImpl((String) value);
		try {
			ps.setNCharacterStream(index, nclob.getCharacterStream(), (int) nclob.length());
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public boolean isEqual(Object x, Object y) {
		return x == y;
	}
}