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
import java.util.Locale;
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

public class Oracle8iDialect extends AbstractDialect {

	public Oracle8iDialect(Connection conn, String sqlKeywords) {
		super(sqlKeywords);
		PreparedStatement schemaPs = null;
		ResultSet schemaRs = null;
		try {
			String strSQL = "select SYS_CONTEXT('USERENV','CURRENT_SCHEMA') from dual";
			schemaPs = conn.prepareStatement(strSQL);
			schemaRs = JdbcUtils.executeQuery(schemaPs, "执行SQL语句:" + strSQL);
			if (schemaRs.next()) {
				schema = schemaRs.getString(1);
			}
		}
		catch (CommonException | SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			JdbcUtils.closeResultSet(schemaRs);
			JdbcUtils.closeStatement(schemaPs);
		}
		registerCharacterTypeMappings();
		registerNumericTypeMappings();
		registerDateTimeTypeMappings();
		registerLargeObjectTypeMappings();
		registerColumnType(DataType.Boolean, "number(1,0)");
	}

	protected void registerCharacterTypeMappings() {
		registerColumnType(DataType.String, 4000, "varchar2($l)");
		registerColumnType(DataType.NString, 4000, "nvarchar2($l)");
		registerColumnType(DataType.String, "clob");
		registerColumnType(DataType.NString, "nclob");
	}

	protected void registerNumericTypeMappings() {
		registerColumnType(DataType.Number, "number($p,$s)");
	}

	protected void registerLargeObjectTypeMappings() {
		registerColumnType(DataType.Blob, "blob");
		registerColumnType(DataType.Clob, "clob");
		registerColumnType(DataType.NClob, "nclob");
	}

	protected void registerDateTimeTypeMappings() {
		registerColumnType(DataType.Date, "date");
		registerColumnType(DataType.Timestamp, "date");
		registerColumnType(DataType.Time, "date");
	}

	@Override
	public DatabaseType getType() {
		return DatabaseType.Oracle;
	}

