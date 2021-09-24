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

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.field.impl.BlobImpl;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class BlobField extends AbstractField {

	@Override
	public String getName() {
		return "blob";
	}

	@Override
	public int getSqlType() {
		return Types.BLOB;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return byte[].class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		throw new CommonException("不支持此方法");
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			Blob value = rs.getBlob(name);
			return get(value);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			Blob value = rs.getBlob(columnIndex);
			return get(value);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Object get(Blob value) throws CommonException {
		try {
			return (value == null) ? null : value.getBytes(1, (int) value.length());
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		BlobImpl blob = new BlobImpl((byte[]) value);
		try {
			ps.setBinaryStream(index, blob.getBinaryStream(), (int) blob.length());
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