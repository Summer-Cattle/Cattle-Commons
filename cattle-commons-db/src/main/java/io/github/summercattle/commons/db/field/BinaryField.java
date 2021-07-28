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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class BinaryField extends AbstractField {

	@Override
	public String getName() {
		return "binary";
	}

	@Override
	public int getSqlType() {
		return Types.VARBINARY;
	}

	@Override
	public Class< ? > getReturnedClass() {
		return byte[].class;
	}

	@Override
	public String toString(Object value) throws CommonException {
		byte[] bytes = (byte[]) value;
		return Hex.encodeHexString(bytes, false);
	}

	@Override
	public Object get(ResultSet rs, String name) throws CommonException {
		try {
			InputStream inputStream = rs.getBinaryStream(name);
			return get(inputStream);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object get(ResultSet rs, int columnIndex) throws CommonException {
		try {
			InputStream inputStream = rs.getBinaryStream(columnIndex);
			return get(inputStream);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Object get(InputStream inputStream) throws CommonException {
		if (inputStream == null) {
			return null;
		}
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048)) {
			byte[] buffer = new byte[2048];
			while (true) {
				int amountRead = inputStream.read(buffer);
				if (amountRead == -1) {
					break;
				}
				outputStream.write(buffer, 0, amountRead);
			}
			return outputStream.toByteArray();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public void set(Dialect dialect, PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			byte[] internalValue = (byte[]) value;
			ps.setBinaryStream(index, new ByteArrayInputStream(internalValue), internalValue.length);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public boolean isEqual(Object x, Object y) {
		return x == y || (x != null && y != null && Arrays.equals((byte[]) x, (byte[]) y));
	}
}