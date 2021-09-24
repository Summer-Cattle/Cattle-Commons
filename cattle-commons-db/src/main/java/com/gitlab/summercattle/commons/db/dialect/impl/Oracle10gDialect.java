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

public class Oracle10gDialect extends Oracle9iDialect {

	public Oracle10gDialect() {
		super();
	}

	@Override
	protected void registerNumericTypeMappings() {
		registerColumnType(DataType.Double, "binary_double");
		registerColumnType(DataType.Number, "number", "number($l,$s)");
		registerColumnType(DataType.Boolean, "number", "number(1,0)");
	}
}