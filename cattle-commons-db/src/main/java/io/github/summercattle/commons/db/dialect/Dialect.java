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
package io.github.summercattle.commons.db.dialect;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.dialect.pagination.LimitHandler;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;

/**
 * 数据库方言
 */
public abstract class Dialect {

	private final TypeNames typeNames = new TypeNames();

	private String schema;

	public Dialect() {
		registerColumnType(DataType.Boolean, "boolean");
		registerColumnType(DataType.String, "varchar", "varchar($l)");
		registerColumnType(DataType.NString, "nvarchar", "nvarchar($l)");
		registerColumnType(DataType.LongString, "varchar", "varchar($l)");
		registerColumnType(DataType.Binary, "bit varying", "bit varying($l)");
		registerColumnType(DataType.LongBinary, "bit varying", "bit varying($l)");
		registerColumnType(DataType.Clob, "clob");
		registerColumnType(DataType.NClob, "nclob");
		registerColumnType(DataType.Blob, "blob");
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Time, "time");
		registerColumnType(DataType.Timestamp, "timestamp");
		registerColumnType(DataType.Number, "decimal", "decimal($p,$s)");
		registerColumnType(DataType.Double, "double precision");
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	public String getTypeName(DataType dataType) throws CommonException {
		String result = typeNames.get(dataType);
		if (result == null) {
			throw new CommonException("没有默认类型映射数据类型:" + dataType.toString());
		}
		return result;
	}

	public String getTypeSimpleName(DataType dataType) throws CommonException {
		String result = typeNames.getSimple(dataType);
		if (result == null) {
			throw new CommonException("没有默认类型映射数据类型:" + dataType.toString());
		}
		return result;
	}

	public String getTypeName(DataType dataType, long length, int scale) throws CommonException {
		String result = typeNames.get(dataType, length, scale);
		if (null == result) {
			throw new CommonException(String.format("没有类型映射数据类型:%s,长度:%s" + dataType.toString(), length));
		}
		return result;
	}

	public String getTypeSimpleName(DataType dataType, long length, int scale) throws CommonException {
		String result = typeNames.getSimple(dataType, length, scale);
		if (null == result) {
			throw new CommonException(String.format("没有类型映射数据类型:%s,长度:%s" + dataType.toString(), length));
		}
		return result;
	}

	public String getCurrentSchemaCommand() {
		return null;
	}

	/**
	 * 是否支持序列
	 * @return 是否支持序列
	 */
	public boolean supportsSequences() {
		return false;
	}

	public boolean supportsPooledSequences() {
		return false;
	}

	/**
	 * 查询下一个序列值语句
	 * @param sequenceName 序列名
	 * @return 查询下一个序列值语句
	 */
	public String getSequenceNextValString(String sequenceName) {
		throw new CommonRuntimeException(getClass().getName() + "不支持序列");
	}

	public String getSelectSequenceNextValString(String sequenceName) {
		throw new CommonRuntimeException(getClass().getName() + "不支持序列");
	}

	/**
	 * 创建序列语句
	 * @param sequenceName 序列名
	 * @return 创建序列语句
	 */
	public String getCreateSequenceCommand(String sequenceName) {
		throw new CommonRuntimeException(getClass().getName() + "不支持序列");
	}

	public String getCreateSequenceCommand(String sequenceName, int initialValue, int incrementSize) throws CommonException {
		if (supportsPooledSequences()) {
			return getCreateSequenceCommand(sequenceName) + " start with " + initialValue + " increment by " + incrementSize;
		}
		throw new CommonException(getClass().getName() + "不支持序列池");
	}

	/**
	 * 查询序列语句
	 * @return 查询序列语句
	 */
	public String getQuerySequencesCommand() {
		throw new CommonRuntimeException(getClass().getName() + "不支持序列");
	}

	public String getSelectGUIDString() {
		throw new CommonRuntimeException(getClass().getName() + "不支持GUID");
	}

	public String appendLock(String tableName) {
		return tableName;
	}

	public String getForUpdateString() {
		return " for update";
	}

	/**
	 * 是否支持当前时间查询语句
	 * @return 是否支持当前时间查询语句
	 */
	public boolean supportsCurrentTimestampSelection() {
		return false;
	}

