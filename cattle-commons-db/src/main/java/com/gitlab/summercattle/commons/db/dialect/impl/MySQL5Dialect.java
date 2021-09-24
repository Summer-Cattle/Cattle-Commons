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
package com.gitlab.summercattle.commons.db.dialect.impl;

import com.gitlab.summercattle.commons.db.constants.DataType;

public class MySQL5Dialect extends MySQLDialect {

	protected String getEngineKeyword() {
		return "engine";
	}

	@Override
	public boolean supportsUnionAll() {
		return true;
	}

	@Override
	protected void registerVarcharTypes() {
		registerColumnType(DataType.NString, "varchar", "varchar($l) character set utf8");
		registerColumnType(DataType.String, "longtext");
		registerColumnType(DataType.String, 65535, "varchar", "varchar($l)");
		registerColumnType(DataType.LongString, "longtext");
	}
}