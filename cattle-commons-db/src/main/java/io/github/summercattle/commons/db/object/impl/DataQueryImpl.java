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
package io.github.summercattle.commons.db.object.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.field.FieldTypes;
import io.github.summercattle.commons.db.object.DataQuery;
import io.github.summercattle.commons.db.object.internal.RowLine;
import io.github.summercattle.commons.db.object.internal.impl.RowLineImpl;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class DataQueryImpl implements DataQuery {

	protected String tableName;

	protected int[] fieldTypes;

	protected int lineIndex = 0;

	protected String[] fieldNames;

	protected List<RowLine> lines = new Vector<RowLine>();

	protected Map<String, Integer> fieldIndexes = new HashMap<String, Integer>();

	public DataQueryImpl() {
	}

	public DataQueryImpl(Dialect dialect, ResultSet rs) throws CommonException {
		initFieldsInfo(dialect, rs);
		initLines(rs);
	}

	protected void initFieldsInfo(Dialect dialect, ResultSet rs) throws CommonException {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			fieldTypes = new int[columnCount];
			fieldNames = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				String jdbcColumnName = metaData.getColumnName(i + 1).toUpperCase();
				String jdbcColumnLabel = metaData.getColumnLabel(i + 1).toUpperCase();
				String columnName = jdbcColumnName.equals(jdbcColumnLabel) ? jdbcColumnName : jdbcColumnLabel;
				int columnType = metaData.getColumnType(i + 1);
				columnType = getColumnType(columnName, columnType, metaData.getColumnTypeName(i + 1));
				fieldIndexes.put(columnName.toUpperCase(), i);
				fieldNames[i] = columnName;
				fieldTypes[i] = columnType;
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	protected int getColumnType(String columnName, int columnType, String columnTypeName) throws CommonException {
		if (columnType == Types.OTHER) {
			if (columnTypeName.equals("DECFLOAT")) {
				columnType = Types.DECIMAL;
			}
			else {
				throw new CommonException("字段" + columnName + ",类型" + columnTypeName + "没有做处理");
			}
		}
		return columnType;
	}

	protected void initLines(ResultSet rs) throws CommonException {
		try {
			while (rs.next()) {
				intLine(rs);
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	protected void intLine(ResultSet rs) throws CommonException {
		Object[] values = new Object[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++) {
			values[i] = FieldTypes.getType(tableName, fieldNames[i], fieldTypes[i]).nullSafeGet(rs, fieldNames[i]);
		}
		lines.add(new RowLineImpl(values));
	}

	@Override
	public void beforeFirst() {
		lineIndex = 0;
	}

	@Override
	public boolean first() {
		boolean result = false;
		int size = size();
		if (size > 0) {
			lineIndex = 1;
			result = true;
		}
		return result;
	}

	@Override
	public boolean last() {
		boolean result = false;
		int size = size();
		if (size > 0) {
			lineIndex = size;
			result = true;
		}
		return result;
	}

	@Override
	public boolean next() {
		boolean result = false;
		int size = size();
		if (size > 0 && lineIndex + 1 <= size) {
			lineIndex++;
			result = true;
		}
		return result;
	}

	@Override
	public boolean absolute(int row) {
		boolean result = false;
		int size = size();
		if (size > 0 && row >= 1 && row <= size) {
			lineIndex = row;
			result = true;
		}
		return result;
	}

	@Override
	public String[] getFieldNames() {
		return fieldNames;
	}

	@Override
	public int size() {
		return lines.size();
	}

	@Override
	public byte[] getBytes(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getBytes(fieldIndex);
	}

	@Override
	public byte[] getBytes(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Array, byte.class, getObject(fieldIndex));
		return value == null ? null : (byte[]) value;
	}

	@Override
	public Object getObject(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getObject(fieldIndex);
	}

	@Override
	public Object getObject(int fieldIndex) throws CommonException {
		checkLineIndex();
		checkFieldIndex(fieldIndex);
		return lines.get(lineIndex - 1).get(fieldIndex - 1);
	}

	@Override
	public long getLong(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getLong(fieldIndex);
	}

	@Override
	public long getLong(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Long, getObject(fieldIndex));
		return value != null ? ((Long) value).longValue() : 0;
	}

	@Override
	public int getInt(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getInt(fieldIndex);
	}

	@Override
	public int getInt(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Int, getObject(fieldIndex));
		return value != null ? ((Integer) value).intValue() : 0;
	}

	@Override
	public BigDecimal getBigDecimal(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getBigDecimal(fieldIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.BigDecimal, getObject(fieldIndex));
		return value != null ? (BigDecimal) value : null;
	}

	@Override
	public double getDouble(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getDouble(fieldIndex);
	}

	@Override
	public double getDouble(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Double, getObject(fieldIndex));
		return value != null ? ((Double) value).doubleValue() : 0;
	}

	@Override
	public String getString(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getString(fieldIndex);
	}

	@Override
	public String getString(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.String, getObject(fieldIndex));
		return value != null ? (String) value : null;
	}

	@Override
	public Timestamp getTimestamp(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getTimestamp(fieldIndex);
	}

	@Override
	public Timestamp getTimestamp(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Timestamp, getObject(fieldIndex));
		return value != null ? (Timestamp) value : null;
	}

	@Override
	public Date getDate(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getDate(fieldIndex);
	}

	@Override
	public Date getDate(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Date, getObject(fieldIndex));
		return value != null ? (Date) value : null;
	}

	@Override
	public boolean getBoolean(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return getBoolean(fieldIndex);
	}

	@Override
	public boolean getBoolean(int fieldIndex) throws CommonException {
		Object value = ReflectUtils.convertValue(ClassType.Boolean, getObject(fieldIndex));
		return value != null ? (Boolean) value : false;
	}

	protected int getFieldIndex(String field) throws CommonException {
		if (fieldIndexes.containsKey(field.toUpperCase())) {
			return fieldIndexes.get(field.toUpperCase()) + 1;
		}
		throw new CommonException((StringUtils.isNotBlank(tableName) ? "表'" + tableName + "'" : "") + "没有找到字段'" + field + "'");
	}

	@Override
	public String toString(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return toString(fieldIndex);
	}

	@Override
	public String toString(int fieldIndex) throws CommonException {
		checkLineIndex();
		checkFieldIndex(fieldIndex);
		Object value = lines.get(lineIndex - 1).get(fieldIndex - 1);
		return value != null ? FieldTypes.getType(tableName, fieldNames[fieldIndex - 1], fieldTypes[fieldIndex - 1]).nullSafeToString(value) : null;
	}

	@Override
	public boolean isDateTypeField(String field) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		return isDateTypeField(fieldIndex);
	}

	@Override
	public boolean isDateTypeField(int fieldIndex) throws CommonException {
		boolean result = false;
		int fieldType = fieldTypes[fieldIndex - 1];
		if (fieldType == Types.DATE || fieldType == Types.TIMESTAMP || fieldType == Types.TIME) {
			result = true;
		}
		return result;
	}

	protected void checkFieldIndex(int fieldIndex) throws CommonException {
		if (fieldIndex < 1 || fieldIndex > fieldNames.length) {
			throw new CommonException("字段fieldIndex值" + fieldIndex + "越界,字段总数" + fieldNames.length);
		}
	}

	protected void checkLineIndex() throws CommonException {
		if (lineIndex < 1 || lineIndex > size()) {
			throw new CommonException("数据行越界,目前行" + lineIndex + ",总行数" + size());
		}
	}
}