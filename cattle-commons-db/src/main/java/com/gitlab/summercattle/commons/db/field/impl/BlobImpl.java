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
package com.gitlab.summercattle.commons.db.field.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class BlobImpl implements Blob {

	private InputStream stream;

	private int length;

	private boolean needsReset = false;

	public BlobImpl(byte[] bytes) {
		this.stream = new ByteArrayInputStream(bytes);
		this.length = bytes.length;
	}

	public BlobImpl(InputStream stream, int length) {
		this.stream = stream;
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
	public byte[] getBytes(long pos, int len) throws SQLException {
		excep();
		return null;
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		excep();
		return 0;
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int i, int j) throws SQLException {
		excep();
		return 0;
	}

	@Override
	public long position(byte[] bytes, long pos) throws SQLException {
		excep();
		return 0;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		try {
			if (needsReset) {
				stream.reset();
			}
		}
		catch (IOException ioe) {
			throw new SQLException("could not reset reader");
		}
		needsReset = true;
		return stream;
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		excep();
		return null;
	}

	@Override
	public long position(Blob blob, long pos) throws SQLException {
		excep();
		return 0;
	}

	private void excep() throws SQLException {
		throw new SQLException("??????????????????");
	}

	@Override
	public void free() throws SQLException {
		excep();
	}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		excep();
		return null;
	}
}