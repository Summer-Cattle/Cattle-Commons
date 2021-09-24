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

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.constants.DataType;
import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.dialect.StructHandler;
import com.gitlab.summercattle.commons.db.dialect.pagination.AbstractLimitHandler;
import com.gitlab.summercattle.commons.db.dialect.pagination.LimitHandler;
import com.gitlab.summercattle.commons.db.dialect.struct.OracleStructHandler;
import com.gitlab.summercattle.commons.exception.CommonException;

public class Oracle8iDialect extends Dialect {

	private static final int PARAM_LIST_SIZE_LIMIT = 1000;

	private static final LimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {

		private static final String ROW_NUMBER_ALIAS = "rownum_";

		@Override
		public String processSql(String sql, int startRow) {
			boolean hasOffset = startRow > 0;
			sql = sql.trim();
			boolean isForUpdate = false;
			if (sql.toLowerCase().endsWith(" for update")) {
				sql = sql.substring(0, sql.length() - 11);
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
				pagingSelect.append(" ) row_ ) where " + ROW_NUMBER_ALIAS + " <= ? and " + ROW_NUMBER_ALIAS + " > ?");
			}
			else {
				pagingSelect.append(" ) where rownum <= ?");
			}
			if (isForUpdate) {
				pagingSelect.append(" for update");
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

	public Oracle8iDialect() {
		super();
		registerCharacterTypeMappings();
		registerNumericTypeMappings();
		registerDateTimeTypeMappings();
		registerLargeObjectTypeMappings();
	}

	protected void registerCharacterTypeMappings() {
		registerColumnType(DataType.NString, "nvarchar2", "nvarchar2($l)");
		registerColumnType(DataType.String, 4000, "varchar2", "varchar2($l)");
		registerColumnType(DataType.String, "long");
	}

	protected void registerNumericTypeMappings() {
		registerColumnType(DataType.Double, "double precision");
		registerColumnType(DataType.Number, "number", "number($l,$s)");
		registerColumnType(DataType.Boolean, "number", "number(1,0)");
	}

	protected void registerDateTimeTypeMappings() {
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Time, "date");
		registerColumnType(DataType.Timestamp, "date");
	}

	protected void registerLargeObjectTypeMappings() {
		registerColumnType(DataType.Binary, 2000, "raw", "raw($l)");
		registerColumnType(DataType.Binary, "long raw");
		registerColumnType(DataType.Blob, "blob");
		registerColumnType(DataType.Clob, "clob");
		registerColumnType(DataType.NClob, "nclob");
		registerColumnType(DataType.LongString, "long");
		registerColumnType(DataType.LongBinary, "long raw");
	}

	@Override
	public LimitHandler getLimitHandler() {
		return LIMIT_HANDLER;
	}

	public String getBasicSelectClauseNullString(int sqlType) {
		return super.getSelectClauseNullString(sqlType);
	}

	@Override
	public String getSelectClauseNullString(int sqlType) {
		switch (sqlType) {
			case Types.VARCHAR:
			case Types.CHAR:
				return "to_char(null)";
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIME:
				return "to_date(null)";
			default:
				return "to_number(null)";
		}
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return "select sysdate from dual";
	}

	@Override
	public String getCurrentTimestampSQLFunctionName() {
		return "sysdate";
	}

	@Override
	public String getAddColumnString() {
		return "add";
	}

	@Override
	public String getSequenceNextValString(String sequenceName) {
		return "select " + getSelectSequenceNextValString(sequenceName) + " from dual";
	}

	@Override
	public String getSelectSequenceNextValString(String sequenceName) {
		return sequenceName + ".nextval";
	}

	@Override
	public String getCreateSequenceCommand(String sequenceName) {
		//starts with 1, implicitly
		return "create sequence " + sequenceName;
	}

	@Override
	public String getCreateSequenceCommand(String sequenceName, int initialValue, int incrementSize) {
		if (initialValue < 0 && incrementSize > 0) {
			return String.format("%s minvalue %d start with %d increment by %d", getCreateSequenceCommand(sequenceName), initialValue, initialValue,
					incrementSize);
		}
		else if (initialValue > 0 && incrementSize < 0) {
			return String.format("%s maxvalue %d start with %d increment by %d", getCreateSequenceCommand(sequenceName), initialValue, initialValue,
					incrementSize);
		}
		else {
			return String.format("%s start with %d increment by  %d", getCreateSequenceCommand(sequenceName), initialValue, incrementSize);
		}
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public boolean supportsPooledSequences() {
		return true;
	}

	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public String getQuerySequencesCommand() {
		return "select * from all_sequences";
	}

	@Override
	public String getSelectGUIDString() {
		return "select rawtohex(sys_guid()) from dual";
	}

	@Override
	public boolean supportsUnionAll() {
		return true;
	}

	@Override
	public boolean supportsCommentOn() {
		return true;
	}

	@Override
	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	@Override
	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	@Override
	public boolean supportsExistsInSelect() {
		return false;
	}

	@Override
	public int getInExpressionCountLimit() {
		return PARAM_LIST_SIZE_LIMIT;
	}

	@Override
	public String getNotExpression(String expression) {
		return "not (" + expression + ")";
	}

	@Override
	public String getCurrentSchemaCommand() {
		return "select sys_context('USERENV', 'CURRENT_SCHEMA') from dual";
	}

	@Override
	public StructHandler getStructHandler() {
		return OracleStructHandler.INSTANCE;
	}

	@Override
	public char openQuote() {
		return '"';
	}

	@Override
	public char closeQuote() {
		return '"';
	}

	@Override
	public String getModifyColumnString() {
		return "modify";
	}

	@Override
	public String getModifyColumnDataTypeCommand(String tableName, String fieldName, DataType dataType, int length, int scale, boolean allowNull,
			String defaultValue, String comment) throws CommonException {
		String sql = getAlterTableString(tableName) + " " + getModifyColumnString() + " ";
		sql += quote(fieldName) + " " + getTypeName(dataType, length, scale);
		return sql;
	}

	@Override
	public String getModifyColumnNullCommand(String tableName, String fieldName, DataType dataType, int length, int scale, boolean allowNull,
			String defaultValue, String comment) throws CommonException {
		String sql = getAlterTableString(tableName) + " " + getModifyColumnString() + " ";
		sql += quote(fieldName) + " " + getTypeName(dataType, length, scale);
		if (!allowNull) {
			sql += " not null";
		}
		else {
			sql += " null";
		}
		return sql;
	}

	@Override
	public String getModifyColumnDefaultCommand(String tableName, String fieldName, DataType dataType, String defaultValue) {
		String sql;
		if (StringUtils.isNotBlank(defaultValue)) {
			sql = getAlterTableString(tableName) + " " + getModifyColumnString() + " " + quote(fieldName);
			sql += " default ";
			if (dataType == DataType.String || dataType == DataType.NString || dataType == DataType.LongString || dataType == DataType.Clob
					|| dataType == DataType.NClob) {
				sql += "'";
			}
			sql += defaultValue;
			if (dataType == DataType.String || dataType == DataType.NString || dataType == DataType.LongString || dataType == DataType.Clob
					|| dataType == DataType.NClob) {
				sql += "'";
			}
		}
		else {
			sql = getAlterTableString(tableName) + " " + getModifyColumnString() + " " + quote(fieldName);
			sql += " default default null";
		}
		return sql;
	}
}