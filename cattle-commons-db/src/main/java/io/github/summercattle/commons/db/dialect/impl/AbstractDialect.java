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
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.constants.DatabaseType;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;

public abstract class AbstractDialect implements Dialect {

	protected final static String ROW_NUMBER_FIELD = "ROW_NUM_FIELD";

	protected String[] sqlKeywordsArray;

	private final TypeNames typeNames = new TypeNames();

	protected String schema;

	public AbstractDialect(String sqlKeywords) {
		if (StringUtils.isNotBlank(sqlKeywords)) {
			sqlKeywordsArray = sqlKeywords.split("\\s*,\\s*");
		}
	}

	@Override
	public String getSchema() {
		return schema;
	}

	@Override
	public String getValidateQuery() {
		return null;
	}

	@Override
	public String getTableTypeString() {
		return null;
	}

	@Override
	public boolean isSQLKeyword(String str) {
		boolean result = false;
		if (sqlKeywordsArray != null) {
			for (int i = 0; i < sqlKeywordsArray.length; i++) {
				if (sqlKeywordsArray[i].equalsIgnoreCase(str)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public String getSQLKeywordMarks() {
		return "'";
	}

	@Override
	public String getSQLKeyword(String str) {
		return getSQLKeyword(str, isSQLKeyword(str));
	}

	@Override
	public String getSQLKeyword(String str, boolean sqlKeyword) {
		if (sqlKeyword) {
			return getSQLKeywordMarks() + str + getSQLKeywordMarks();
		}
		else {
			return str;
		}
	}

	@Override
	public boolean supportsCurrentTimestampSelection() {
		return false;
	}

	@Override
	public String getCurrentTimestampCallString() throws CommonException {
		return null;
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return null;
	}

	@Override
	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	@Override
	public boolean supportsPageLimitOffset() {
		return false;
	}

	@Override
	public String getPageLimitString(String esql, int startRowNum, int perPageSize) throws CommonException {
		return null;
	}

	@Override
	public boolean isFilterPageFields() {
		DatabaseType type = getType();
		return type == DatabaseType.Oracle || type == DatabaseType.DB2;
	}

	@Override
	public String[] getFilterPageFields() {
		String[] filterFields = null;
		DatabaseType type = getType();
		if (type == DatabaseType.Oracle || type == DatabaseType.DB2) {
			filterFields = new String[] { ROW_NUMBER_FIELD };
		}
		return filterFields;
	}

	@Override
	public boolean supportsSequences() {
		return false;
	}

	@Override
	public String getCreateSequenceString(String sequenceName) {
		return null;
	}

	@Override
	public String getQuerySequencesString() {
		return null;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) {
		return null;
	}

	@Override
	public String getAddColumnString() {
		throw new CommonRuntimeException("不支持的添加列语法,类:" + getClass().getName());
	}

	@Override
	public String getModifyColumnString() {
		throw new CommonRuntimeException("不支持的修改列语法,类:" + getClass().getName());
	}

	@Override
	public String getAddPrimaryKeyConstraintString(String constraintName) {
		return "add constraint " + constraintName + " primary key";
	}

	@Override
	public String getDropPrimaryKeyConstraintString(String constraintName) {
		return "drop constraint " + constraintName;
	}

	@Override
	public String getDropIndexString(String tableName, String indexName) {
		return "drop index " + indexName;
	}

	@Override
	public String getTypeName(DataType dataType, long length, int precision) throws CommonException {
		String result = typeNames.get(dataType, length, precision);
		if (null == result) {
			throw new CommonException(String.format("没有类型映射数据类型:%s,长度:%s" + dataType.toString(), length));
		}
		return result;
	}

	@Override
	public String getTypeName(DataType dataType) throws CommonException {
		String result = typeNames.get(dataType);
		if (result == null) {
			throw new CommonException("没有默认类型映射数据类型:" + dataType.toString());
		}
		return result;
	}

	@Override
	public void registerColumnType(DataType dataType, long capacity, String name) {
		typeNames.put(dataType, capacity, name);
	}

	@Override
	public void registerColumnType(DataType dataType, String name) {
		typeNames.put(dataType, name);
	}

	@Override
	public String getForUpdateString() {
		return "";
	}

	@Override
	public String appendLock(String tableName) {
		return tableName;
	}

	@Override
	public TableObjectStruct getTableStruct(Connection conn, String tableName) throws CommonException {
		throw new CommonException("不支持的获得数据表结构方法,类:" + getClass().getName());
	}

	@Override
	public boolean existTable(Connection conn, String tableName) throws CommonException {
		throw new CommonException("不支持的是否存在指定表方法,类:" + getClass().getName());
	}

	@Override
	public ViewObjectStruct getViewStruct(Connection conn, String viewName) throws CommonException {
		throw new CommonException("不支持的获得视图结构方法,类:" + getClass().getName());
	}

	@Override
	public boolean existView(Connection conn, String viewName) throws CommonException {
		throw new CommonException("不支持的是否存在指定视图方法,类:" + getClass().getName());
	}

	protected int getJdbcDataType(String typeName) {
		if (typeName.equalsIgnoreCase("DECIMAL")) {
			return Types.DECIMAL;
		}
		else if (typeName.equalsIgnoreCase("TIMESTAMP") || typeName.equalsIgnoreCase("DATETIME")) {
			return Types.TIMESTAMP;
		}
		else if (typeName.equalsIgnoreCase("TIME")) {
			return Types.TIME;
		}
		else if (typeName.equalsIgnoreCase("DATE")) {
			return Types.DATE;
		}
		else if (typeName.equalsIgnoreCase("VARCHAR") || typeName.equalsIgnoreCase("VARCHAR2")) {
			return Types.VARCHAR;
		}
		else if (typeName.equalsIgnoreCase("NVARCHAR2")) {
			return Types.NVARCHAR;
		}
		else if (typeName.equalsIgnoreCase("LONGTEXT") || typeName.equalsIgnoreCase("TEXT")) {
			return Types.LONGVARCHAR;
		}
		else if (typeName.equalsIgnoreCase("LONGBLOB")) {
			return Types.LONGVARBINARY;
		}
		else if (typeName.equalsIgnoreCase("NUMBER")) {
			return Types.NUMERIC;
		}
		else if (typeName.equalsIgnoreCase("CLOB")) {
			return Types.CLOB;
		}
		else if (typeName.equalsIgnoreCase("NCLOB")) {
			return Types.NCLOB;
		}
		else if (typeName.equalsIgnoreCase("BLOB")) {
			return Types.BLOB;
		}
		else if (typeName.equalsIgnoreCase("INT") || typeName.equalsIgnoreCase("INTEGER")) {
			return Types.INTEGER;
		}
		return Types.OTHER;
	}

	@Override
	public boolean supportsUnicodeStringType() {
		return true;
	}

	@Override
	public String getCurrentTimestampSQLFunctionName() {
		return "current_timestamp";
	}
}