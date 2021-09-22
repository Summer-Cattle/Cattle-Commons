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

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.dialect.pagination.AbstractLimitHandler;
import io.github.summercattle.commons.db.dialect.pagination.LimitHandler;

public class Oracle9iDialect extends Oracle8iDialect {

	private static final LimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {

		private static final String ROW_NUMBER_ALIAS = "rownum_";

		@Override
		public String processSql(String sql, int startRow) {
			boolean hasOffset = startRow > 0;
			sql = sql.trim();
			String forUpdateClause = null;
			boolean isForUpdate = false;
			final int forUpdateIndex = sql.toLowerCase().lastIndexOf("for update");
			if (forUpdateIndex > -1) {
				// save 'for update ...' and then remove it
				forUpdateClause = sql.substring(forUpdateIndex);
				sql = sql.substring(0, forUpdateIndex - 1);
				isForUpdate = true;
			}
			StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
			if (hasOffset) {
				pagingSelect.append("select * from ( select row_.*, rownum " + ROW_NUMBER_ALIAS + " from ( ");
			}
			else {
				pagingSelect.append("select * from ( ");
			}
			pagingSelect.append(sql);
			if (hasOffset) {
				pagingSelect.append(" ) row_ where rownum <= ?) where " + ROW_NUMBER_ALIAS + " > ?");
			}
			else {
				pagingSelect.append(" ) where rownum <= ?");
			}
			if (isForUpdate) {
				pagingSelect.append(" ");
				pagingSelect.append(forUpdateClause);
			}
			return pagingSelect.toString();
		}

		@Override
		public boolean useMaxForLimit() {
			return true;
		}

		@Override
		public boolean isFilterPageFields() {
			return true;
		}

		@Override
		public String[] getFilterPageFields() {
			return new String[] { ROW_NUMBER_ALIAS };
		}
	};

	@Override
	protected void registerCharacterTypeMappings() {
		registerColumnType(DataType.String, 4000, "varchar2", "varchar2($l char)");
		registerColumnType(DataType.String, "long");
		registerColumnType(DataType.NString, "nvarchar2", "nvarchar2($l)");
	}

	@Override
	protected void registerDateTimeTypeMappings() {
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Time, "date");
		registerColumnType(DataType.Timestamp, "timestamp");
	}

	@Override
	public LimitHandler getLimitHandler() {
		return LIMIT_HANDLER;
	}

	@Override
	public String getSelectClauseNullString(int sqlType) {
		return getBasicSelectClauseNullString(sqlType);
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return "select systimestamp from dual";
	}

	@Override
	public String getCurrentTimestampSQLFunctionName() {
		// the standard SQL function name is current_timestamp...
		return "current_timestamp";
	}
}