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

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.dialect.StructHandler;
import io.github.summercattle.commons.db.dialect.pagination.AbstractLimitHandler;
import io.github.summercattle.commons.db.dialect.pagination.LimitHandler;
import io.github.summercattle.commons.db.dialect.struct.MySQLStructHandler;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.spring.SpringContext;

public class MySQLDialect extends Dialect {

	private final MySQLStorageEngine storageEngine;

	private final StructHandler structHandler;

	private static final LimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {

		@Override
		public String processSql(String sql, int startRow) {
			boolean hasOffset = startRow > 0;
			return sql + (hasOffset ? " limit ?, ?" : " limit ?");
		}
	};

	public MySQLDialect() {
		super();
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		String storageEngine = dbProperties.getStorageEngine();
		if (StringUtils.isBlank(storageEngine)) {
			this.storageEngine = getDefaultMySQLStorageEngine();
		}
		else if ("innodb".equalsIgnoreCase(storageEngine)) {
			this.storageEngine = InnoDBStorageEngine.INSTANCE;
		}
		else if ("myisam".equalsIgnoreCase(storageEngine)) {
			this.storageEngine = MyISAMStorageEngine.INSTANCE;
		}
		else {
			throw new UnsupportedOperationException("The storage engine '" + storageEngine + "' is not supported!");
		}
		registerColumnType(DataType.Boolean, "bit");
		registerColumnType(DataType.Binary, "longblob");
		registerColumnType(DataType.Binary, 16777215, "mediumblob");
		registerColumnType(DataType.Binary, 65535, "blob");
		registerColumnType(DataType.Binary, 255, "tinyblob");
		registerColumnType(DataType.LongBinary, "longblob");
		registerColumnType(DataType.LongBinary, 16777215, "mediumblob");
		registerColumnType(DataType.Clob, "longtext");
		registerColumnType(DataType.NClob, "longtext", "longtext character set utf8");
		registerColumnType(DataType.Blob, "longblob");
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Time, "time");
		registerColumnType(DataType.Timestamp, "datetime");
		registerColumnType(DataType.Number, "decimal", "decimal($l,$s)");
		registerColumnType(DataType.Double, "double", "double precision");
		registerVarcharTypes();
		structHandler = new MySQLStructHandler(this);
	}

	protected void registerVarcharTypes() {
		registerColumnType(DataType.NString, "varchar", "varchar($l) character set utf8");
		registerColumnType(DataType.String, "longtext");
		registerColumnType(DataType.String, 255, "varchar", "varchar($l)");
		registerColumnType(DataType.LongString, "longtext");
	}

	@Override
	public String getAddColumnString() {
		return "add column";
	}

	@Override
	public boolean supportsLimit() {
		return true;
	}

	@Override
	public LimitHandler getLimitHandler() {
		return LIMIT_HANDLER;
	}

	@Override
	public char openQuote() {
		return '`';
	}

	@Override
	public char closeQuote() {
		return '`';
	}

	@Override
	public String getSelectGUIDString() {
		return "select uuid()";
	}

	@Override
	public String getTableComment(String comment) {
		return " comment='" + comment + "'";
	}

	@Override
	public String getColumnComment(String comment) {
		return " comment '" + comment + "'";
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
	public String getCurrentTimestampSelectString() {
		return "select now()";
	}

	@Override
	public boolean supportsSubqueryOnMutatingTable() {
		return false;
	}

	@Override
	public String getNotExpression(String expression) {
		return "not (" + expression + ")";
	}

	@Override
	public String getTableTypeString() {
		return storageEngine.getTableTypeString(getEngineKeyword());
	}

	protected String getEngineKeyword() {
		return "type";
	}

	protected MySQLStorageEngine getDefaultMySQLStorageEngine() {
		return MyISAMStorageEngine.INSTANCE;
	}

	@Override
	public String getCurrentSchemaCommand() {
		return "select schema()";
	}

	@Override
	public StructHandler getStructHandler() {
		return structHandler;
	}

	@Override
	public String getAddPrimaryKeyString(String constraintName) {
		return " add primary key ";
	}

	@Override
	public String getDropPrimaryKeyString(String constraintName) {
		return " drop primary key";
	}

	@Override
	public String getDropIndexCommand(String tableName, String indexName) {
		return "drop index " + indexName + " on " + tableName;
	}

	@Override
	public String getModifyColumnString() {
		return "modify column";
	}

	@Override
	public String getModifyColumnDataTypeCommand(String tableName, String fieldName, DataType dataType, int length, int scale, boolean allowNull,
			String defaultValue, String comment) throws CommonException {
		String sql = getAlterTableString(tableName) + " " + getModifyColumnString() + " ";
		sql += quote(fieldName) + " " + getTypeName(dataType, length, scale);
		if (!allowNull) {
			sql += " not null";
		}
		else {
			sql += " null";
		}
		if (StringUtils.isNotBlank(defaultValue)) {
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
		sql += getColumnComment(comment);
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
		if (StringUtils.isNotBlank(defaultValue)) {
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
		sql += getColumnComment(comment);
		return sql;
	}

	@Override
	public String getModifyColumnDefaultCommand(String tableName, String fieldName, DataType dataType, String defaultValue) {
		String sql;
		if (StringUtils.isNotBlank(defaultValue)) {
			sql = getAlterTableString(tableName) + " alter column " + quote(fieldName) + " set default ";
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
			sql = getAlterTableString(tableName) + " alter column " + quote(fieldName) + " drop default";
		}
		return sql;
	}
}