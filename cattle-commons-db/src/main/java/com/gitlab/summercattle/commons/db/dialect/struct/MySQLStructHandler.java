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
package com.gitlab.summercattle.commons.db.dialect.struct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.dialect.StructHandler;
import com.gitlab.summercattle.commons.db.struct.FieldStruct;
import com.gitlab.summercattle.commons.db.struct.TableIndexStruct;
import com.gitlab.summercattle.commons.db.struct.TableObjectStruct;
import com.gitlab.summercattle.commons.db.struct.TablePrimaryKeyStruct;
import com.gitlab.summercattle.commons.db.struct.ViewObjectStruct;
import com.gitlab.summercattle.commons.db.struct.impl.FieldStructImpl;
import com.gitlab.summercattle.commons.db.struct.impl.TableFieldStructImpl;
import com.gitlab.summercattle.commons.db.struct.impl.TableIndexStructImpl;
import com.gitlab.summercattle.commons.db.struct.impl.TableObjectStructImpl;
import com.gitlab.summercattle.commons.db.struct.impl.TablePrimaryKeyStructImpl;
import com.gitlab.summercattle.commons.db.struct.impl.ViewObjectStructImpl;
import com.gitlab.summercattle.commons.db.utils.JdbcUtils;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.auxiliary.ArrayUtils;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class MySQLStructHandler implements StructHandler {

	private final Dialect dialect;

	public MySQLStructHandler(Dialect dialect) {
		this.dialect = dialect;
	}

	@Override
	public boolean existTable(Connection conn, String name) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String tableType = "BASE TABLE";
			String sql = "select count(*) from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA=? and TABLE_TYPE=? and TABLE_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, dialect.getSchema());
			ps.setString(2, tableType);
			ps.setString(3, name.toLowerCase());
			rs = JdbcUtils.executeQuery(ps,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), tableType, name.toLowerCase() }));
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
	public TableObjectStruct getTable(Connection conn, String name) throws CommonException {
		//表
		String tableType = "BASE TABLE";
		String tableComment;
		PreparedStatement tablePs = null;
		ResultSet tableRs = null;
		try {
			String sql = "select TABLE_COMMENT from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA=? and TABLE_TYPE=? and TABLE_NAME=?";
			tablePs = conn.prepareStatement(sql);
			tablePs.setString(1, dialect.getSchema());
			tablePs.setString(2, tableType);
			tablePs.setString(3, name.toLowerCase());
			tableRs = JdbcUtils.executeQuery(tablePs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), tableType, name.toLowerCase() }));
			if (!tableRs.next()) {
				throw new CommonException("表'" + name + "'不存在");
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
			columnPs.setString(1, dialect.getSchema());
			columnPs.setString(2, name.toLowerCase());
			columnRs = JdbcUtils.executeQuery(columnPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), name.toLowerCase() }));
			while (columnRs.next()) {
				String columnName = columnRs.getString("COLUMN_NAME");
				String typeName = columnRs.getString("DATA_TYPE");
				boolean isNullable = columnRs.getString("IS_NULLABLE").equalsIgnoreCase("YES");
				String columnDefault = columnRs.getString("COLUMN_DEFAULT");
				int jdbcDataType = JdbcUtils.getJdbcDataType(typeName);
				long columnSize = 0;
				int decimalDigits = 0;
				if (jdbcDataType == Types.VARCHAR) {
					columnSize = columnRs.getLong("CHARACTER_MAXIMUM_LENGTH");
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if ("utf8".equalsIgnoreCase(character)) {
						jdbcDataType = Types.NVARCHAR;
					}
				}
				else if (jdbcDataType == Types.CLOB) {
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if ("utf8".equalsIgnoreCase(character)) {
						jdbcDataType = Types.NCLOB;
					}
				}
				else if (jdbcDataType == Types.DECIMAL) {
					columnSize = columnRs.getLong("NUMERIC_PRECISION");
					decimalDigits = columnRs.getInt("NUMERIC_SCALE");
				}
				else if (jdbcDataType == Types.BIT) {
					if (StringUtils.isNotBlank(columnDefault)) {
						if (columnDefault.length() > 3 && columnDefault.startsWith("b'") && columnDefault.endsWith("'")) {
							columnDefault = columnDefault.substring(2, columnDefault.length() - 1);
						}
					}
				}
				fields.put(columnName.toUpperCase(), new TableFieldStructImpl(columnName, jdbcDataType, typeName, isNullable, columnSize,
						decimalDigits, columnDefault, columnRs.getString("COLUMN_COMMENT")));
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
			throw new CommonException("表'" + name + "'没有字段");
		}
		//主键
		TablePrimaryKeyStruct primaryKey = null;
		PreparedStatement primaryKeyPs = null;
		ResultSet primaryKeyRs = null;
		try {
			String sql = "select COLUMN_NAME from INFORMATION_SCHEMA.STATISTICS where TABLE_SCHEMA=? and TABLE_NAME=? and INDEX_NAME=? order by SEQ_IN_INDEX";
			primaryKeyPs = conn.prepareStatement(sql);
			primaryKeyPs.setString(1, dialect.getSchema());
			primaryKeyPs.setString(2, name.toLowerCase());
			primaryKeyPs.setString(3, "PRIMARY");
			primaryKeyRs = JdbcUtils.executeQuery(primaryKeyPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), name.toLowerCase(), "PRIMARY" }));
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
			indexPs.setString(1, dialect.getSchema());
			indexPs.setString(2, name.toLowerCase());
			indexPs.setString(3, "PRIMARY");
			indexRs = JdbcUtils.executeQuery(indexPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), name.toLowerCase(), "PRIMARY" }));
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
		return new TableObjectStructImpl(name, tableComment, fields, primaryKey, indexes);
	}

	@Override
	public boolean existView(Connection conn, String name) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select count(*) from INFORMATION_SCHEMA.VIEWS where TABLE_SCHEMA=? and TABLE_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, dialect.getSchema());
			ps.setString(2, name.toLowerCase());
			rs = JdbcUtils.executeQuery(ps,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), name.toLowerCase() }));
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
	public ViewObjectStruct getView(Connection conn, String name) throws CommonException {
		String definition;
		PreparedStatement tablePs = null;
		ResultSet tableRs = null;
		try {
			String sql = "select VIEW_DEFINITION from INFORMATION_SCHEMA.VIEWS where TABLE_SCHEMA=? and TABLE_NAME=?";
			tablePs = conn.prepareStatement(sql);
			tablePs.setString(1, dialect.getSchema());
			tablePs.setString(2, name.toLowerCase());
			tableRs = JdbcUtils.executeQuery(tablePs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), name.toLowerCase() }));
			if (!tableRs.next()) {
				throw new CommonException("视图'" + name + "'不存在");
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
			columnPs.setString(1, dialect.getSchema());
			columnPs.setString(2, name.toLowerCase());
			columnRs = JdbcUtils.executeQuery(columnPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { dialect.getSchema(), name.toLowerCase() }));
			while (columnRs.next()) {
				String columnName = columnRs.getString("COLUMN_NAME");
				String typeName = columnRs.getString("DATA_TYPE");
				int jdbcDataType = JdbcUtils.getJdbcDataType(typeName);
				long columnSize = 0;
				int decimalDigits = 0;
				if (jdbcDataType == Types.VARCHAR) {
					columnSize = columnRs.getLong("CHARACTER_MAXIMUM_LENGTH");
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if (character.equals("utf8")) {
						jdbcDataType = Types.NVARCHAR;
					}
				}
				else if (jdbcDataType == Types.CLOB) {
					String character = columnRs.getString("CHARACTER_SET_NAME");
					if (character.equals("utf8")) {
						jdbcDataType = Types.NCLOB;
					}
				}
				else if (jdbcDataType == Types.DECIMAL) {
					columnSize = columnRs.getLong("NUMERIC_PRECISION");
					decimalDigits = columnRs.getInt("NUMERIC_SCALE");
				}
				fields.put(columnName.toUpperCase(), new FieldStructImpl(columnName, jdbcDataType, typeName, columnSize, decimalDigits));
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
			throw new CommonException("视图'" + name + "'没有字段");
		}
		return new ViewObjectStructImpl(name, definition, fields);
	}
}