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
package io.github.summercattle.commons.db.dialect.impl;

import java.sql.Connection;

import io.github.summercattle.commons.exception.CommonException;

public class Oracle12cDialect extends Oracle9iDialect {

	public Oracle12cDialect(Connection conn, String sqlKeywords) {
		super(conn, sqlKeywords);
	}

	@Override
	public String getPageLimitString(String sql, int startRowNum, int perPageSize) throws CommonException {
		return sql + (startRowNum > 0 ? " OFFSET " + String.valueOf(startRowNum) + " ROWS FETCH NEXT " + String.valueOf(perPageSize) + " ROWS ONLY"
				: " FETCH FIRST " + String.valueOf(perPageSize) + " ROWS ONLY");
	}
}