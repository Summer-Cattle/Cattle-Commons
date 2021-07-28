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
package io.github.summercattle.commons.db.struct.impl;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.struct.FieldStruct;

public class FieldStructImpl implements FieldStruct {

	private String name;

	private DataType type;

	private int jdbcType;

	private String typeName;

	private long size;

	private int decimalDigits;

	private boolean sqlKeyword;

	public FieldStructImpl(String name, DataType type, int jdbcType, String typeName, long size, int decimalDigits, boolean sqlKeyword) {
		this.name = name;
		this.type = type;
		this.jdbcType = jdbcType;
		this.typeName = typeName;
		this.size = size;
		this.decimalDigits = decimalDigits;
		this.sqlKeyword = sqlKeyword;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public DataType getType() {
		return type;
	}

	@Override
	public int getJdbcType() {
		return jdbcType;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public int getDecimalDigits() {
		return decimalDigits;
	}

	@Override
	public boolean isSqlKeyword() {
		return sqlKeyword;
	}
}