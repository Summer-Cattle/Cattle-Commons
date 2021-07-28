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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.constants.DatabaseType;
import io.github.summercattle.commons.db.struct.FieldStruct;
import io.github.summercattle.commons.db.struct.TableIndexStruct;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.struct.TablePrimaryKeyStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.db.struct.impl.FieldStructImpl;
import io.github.summercattle.commons.db.struct.impl.TableFieldStructImpl;
import io.github.summercattle.commons.db.struct.impl.TableIndexStructImpl;
import io.github.summercattle.commons.db.struct.impl.TableObjectStructImpl;
import io.github.summercattle.commons.db.struct.impl.TablePrimaryKeyStructImpl;
import io.github.summercattle.commons.db.struct.impl.ViewObjectStructImpl;
import io.github.summercattle.commons.db.utils.JdbcUtils;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.ArrayUtils;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class MySQLDialect extends AbstractDialect {

	private String tableTypeString;

	private String character;

	public MySQLDialect(Connection conn, String sqlKeywords) {
		super(sqlKeywords);
		PreparedStatement databasePs = null;
		ResultSet databaseRs = null;
		try {
			String strSQL = "select database()";
			databasePs = conn.prepareStatement(strSQL);
			databaseRs = JdbcUtils.executeQuery(databasePs, "执行SQL语句:" + strSQL);
			if (databaseRs.next()) {
				schema = databaseRs.getString(1);
			}
		}
		catch (CommonException | SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			JdbcUtils.closeResultSet(databaseRs);
			JdbcUtils.closeStatement(databasePs);
		}
		PreparedStatement enginePs = null;
		ResultSet engineRs = null;
		try {
			String strSQL = "select ENGINE from INFORMATION_SCHEMA.ENGINES where (SUPPORT=? or SUPPORT=?) and TRANSACTIONS=?";
			enginePs = conn.prepareStatement(strSQL);
			enginePs.setString(1, "DEFAULT");
			enginePs.setString(2, "YES");
			enginePs.setString(3, "YES");
			engineRs = JdbcUtils.executeQuery(enginePs,
					"执行SQL语句:" + strSQL + ",参数值:" + ArrayUtils.toString(new Object[] { "DEFAULT", "YES", "YES" }));
			String type = null;
			while (engineRs.next()) {
				String engine = engineRs.getString(1);
				if (engine.equals("InnoDB")) {
					type = engine;
				}
				else if (engine.equals("ndbcluster")) {
					type = engine;
					break;
				}
			}
			if (StringUtils.isBlank(type)) {
				throw new CommonException("没有可支持的MYSQL引擎");
			}
			tableTypeString = "ENGINE=" + type;
		}
		catch (CommonException | SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			JdbcUtils.closeResultSet(engineRs);
			JdbcUtils.closeStatement(enginePs);
		}
		PreparedStatement characterPs = null;
		ResultSet characterRs = null;
		try {
			String strSQL = "SHOW VARIABLES LIKE 'character_set_database'";
			characterPs = conn.prepareStatement(strSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			characterRs = JdbcUtils.executeQuery(characterPs, "执行SQL语句:" + strSQL);
			if (characterRs.first()) {
				character = characterRs.getString("value");
			}
		}
		catch (CommonException | SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			JdbcUtils.closeResultSet(characterRs);
			JdbcUtils.closeStatement(characterPs);
		}
		registerColumnType(DataType.String, 65532, "varchar($l) character set " + character);
		registerColumnType(DataType.NString, 65532, "varchar($l) character set utf8");
		registerColumnType(DataType.String, "longtext character set " + character);
		registerColumnType(DataType.NString, "longtext character set utf8");
		registerColumnType(DataType.Number, "decimal($p,$s)");
		registerColumnType(DataType.Blob, "longblob");
		registerColumnType(DataType.Clob, "longtext character set " + character);
		registerColumnType(DataType.NClob, "longtext character set utf8");
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Timestamp, "datetime");
		registerColumnType(DataType.Time, "time");
		registerColumnType(DataType.Boolean, "decimal(1,0)");
	}

	public DatabaseType getType() {
		return DatabaseType.MySQL;
	}

	@Override
	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	@Override
	public String getCurrentTimestampSelectString() {
		return "select now()";
	}

	@Override
	public boolean supportsPageLimitOffset() {
		return true;
	}

	@Override
	public String getPageLimitString(String sql, int startRowNum, int perPageSize) throws CommonException {
		return new StringBuffer(sql.length() + 20).append(sql)
				.append(startRowNum > 0 ? " limit " + String.valueOf(startRowNum) + "," + String.valueOf(perPageSize)
						: " limit " + String.valueOf(perPageSize))
				.toString();
	}

	@Override
	public String getTableTypeString() {
		return tableTypeString;
	}

	@Override
	public String getValidateQuery() {
		return "select 1";
	}

	@Override
	public String getForUpdateString() {
		return " for update";
	}

	@Override
	public String getAddPrimaryKeyConstraintString(String constraintName) {
		return "add primary key";
	}

	@Override
	public String getDropPrimaryKeyConstraintString(String constraintName) {
		return "drop primary key";
	}

	@Override
	public String getDropIndexString(String tableName, String indexName) {
		return "drop index " + indexName + " on " + tableName;
	}

	@Override
	public boolean existTable(Connection conn, String tableName) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String tableType = "BASE TABLE";
			String sql = "select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA=? and TABLE_TYPE=? and TABLE_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, tableType);
			ps.setString(3, tableName.toLowerCase());
			rs = JdbcUtils.executeQuery(ps,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, tableType, tableName.toLowerCase() }));
			rs.next();
			return rs.getInt(1) > 0;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
	}

	@Override
	public boolean existView(Connection conn, String viewName) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select count(*) from INFORMATION_SCHEMA.VIEWS where TABLE_SCHEMA=? and TABLE_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, schema);
			ps.setString(2, viewName.toLowerCase());
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, viewName.toLowerCase() }));
			rs.next();
			return rs.getInt(1) > 0;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
	}

	@Override
	public TableObjectStruct getTableStruct(Connection conn, String tableName) throws CommonException {
		//表
		String tableType = "BASE TABLE";
		String tableComment;
		PreparedStatement tablePs = null;
		ResultSet tableRs = null;
		try {
			String sql = "select TABLE_COMMENT from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA=? and TABLE_TYPE=? and TABLE_NAME=?";
			tablePs = conn.prepareStatement(sql);
			tablePs.setString(1, schema);
			tablePs.setString(2, tableType);
			tablePs.setString(3, tableName.toLowerCase());
			tableRs = JdbcUtils.executeQuery(tablePs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, tableType, tableName.toLowerCase() }));
			if (!tableRs.next()) {
				throw new CommonException("表'" + tableName + "'不存在");
			}
			tableComment = tableRs.getString("TABLE_COMMENT");
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(tableRs);
			JdbcUtils.closeStatement(tablePs);
		}
		//字段
		ConcurrentMap<String, FieldStruct> fields = new ConcurrentHashMap<String, FieldStruct>();
		PreparedStatement columnPs = null;
		ResultSet columnRs = null;
		try {
			String sql = "select COLUMN_NAME,DATA_TYPE,IS_NULLABLE,COLUMN_DEFAULT,CHARACTER_MAXIMUM_LENGTH,CHARACTER_SET_NAME,NUMERIC_PRECISION,NUMERIC_SCALE,COLUMN_COMMENT from "
					+ "INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA=? and TABLE_NAME=? order by ORDINAL_POSITION";
			columnPs = conn.prepareStatement(sql);
			columnPs.setString(1, schema);
			columnPs.setString(2, tableName.toLowerCase());
			columnRs = JdbcUtils.executeQuery(columnPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, tableName.toLowerCase() }));
			while (columnRs.next()) {
				String columnName = columnRs.getString("COLUMN_NAME");
				String typeName = columnRs.getString("DATA_TYPE");
				boolean isNullable = columnRs.getString("IS_NULLABLE").equalsIgnoreCase("YES");
				String columnDefault = columnRs.getString("COLUMN_DEFAULT");
				int jdbcDataType = getJdbcDataType(typeName);
				long columnSize = 0;
				int decimalDigits = 0;
				if (jdbcDataType == Types.VARCHAR) {
					columnSize = columnRs.getLong("CHARACTER_MAXIMUM_LENGTH");
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if (supportsUnicodeStringType() && character.equals("utf8")) {
						jdbcDataType = Types.NVARCHAR;
					}
				}
				else if (jdbcDataType == Types.LONGVARCHAR) {
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if (supportsUnicodeStringType() && character.equals("utf8")) {
						jdbcDataType = Types.LONGNVARCHAR;
					}
				}
				else if (jdbcDataType == Types.DECIMAL) {
					columnSize = columnRs.getLong("NUMERIC_PRECISION");
					decimalDigits = columnRs.getInt("NUMERIC_SCALE");
				}
				fields.put(columnName.toUpperCase(), new TableFieldStructImpl(columnName, JdbcUtils.getDataType(jdbcDataType), jdbcDataType, typeName,
						isNullable, columnSize, decimalDigits, columnDefault, columnRs.getString("COLUMN_COMMENT"), isSQLKeyword(columnName)));
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(columnRs);
			JdbcUtils.closeStatement(columnPs);
		}
		if (fields.size() == 0) {
			throw new CommonException("表'" + tableName + "'没有字段");
		}
		//主键
		TablePrimaryKeyStruct primaryKey = null;
		PreparedStatement primaryKeyPs = null;
		ResultSet primaryKeyRs = null;
		try {
			String sql = "select COLUMN_NAME from INFORMATION_SCHEMA.STATISTICS where TABLE_SCHEMA=? and TABLE_NAME=? and INDEX_NAME=? order by SEQ_IN_INDEX";
			primaryKeyPs = conn.prepareStatement(sql);
			primaryKeyPs.setString(1, schema);
			primaryKeyPs.setString(2, tableName.toLowerCase());
			primaryKeyPs.setString(3, "PRIMARY");
			primaryKeyRs = JdbcUtils.executeQuery(primaryKeyPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, tableName.toLowerCase(), "PRIMARY" }));
			String primaryKeyFields = "";
			while (primaryKeyRs.next()) {
				if (primaryKeyFields.length() > 0) {
					primaryKeyFields += ",";
				}
				primaryKeyFields += primaryKeyRs.getString("COLUMN_NAME");
			}
			if (StringUtils.isNotBlank(primaryKeyFields)) {
				primaryKey = new TablePrimaryKeyStructImpl(primaryKeyFields);
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(primaryKeyRs);
			JdbcUtils.closeStatement(primaryKeyPs);
		}
		//索引
		ConcurrentMap<String, TableIndexStruct> indexes = new ConcurrentHashMap<String, TableIndexStruct>();
		PreparedStatement indexPs = null;
		ResultSet indexRs = null;
		try {
			String sql = "select INDEX_NAME,NON_UNIQUE,COLUMN_NAME,COLLATION from INFORMATION_SCHEMA.STATISTICS where TABLE_SCHEMA=? and TABLE_NAME=? and INDEX_NAME<>? order by INDEX_NAME,"
					+ "SEQ_IN_INDEX";
			indexPs = conn.prepareStatement(sql);
			indexPs.setString(1, schema);
			indexPs.setString(2, tableName.toLowerCase());
			indexPs.setString(3, "PRIMARY");
			indexRs = JdbcUtils.executeQuery(indexPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, tableName.toLowerCase(), "PRIMARY" }));
			String lIndexName = null;
			boolean unique = false;
			String indexField = "";
			while (indexRs.next()) {
				String indexName = indexRs.getString("INDEX_NAME");
				if (StringUtils.isNotBlank(lIndexName)) {
					if (!lIndexName.equals(indexName)) {
						indexes.put(lIndexName.toUpperCase(), new TableIndexStructImpl(lIndexName, unique, indexField));
						indexField = "";
					}
				}
				lIndexName = indexName;
				int nonUnique = indexRs.getInt("NON_UNIQUE");
				if (nonUnique == 1) {
					unique = false;
				}
				else if (nonUnique == 0) {
					unique = true;
				}
				if (indexField.length() > 0) {
					indexField += ",";
				}
				indexField += indexRs.getString("COLUMN_NAME");
				String collation = indexRs.getString("COLLATION");
				if (StringUtils.isNotBlank(collation)) {
					if (collation.equalsIgnoreCase("A")) {
						indexField += ":asc";
					}
					else if (collation.equalsIgnoreCase("D")) {
						indexField += ":desc";
					}
				}
				else {
					indexField += ":asc";
				}
			}
			if (StringUtils.isNotBlank(indexField)) {
				indexes.put(lIndexName.toUpperCase(), new TableIndexStructImpl(lIndexName, unique, indexField));
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(indexRs);
			JdbcUtils.closeStatement(indexPs);
		}
		return new TableObjectStructImpl(tableName, tableComment, fields, primaryKey, indexes);
	}

	@Override
	public ViewObjectStruct getViewStruct(Connection conn, String viewName) throws CommonException {
		String definition;
		PreparedStatement tablePs = null;
		ResultSet tableRs = null;
		try {
			String sql = "select VIEW_DEFINITION from INFORMATION_SCHEMA.VIEWS where TABLE_SCHEMA=? and TABLE_NAME=?";
			tablePs = conn.prepareStatement(sql);
			tablePs.setString(1, schema);
			tablePs.setString(2, viewName.toLowerCase());
			tableRs = JdbcUtils.executeQuery(tablePs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, viewName.toLowerCase() }));
			if (!tableRs.next()) {
				throw new CommonException("视图'" + viewName + "'不存在");
			}
			definition = tableRs.getString("VIEW_DEFINITION");
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(tableRs);
			JdbcUtils.closeStatement(tablePs);
		}
		//字段
		ConcurrentMap<String, FieldStruct> fields = new ConcurrentHashMap<String, FieldStruct>();
		PreparedStatement columnPs = null;
		ResultSet columnRs = null;
		try {
			String sql = "select COLUMN_NAME,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,CHARACTER_SET_NAME,NUMERIC_PRECISION,NUMERIC_SCALE from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA=? "
					+ "and TABLE_NAME=? order by ORDINAL_POSITION";
			columnPs = conn.prepareStatement(sql);
			columnPs.setString(1, schema);
			columnPs.setString(2, viewName.toLowerCase());
			columnRs = JdbcUtils.executeQuery(columnPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { schema, viewName.toLowerCase() }));
			while (columnRs.next()) {
				String columnName = columnRs.getString("COLUMN_NAME");
				String typeName = columnRs.getString("DATA_TYPE");
				int jdbcDataType = getJdbcDataType(typeName);
				long columnSize = 0;
				int decimalDigits = 0;
				if (jdbcDataType == Types.VARCHAR) {
					columnSize = columnRs.getLong("CHARACTER_MAXIMUM_LENGTH");
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if (supportsUnicodeStringType() && character.equals("utf8")) {
						jdbcDataType = Types.NVARCHAR;
					}
				}
				else if (jdbcDataType == Types.LONGVARCHAR) {
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if (supportsUnicodeStringType() && character.equals("utf8")) {
						jdbcDataType = Types.LONGNVARCHAR;
					}
				}
				else if (jdbcDataType == Types.DECIMAL) {
					columnSize = columnRs.getLong("NUMERIC_PRECISION");
					decimalDigits = columnRs.getInt("NUMERIC_SCALE");
				}
				fields.put(columnName.toUpperCase(), new FieldStructImpl(columnName, JdbcUtils.getDataType(jdbcDataType), jdbcDataType, typeName,
						columnSize, decimalDigits, isSQLKeyword(columnName)));
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(columnRs);
			JdbcUtils.closeStatement(columnPs);
		}
		if (fields.size() == 0) {
			throw new CommonException("视图'" + viewName + "'没有字段");
		}
		return new ViewObjectStructImpl(viewName, definition, fields);
	}

	@Override
	public String getAddColumnString() {
		return "add column";
	}

	@Override
	public String getModifyColumnString() {
		return "modify column";
	}

	@Override
	public boolean supportsUnicodeStringType() {
		return !"utf8".equalsIgnoreCase(character);
	}

	@Override
	public String getSQLKeywordMarks() {
		return "`";
	}
}