	@Override
	public boolean supportsCurrentTimestampSelection() {
		return true;
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
	public boolean supportsPageLimitOffset() {
		return true;
	}

	@Override
	public String getPageLimitString(String sql, int startRowNum, int perPageSize) throws CommonException {
		boolean isForUpdate = false;
		if (sql.toLowerCase(Locale.ROOT).endsWith(" for update")) {
			sql = sql.substring(0, sql.length() - 11);
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
			pagingSelect.append(") row_) where " + ROW_NUMBER_FIELD + "<=" + String.valueOf(startRowNum + perPageSize) + " and " + ROW_NUMBER_FIELD
					+ ">" + String.valueOf(startRowNum));
		}
		else {
			pagingSelect.append(") where rownum<=" + String.valueOf(perPageSize));
		}
		if (isForUpdate) {
			pagingSelect.append(" for update");
		}
		return pagingSelect.toString();
	}

	@Override
	public boolean supportsSequences() {
		return true;
	}

	@Override
	public String getCreateSequenceString(String sequenceName) {
		return "create sequence EAPP_" + sequenceName;
	}

	@Override
	public String getSequenceNextValString(String sequenceName) {
		return "select EAPP_" + sequenceName + ".nextval from dual";
	}

	@Override
	public String getQuerySequencesString() {
		return "select sequence_name from all_sequences union select synonym_name from all_synonyms us,all_sequences asq where asq.sequence_name=us.table_name and asq.sequence_owner=us.table_owner";
	}

	@Override
	public String getValidateQuery() {
		return "select 1 from dual";
	}

	@Override
	public String getForUpdateString() {
		return " for update";
	}

	@Override
	public boolean existTable(Connection conn, String tableName) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select count(*) FROM user_tables where TABLE_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableName.toUpperCase());
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase() }));
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
			String sql = "select count(*) FROM user_views where VIEW_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, viewName.toUpperCase());
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { viewName.toUpperCase() }));
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
		String tableComment;
		PreparedStatement tablePs = null;
		ResultSet tableRs = null;
		try {
			String sql = "select COMMENTS from user_tab_comments where TABLE_NAME=?";
			tablePs = conn.prepareStatement(sql);
			tablePs.setString(1, tableName.toUpperCase());
			tableRs = JdbcUtils.executeQuery(tablePs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase() }));
			if (!tableRs.next()) {
				throw new CommonException("表'" + tableName + "'不存在");
			}
			tableComment = tableRs.getString("COMMENTS");
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
			String sql = "select COLUMN_NAME,DATA_TYPE,NULLABLE,DATA_DEFAULT,CHAR_LENGTH,DATA_PRECISION,DATA_SCALE from user_tab_columns where TABLE_NAME=? order by COLUMN_ID";
			columnPs = conn.prepareStatement(sql);
			columnPs.setString(1, tableName.toUpperCase());
			columnRs = JdbcUtils.executeQuery(columnPs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase() }));
			while (columnRs.next()) {
				String columnName = columnRs.getString("COLUMN_NAME");
				String typeName = columnRs.getString("DATA_TYPE");
				int k = typeName.indexOf("(");
				if (k >= 0) {
					typeName = typeName.substring(0, k);
				}
				boolean isNullable = columnRs.getString("NULLABLE").equalsIgnoreCase("Y");
				String dataDefault = columnRs.getString("DATA_DEFAULT");
				int jdbcDataType = getJdbcDataType(typeName);
				if (StringUtils.isNotBlank(dataDefault) && (jdbcDataType == Types.VARCHAR || jdbcDataType == Types.NVARCHAR)) {
					if (dataDefault.equalsIgnoreCase("null")) {
						dataDefault = null;
					}
					else {
						dataDefault = dataDefault.substring(1, dataDefault.length() - 1);
					}
				}
				long columnSize = 0;
				int decimalDigits = 0;
				if (jdbcDataType == Types.VARCHAR || jdbcDataType == Types.NVARCHAR) {
					columnSize = columnRs.getLong("CHAR_LENGTH");
				}
				else if (jdbcDataType == Types.DECIMAL || jdbcDataType == Types.NUMERIC) {
					columnSize = columnRs.getLong("DATA_PRECISION");
					decimalDigits = columnRs.getInt("DATA_SCALE");
				}
				fields.put(columnName.toUpperCase(), new TableFieldStructImpl(columnName, JdbcUtils.getDataType(jdbcDataType), jdbcDataType, typeName,
						isNullable, columnSize, decimalDigits, dataDefault, getFieldComment(conn, tableName, columnName), isSQLKeyword(columnName)));
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
		String primaryKeyIndexName = null;
		try {
			String sql = "select CONSTRAINT_NAME,INDEX_NAME from user_constraints where TABLE_NAME=? and CONSTRAINT_TYPE=?";
			primaryKeyPs = conn.prepareStatement(sql);
			primaryKeyPs.setString(1, tableName.toUpperCase());
			primaryKeyPs.setString(2, "P");
			primaryKeyRs = JdbcUtils.executeQuery(primaryKeyPs,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase(), "P" }));
			if (primaryKeyRs.next()) {
				String constraintName = primaryKeyRs.getString("CONSTRAINT_NAME");
				primaryKeyIndexName = primaryKeyRs.getString("INDEX_NAME");
				PreparedStatement primaryKeyColumnPs = null;
				ResultSet primaryKeyColumnRs = null;
				try {
					sql = "select COLUMN_NAME from user_cons_columns where TABLE_NAME=? and CONSTRAINT_NAME=? order by POSITION";
					primaryKeyColumnPs = conn.prepareStatement(sql);
					primaryKeyColumnPs.setString(1, tableName.toUpperCase());
					primaryKeyColumnPs.setString(2, constraintName);
					primaryKeyColumnRs = JdbcUtils.executeQuery(primaryKeyColumnPs,
							"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase(), constraintName }));
					String primaryKeyFields = "";
					while (primaryKeyColumnRs.next()) {
						if (primaryKeyFields.length() > 0) {
							primaryKeyFields += ",";
						}
						primaryKeyFields += primaryKeyColumnRs.getString("COLUMN_NAME");
					}
					if (StringUtils.isNotBlank(primaryKeyFields)) {
						primaryKey = new TablePrimaryKeyStructImpl(constraintName, primaryKeyFields);
					}
				}
				finally {
					JdbcUtils.closeResultSet(primaryKeyColumnRs);
					JdbcUtils.closeStatement(primaryKeyColumnPs);
				}
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
			String sql = "select INDEX_NAME,UNIQUENESS from user_indexes where TABLE_NAME=?"
					+ (StringUtils.isNotBlank(primaryKeyIndexName) ? " and INDEX_NAME<>?" : "");
			indexPs = conn.prepareStatement(sql);
			indexPs.setString(1, tableName.toUpperCase());
			if (StringUtils.isNotBlank(primaryKeyIndexName)) {
				indexPs.setString(2, primaryKeyIndexName);
			}
			indexRs = JdbcUtils
					.executeQuery(indexPs,
							"执行SQL语句:" + sql + ",参数值:"
									+ ArrayUtils.toString(StringUtils.isNotBlank(primaryKeyIndexName)
											? new Object[] { tableName.toUpperCase(), primaryKeyIndexName }
											: new Object[] { tableName.toUpperCase() }));
			while (indexRs.next()) {
				String indexName = indexRs.getString("INDEX_NAME");
				String uniqueness = indexRs.getString("UNIQUENESS");
				boolean unique = false;
				if (uniqueness.equals("UNIQUE")) {
					unique = true;
				}
				else if (uniqueness.equals("NONUNIQUE")) {
					unique = false;
				}
				PreparedStatement indexColumnPs = null;
				ResultSet indexColumnRs = null;
				try {
					sql = "select COLUMN_NAME,COLUMN_POSITION,DESCEND from user_ind_columns where TABLE_NAME=? AND INDEX_NAME=? order by COLUMN_POSITION";
					indexColumnPs = conn.prepareStatement(sql);
					indexColumnPs.setString(1, tableName.toUpperCase());
					indexColumnPs.setString(2, indexName);
					indexColumnRs = JdbcUtils.executeQuery(indexColumnPs,
							"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase(), indexName }));
					String indexField = "";
					while (indexColumnRs.next()) {
						if (indexField.length() > 0) {
							indexField += ",";
						}
						String columnName = indexColumnRs.getString("COLUMN_NAME");
						if (columnName.startsWith("SYS_NC")) {
							int columnPosition = indexColumnRs.getInt("COLUMN_POSITION");
							String lColumnName = getIndexField(conn, tableName, indexName, columnPosition);
							columnName = StringUtils.isNotBlank(lColumnName) ? lColumnName : columnName;
						}
						indexField += columnName;
						String descend = indexColumnRs.getString("DESCEND");
						if (StringUtils.isNotBlank(descend)) {
							if (descend.equalsIgnoreCase("ASC")) {
								indexField += ":asc";
							}
							else if (descend.equalsIgnoreCase("DESC")) {
								indexField += ":desc";
							}
						}
						else {
							indexField += ":asc";
						}
					}
					indexes.put(indexName.toUpperCase(), new TableIndexStructImpl(indexName, unique, indexField));
				}
				finally {
					JdbcUtils.closeResultSet(indexColumnRs);
					JdbcUtils.closeStatement(indexColumnPs);
				}
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

	private String getFieldComment(Connection conn, String tableName, String fieldName) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select COMMENTS from user_col_comments where TABLE_NAME=? and COLUMN_NAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableName);
			ps.setString(2, fieldName);
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName, fieldName }));
			if (rs.next()) {
				return rs.getString("COMMENTS");
			}
			return null;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
	}

	private String getIndexField(Connection conn, String tableName, String indexName, int columnPosition) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select COLUMN_EXPRESSION from user_ind_expressions where TABLE_NAME=? and INDEX_NAME=? and COLUMN_POSITION=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, tableName.toUpperCase());
			ps.setString(2, indexName);
			ps.setInt(3, columnPosition);
			rs = JdbcUtils.executeQuery(ps,
					"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { tableName.toUpperCase(), indexName, columnPosition }));
			String expression = null;
			if (rs.next()) {
				expression = rs.getString("COLUMN_EXPRESSION");
			}
			if (StringUtils.isNotBlank(expression)) {
				expression = expression.substring(1, expression.length() - 1);
			}
			return expression;
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
	public String getAddColumnString() {
		return "add";
	}

	@Override
	public String getModifyColumnString() {
		return "modify";
	}

	@Override
	public String getSQLKeywordMarks() {
		return "\"";
	}

	@Override
	public ViewObjectStruct getViewStruct(Connection conn, String viewName) throws CommonException {
		String definition;
		PreparedStatement tablePs = null;
		ResultSet tableRs = null;
		try {
			String sql = "select TEXT from user_views where VIEW_NAME=?";
			tablePs = conn.prepareStatement(sql);
			tablePs.setString(1, viewName.toUpperCase());
			tableRs = JdbcUtils.executeQuery(tablePs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { viewName.toUpperCase() }));
			if (!tableRs.next()) {
				throw new CommonException("视图'" + viewName + "'不存在");
			}
			definition = tableRs.getString("TEXT");
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
			String sql = "select COLUMN_NAME,DATA_TYPE,CHAR_LENGTH,DATA_PRECISION,DATA_SCALE from user_tab_columns where TABLE_NAME=? order by COLUMN_ID";
			columnPs = conn.prepareStatement(sql);
			columnPs.setString(1, viewName.toUpperCase());
			columnRs = JdbcUtils.executeQuery(columnPs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { viewName.toUpperCase() }));
			while (columnRs.next()) {
				String columnName = columnRs.getString("COLUMN_NAME");
				String typeName = columnRs.getString("DATA_TYPE");
				int k = typeName.indexOf("(");
				if (k >= 0) {
					typeName = typeName.substring(0, k);
				}
				int jdbcDataType = getJdbcDataType(typeName);
				long columnSize = 0;
				int decimalDigits = 0;
				if (jdbcDataType == Types.VARCHAR || jdbcDataType == Types.NVARCHAR) {
					columnSize = columnRs.getLong("CHAR_LENGTH");
				}
				else if (jdbcDataType == Types.DECIMAL || jdbcDataType == Types.NUMERIC) {
					columnSize = columnRs.getLong("DATA_PRECISION");
					decimalDigits = columnRs.getInt("DATA_SCALE");
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
}