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

import io.github.summercattle.commons.db.struct.FieldStruct;

public class FieldStructImpl implements FieldStruct {

	private String name;

	private int jdbcType;

	private String typeName;

	private long length;

	private int scale;

	public FieldStructImpl(String name, int jdbcType, String typeName, long length, int scale) {
		this.name = name;
		this.jdbcType = jdbcType;
		this.typeName = typeName;
		this.length = length;
		this.scale = scale;
	}

	@Override
	public String getName() {
		return name;
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
	public long getLength() {
		return length;
	}

	@Override
	public int getScale() {
		return scale;
	}
}