	/**
	 * 是否支持当前时间调用语句
	 * @return 是否支持当前时间调用语句
	 */
	public boolean isCurrentTimestampSelectStringCallable() {
		throw new CommonRuntimeException("数据库不支持当前时间函数");
	}

	/**
	 * 当前时间查询语句
	 * @return 当前时间查询语句
	 */
	public String getCurrentTimestampSelectString() {
		throw new CommonRuntimeException("数据库不支持当前时间函数");
	}

	public String getCurrentTimestampSQLFunctionName() {
		return "current_timestamp";
	}

	public char openQuote() {
		return '"';
	}

	public char closeQuote() {
		return '"';
	}

	public final String quote(String name) {
		return openQuote() + name + closeQuote();
	}

	public boolean supportsSubqueryOnMutatingTable() {
		return true;
	}

	public String getAddColumnString() {
		throw new CommonRuntimeException(getClass().getName() + "不支持列增加");
	}

	public String getAddColumnSuffixString() {
		return "";
	}

	public String getNullColumnString() {
		return "";
	}

	public String getTableTypeString() {
		return "";
	}

	public String getAddPrimaryKeyString(String constraintName) {
		return " add constraint " + constraintName + " primary key ";
	}

	public String getDropPrimaryKeyString(String constraintName) {
		return " drop constraint " + constraintName;
	}

	public String getDropIndexCommand(String tableName, String indexName) {
		return "drop index " + indexName;
	}

	public boolean supportsCommentOn() {
		return false;
	}

	public String getTableComment(String comment) {
		return "";
	}

	public String getColumnComment(String comment) {
		return "";
	}

	public String getCreateTableString() {
		return "create table";
	}

	public String getAlterTableString(String tableName) {
		final StringBuilder sb = new StringBuilder("alter table ");
		if (supportsIfExistsAfterAlterTable()) {
			sb.append("if exists ");
		}
		sb.append(tableName);
		return sb.toString();
	}

	public boolean supportsIfExistsAfterAlterTable() {
		return false;
	}

	public String getNotExpression(String expression) {
		return "not " + expression;
	}

	public boolean supportsLimit() {
		return false;
	}

	public LimitHandler getLimitHandler() {
		throw new CommonRuntimeException(getClass().getName() + "不支持分页");
	}

	public boolean supportsUnionAll() {
		return false;
	}

	public String getSelectClauseNullString(int sqlType) {
		return "null";
	}

	public int getInExpressionCountLimit() {
		return 0;
	}

	public boolean supportsExistsInSelect() {
		return true;
	}

	public boolean supportsSubselectAsInPredicateLHS() {
		return true;
	}

	protected void registerColumnType(DataType dataType, long capacity, String name) {
		typeNames.put(dataType, capacity, name, name);
	}

	protected void registerColumnType(DataType dataType, long capacity, String simpleName, String name) {
		typeNames.put(dataType, capacity, simpleName, name);
	}

	protected void registerColumnType(DataType dataType, String name) {
		registerColumnType(dataType, name, name);
	}

	protected void registerColumnType(DataType dataType, String simpleName, String name) {
		typeNames.put(dataType, simpleName, name);
	}

	/**
	 * 数据结构查询
	 * @return 数据结构查询
	 */
	public StructHandler getStructHandler() {
		throw new CommonRuntimeException(getClass().getName() + "不支持数据结构查询");
	}

	/**
	 * 数据库模式名
	 * @return 数据库模式名
	 */
	public String getSchema() {
		return schema;
	}

	public String getModifyColumnString() {
		throw new CommonRuntimeException(getClass().getName() + "不支持列修改");
	}

	public String getModifyColumnDataTypeCommand(String tableName, String fieldName, DataType dataType, int length, int scale, boolean allowNull,
			String defaultValue, String comment) throws CommonException {
		throw new CommonException(getClass().getName() + "不支持列类型修改");
	}

	public String getModifyColumnNullCommand(String tableName, String fieldName, DataType dataType, int length, int scale, boolean allowNull,
			String defaultValue, String comment) throws CommonException {
		throw new CommonException(getClass().getName() + "不支持列空值修改");
	}

	public String getModifyColumnDefaultCommand(String tableName, String fieldName, DataType dataType, String defaultValue) {
		throw new CommonRuntimeException(getClass().getName() + "不支持列缺省值修改");
	}
}