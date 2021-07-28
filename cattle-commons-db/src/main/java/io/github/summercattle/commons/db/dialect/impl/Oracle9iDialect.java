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
import java.util.Locale;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.exception.CommonException;

public class Oracle9iDialect extends Oracle8iDialect {

	public Oracle9iDialect(Connection conn, String sqlKeywords) {
		super(conn, sqlKeywords);
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return "select systimestamp from dual";
	}

	@Override
	public String getCurrentTimestampSQLFunctionName() {
		return "current_timestamp";
	}

	@Override
	public String getPageLimitString(String sql, int startRowNum, int perPageSize) throws CommonException {
		String forUpdateClause = null;
		boolean isForUpdate = false;
		final int forUpdateIndex = sql.toLowerCase(Locale.ROOT).lastIndexOf("for update");
		if (forUpdateIndex > -1) {
			// save 'for update ...' and then remove it
			forUpdateClause = sql.substring(forUpdateIndex);
			sql = sql.substring(0, forUpdateIndex - 1);
			isForUpdate = true;
		}
		StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
		if (startRowNum > 0) {
			pagingSelect.append("select * from (select row_.*, rownum " + ROW_NUMBER_FIELD + " from (");
		}
		else {
			pagingSelect.append("select * from (");
		}
		pagingSelect.append(sql);
		if (startRowNum > 0) {
			pagingSelect.append(") row_ where rownum<=" + String.valueOf(startRowNum + perPageSize) + ") where " + ROW_NUMBER_FIELD + ">"
					+ String.valueOf(startRowNum));
		}
		else {
			pagingSelect.append(") where rownum<=" + String.valueOf(perPageSize));
		}
		if (isForUpdate) {
			pagingSelect.append(" ");
			pagingSelect.append(forUpdateClause);
		}
		return pagingSelect.toString();
	}

	@Override
	protected void registerDateTimeTypeMappings() {
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Time, "date");
		registerColumnType(DataType.Timestamp, "timestamp");
	}

	@Override
	public String getQuerySequencesString() {
		return "select sequence_name from user_sequences";
	}
}