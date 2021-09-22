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

import io.github.summercattle.commons.db.struct.TableFieldStruct;

public class TableFieldStructImpl extends FieldStructImpl implements TableFieldStruct {

	private boolean nullable;

	private String defaultValue;

	private String comment;

	public TableFieldStructImpl(String name, int jdbcType, String typeName, boolean nullable, long size, int decimalDigits, String defaultValue,
			String comment) {
		super(name, jdbcType, typeName, size, decimalDigits);
		this.nullable = nullable;
		this.defaultValue = defaultValue;
		this.comment = comment;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getComment() {
		return comment;
	}
}