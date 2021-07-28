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
package io.github.summercattle.commons.db.field.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public class ClobImpl implements Clob {

	private Reader reader;

	private int length;

	private boolean needsReset = false;

	public ClobImpl(String string) {
		reader = new StringReader(string);
		length = string.length();
	}

	public ClobImpl(Reader reader, int length) {
		this.reader = reader;
		this.length = length;
	}

	@Override
	public long length() throws SQLException {
		return length;
	}

	@Override
	public void truncate(long pos) throws SQLException {
		excep();
	}

	@Override
	public InputStream getAsciiStream() throws SQLException {
		try {
			if (needsReset) {
				reader.reset();
			}
		}
		catch (IOException ioe) {
			throw new SQLException("could not reset reader");
		}
		needsReset = true;
		return new ReaderInputStream(reader);
	}

	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException {
		excep();
		return null;
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		try {
			if (needsReset) {
				reader.reset();
			}
		}
		catch (IOException ioe) {
			throw new SQLException("could not reset reader");
		}
		needsReset = true;
		return reader;
	}

	@Override
	public Writer setCharacterStream(long pos) throws SQLException {
		excep();
		return null;
	}

	@Override
	public String getSubString(long pos, int len) throws SQLException {
		excep();
		return null;
	}

	@Override
	public int setString(long pos, String string) throws SQLException {
		excep();
		return 0;
	}

	@Override
	public int setString(long pos, String string, int i, int j) throws SQLException {
		excep();
		return 0;
	}

	@Override
	public long position(String string, long pos) throws SQLException {
		excep();
		return 0;
	}

	@Override
	public long position(Clob colb, long pos) throws SQLException {
		excep();
		return 0;
	}

	private static void excep() throws SQLException {
		throw new SQLException("不支持此方法");
	}

	@Override
	public void free() throws SQLException {
		excep();
	}

	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		excep();
		return null;
	}
}