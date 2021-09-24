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
package com.gitlab.summercattle.commons.db.object.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.gitlab.summercattle.commons.db.DbUtils;
import com.gitlab.summercattle.commons.db.configure.DbProperties;
import com.gitlab.summercattle.commons.db.constants.DataConstants;
import com.gitlab.summercattle.commons.db.field.FieldTypes;
import com.gitlab.summercattle.commons.db.meta.FieldMeta;
import com.gitlab.summercattle.commons.db.meta.TableMeta;
import com.gitlab.summercattle.commons.db.object.DataTable;
import com.gitlab.summercattle.commons.db.object.internal.InternalDataTable;
import com.gitlab.summercattle.commons.db.object.internal.RowLineSet;
import com.gitlab.summercattle.commons.db.object.internal.RowStatus;
import com.gitlab.summercattle.commons.db.object.internal.impl.RowLineSetImpl;
import com.gitlab.summercattle.commons.db.struct.TableFieldStruct;
import com.gitlab.summercattle.commons.db.struct.TableObjectStruct;
import com.gitlab.summercattle.commons.db.utils.JdbcUtils;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.auxiliary.CommonUtils;
import com.gitlab.summercattle.commons.utils.reflect.ClassType;
import com.gitlab.summercattle.commons.utils.reflect.ReflectUtils;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;

public class DataTableImpl extends DataQueryImpl implements DataTable, InternalDataTable {

	protected boolean[] fieldAllowNulls;

	protected long[] fieldLengths;

	protected int[] fieldScales;

	private List<RowLineSet> deleteLines = new Vector<RowLineSet>();

	private DbProperties dbProperties;

	private boolean primaryKeyUseNumber;

	private boolean useCache;

	private String alias;

	public DataTableImpl(DbProperties dbProperties, ResultSet rs, TableMeta tableMeta, TableObjectStruct tableStructure) throws CommonException {
		this(dbProperties, tableMeta, tableStructure);
		initLines(rs);
	}

	public DataTableImpl(DbProperties dbProperties, TableMeta tableMeta, TableObjectStruct tableStructure) throws CommonException {
		this.dbProperties = dbProperties;
		alias = tableMeta.getAlias();
		initFieldsInfo(tableMeta, tableStructure);
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField());
		useCache = tableMeta.isUseCache();
		primaryKeyUseNumber = tableMeta.isPrimaryKeyUseNumber();
		int primaryType = fieldTypes[primaryFieldIndex.intValue()];
		if (primaryKeyUseNumber) {
			if (!JdbcUtils.isNumeric(primaryType)) {
				throw new CommonException("表'" + tableStructure.getName() + "'的主键字段'" + dbProperties.getPrimaryField() + "'必须为数值型,目前字段类型值'"
						+ tableStructure.getField(dbProperties.getPrimaryField()).getTypeName() + "'");
			}
		}
		else {
			if (!JdbcUtils.isString(primaryType)) {
				throw new CommonException("表'" + tableStructure.getName() + "'的主键字段'" + dbProperties.getPrimaryField() + "'必须为字符型,目前字段类型值'"
						+ tableStructure.getField(dbProperties.getPrimaryField()).getTypeName() + "'");
			}
		}
	}

	@Override
	protected void intLine(ResultSet rs) throws CommonException {
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		Object[] values = new Object[fieldNames.length + DataConstants.SYSTEM_DEFAULT_COLUMN_SIZE];
		for (int i = 0; i < fieldNames.length; i++) {
			values[i] = FieldTypes.getType(tableName, fieldNames[i], fieldTypes[i]).nullSafeGet(rs, fieldNames[i]);
		}
		Integer createDateFieldIndex = fieldIndexes.get(dbProperties.getCreateTimeField());
		values[createDateFieldIndex.intValue()] = FieldTypes
				.getType(tableName, dbProperties.getCreateTimeField(), fieldTypes[createDateFieldIndex.intValue()])
				.nullSafeGet(rs, dbProperties.getCreateTimeField());
		Integer modifyDateFieldIndex = fieldIndexes.get(dbProperties.getUpdateTimeField());
		values[modifyDateFieldIndex.intValue()] = FieldTypes
				.getType(tableName, dbProperties.getUpdateTimeField(), fieldTypes[modifyDateFieldIndex.intValue()])
				.nullSafeGet(rs, dbProperties.getUpdateTimeField());
		Integer deletedFieldIndex = fieldIndexes.get(dbProperties.getDeletedField());
		values[deletedFieldIndex.intValue()] = FieldTypes.getType(tableName, dbProperties.getDeletedField(), fieldTypes[deletedFieldIndex.intValue()])
				.nullSafeGet(rs, dbProperties.getDeletedField());
		Integer versionFieldIndex = fieldIndexes.get(dbProperties.getVersionField());
		values[versionFieldIndex.intValue()] = FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()])
				.nullSafeGet(rs, dbProperties.getVersionField());
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField());
		values[primaryFieldIndex.intValue()] = FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()])
				.nullSafeGet(rs, dbProperties.getPrimaryField());
		if (values[primaryFieldIndex.intValue()] == null) {
			throw new CommonException("表'" + tableName + "'的主键字段'" + dbProperties.getPrimaryField() + "'不允许为空");
		}
		lines.add(new RowLineSetImpl(RowStatus.Init, tableName, fieldNames, fieldTypes, values));
	}

	private void setFieldInfo(TableFieldStruct fieldStructure, String fieldName, int fieldIndex) throws CommonException {
		fieldTypes[fieldIndex] = fieldStructure.getJdbcType();
		fieldAllowNulls[fieldIndex] = fieldStructure.isNullable();
		fieldLengths[fieldIndex] = fieldStructure.getLength();
		fieldScales[fieldIndex] = fieldStructure.getScale();
		fieldIndexes.put(fieldName.toUpperCase(), fieldIndex);
	}

	private void setSystemFieldInfo(TableFieldStruct fieldStructure, String fieldName, int fieldIndex) throws CommonException {
		fieldTypes[fieldIndex] = fieldStructure.getJdbcType();
		fieldIndexes.put(fieldName.toUpperCase(), fieldIndex);
	}

	private void initFieldsInfo(TableMeta tableMeta, TableObjectStruct tableStructure) throws CommonException {
		tableName = tableMeta.getName();
		FieldMeta[] fieldMetas = tableMeta.getFields();
		int fieldCount = 0;
		for (FieldMeta fieldMeta : fieldMetas) {
			String fieldName = fieldMeta.getName();
			if (!fieldName.equalsIgnoreCase(dbProperties.getCreateTimeField()) && !fieldName.equalsIgnoreCase(dbProperties.getUpdateTimeField())
					&& !fieldName.equalsIgnoreCase(dbProperties.getDeletedField()) && !fieldName.equalsIgnoreCase(dbProperties.getVersionField())
					&& !fieldName.equalsIgnoreCase(dbProperties.getPrimaryField())) {
				fieldCount++;
			}
		}
		fieldTypes = new int[fieldCount + DataConstants.SYSTEM_DEFAULT_COLUMN_SIZE];
		fieldNames = new String[fieldCount];
		fieldAllowNulls = new boolean[fieldCount];
		fieldLengths = new long[fieldCount];
		fieldScales = new int[fieldCount];
		int fieldIndex = 0;
		for (FieldMeta fieldMeta : fieldMetas) {
			String fieldName = fieldMeta.getName();
			if (!fieldName.equalsIgnoreCase(dbProperties.getCreateTimeField()) && !fieldName.equalsIgnoreCase(dbProperties.getUpdateTimeField())
					&& !fieldName.equalsIgnoreCase(dbProperties.getDeletedField()) && !fieldName.equalsIgnoreCase(dbProperties.getVersionField())
					&& !fieldName.equalsIgnoreCase(dbProperties.getPrimaryField())) {
				TableFieldStruct fieldStructure = (TableFieldStruct) tableStructure.getField(fieldName);
				setFieldInfo(fieldStructure, fieldMeta.getName(), fieldIndex);
				fieldNames[fieldIndex] = fieldName;
				fieldIndex++;
			}
		}
		TableFieldStruct createDateFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getCreateTimeField());
		setSystemFieldInfo(createDateFieldStructure, dbProperties.getCreateTimeField(), fieldIndex);
		fieldIndex++;
		TableFieldStruct modifyDateFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getUpdateTimeField());
		setSystemFieldInfo(modifyDateFieldStructure, dbProperties.getUpdateTimeField(), fieldIndex);
		fieldIndex++;
		TableFieldStruct deletedFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getDeletedField());
		setSystemFieldInfo(deletedFieldStructure, dbProperties.getDeletedField(), fieldIndex);
		fieldIndex++;
		TableFieldStruct versionFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getVersionField());
		setSystemFieldInfo(versionFieldStructure, dbProperties.getVersionField(), fieldIndex);
		fieldIndex++;
		TableFieldStruct primaryFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getPrimaryField());
		setSystemFieldInfo(primaryFieldStructure, dbProperties.getPrimaryField(), fieldIndex);
	}

	@Override
	public String getName() {
		return tableName;
	}

	@Override
	public RowLineSet[] getDeleteLines() {
		return deleteLines.toArray(new RowLineSet[0]);
	}

	@Override
	public int[] getFieldTypes() {
		return fieldTypes;
	}

	private void checkStringFieldValue(String fieldName, int valueLength, long fieldPrecision) throws CommonException {
		if (fieldPrecision != -1 && valueLength > fieldPrecision) {
			throw new CommonException("表" + tableName + ",字段" + fieldName + ",值的长度" + valueLength + "超过字段最大长度" + fieldPrecision);
		}
	}

	@Override
	public void insert() throws CommonException {
		if (primaryKeyUseNumber) {
			insert(DbUtils.getDbTool().getSequenceNextVal(tableName));
		}
		else {
			insert(CommonUtils.getUUID());
		}
	}

	@Override
	public void insert(Object primaryValue) throws CommonException {
		if (null == primaryValue) {
			throw new CommonException("主键值为空");
		}
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField());
		Object[] values = new Object[fieldNames.length + DataConstants.SYSTEM_DEFAULT_COLUMN_SIZE];
		if (primaryKeyUseNumber) {
			Class< ? > returnedClass = FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()])
					.getReturnedClass();
			ClassType fieldType = ReflectUtils.getClassType(returnedClass);
			values[primaryFieldIndex.intValue()] = ReflectUtils.convertValue(fieldType, null, primaryValue);
		}
		else {
			values[primaryFieldIndex.intValue()] = ReflectUtils.convertValue(ClassType.String, null, primaryValue);
		}
		lines.add(new RowLineSetImpl(RowStatus.Add, tableName, fieldNames, fieldTypes, values));
		lineIndex = lines.size();
	}

	@Override
	public void delete() throws CommonException {
		checkLineIndex();
		RowStatus status = ((RowLineSet) lines.get(lineIndex - 1)).getStatus();
		if (status != RowStatus.Add) {
			deleteLines.add((RowLineSet) lines.get(lineIndex - 1));
		}
		lines.remove(lineIndex - 1);
		if (lineIndex > lines.size()) {
			lineIndex = lines.size();
		}
		else {
			if (lines.size() > 0) {
				lineIndex--;
			}
			else {
				lineIndex = 0;
			}
		}
	}

	@Override
	public Object getPrimaryValue() throws CommonException {
		checkLineIndex();
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField());
		return lines.get(lineIndex - 1).get(primaryFieldIndex.intValue());
	}

	@Override
	public void setLong(String field, long value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setLong(fieldIndex, value);
	}

	@Override
	public void setLong(int fieldIndex, long value) throws CommonException {
		setObject(fieldIndex, new Long(value));
	}

	@Override
	public void setInt(String field, int value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setInt(fieldIndex, value);
	}

	@Override
	public void setInt(int fieldIndex, int value) throws CommonException {
		setObject(fieldIndex, new Integer(value));
	}

	@Override
	public void setBigDecimal(String field, BigDecimal value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setBigDecimal(fieldIndex, value);
	}

	@Override
	public void setBigDecimal(int fieldIndex, BigDecimal value) throws CommonException {
		setObject(fieldIndex, value);
	}

	@Override
	public void setDouble(String field, double value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setDouble(fieldIndex, value);
	}

	@Override
	public void setDouble(int fieldIndex, double value) throws CommonException {
		setObject(fieldIndex, new Double(value));
	}

	@Override
	public void setString(String field, String value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setString(fieldIndex, value);
	}

	@Override
	public void setString(int fieldIndex, String value) throws CommonException {
		setObject(fieldIndex, value);
	}

	@Override
	public void setTimestamp(String field, Timestamp value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setTimestamp(fieldIndex, value);
	}

	@Override
	public void setTimestamp(int fieldIndex, Timestamp value) throws CommonException {
		setObject(fieldIndex, value);
	}

	@Override
	public void setDate(String field, Date value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setDate(fieldIndex, value);
	}

	@Override
	public void setDate(int fieldIndex, Date value) throws CommonException {
		setObject(fieldIndex, value);
	}

	@Override
	public void setBoolean(String field, boolean value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setBoolean(fieldIndex, value);
	}

	@Override
	public void setBoolean(int fieldIndex, boolean value) throws CommonException {
		setObject(fieldIndex, value);
	}

	@Override
	public void setBytes(String field, byte[] value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setBytes(fieldIndex, value);
	}

	@Override
	public void setBytes(int fieldIndex, byte[] value) throws CommonException {
		setObject(fieldIndex, value);
	}

	@Override
	public void setObject(String field, Object value) throws CommonException {
		int fieldIndex = getFieldIndex(field);
		setObject(fieldIndex, value);
	}

	@Override
	public void setObject(int fieldIndex, Object value) throws CommonException {
		checkLineIndex();
		checkFieldIndex(fieldIndex);
		Class< ? > returnedClass = FieldTypes.getType(tableName, fieldNames[fieldIndex - 1], fieldTypes[fieldIndex - 1]).getReturnedClass();
		ClassType fieldType = ReflectUtils.getClassType(returnedClass);
		Object lValue = ReflectUtils.convertValue(fieldType, fieldType == ClassType.Array ? returnedClass.getComponentType() : null, value);
		if (fieldType == ClassType.String && lValue != null
				&& (fieldTypes[fieldIndex - 1] == Types.VARCHAR || fieldTypes[fieldIndex - 1] == Types.NVARCHAR)) {
			checkStringFieldValue(fieldNames[fieldIndex - 1], lValue.toString().length(), fieldLengths[fieldIndex - 1]);
		}
		((RowLineSet) lines.get(lineIndex - 1)).set(fieldIndex - 1, lValue);
	}

	@Override
	public void setAddAndModifyLines(List<RowLineSet> addLines, List<RowLineSet> modifyLines) throws CommonException {
		for (int i = 0; i < lines.size(); i++) {
			Object[] values = ((RowLineSet) lines.get(i)).getValues();
			for (int t = 0; t < fieldAllowNulls.length; t++) {
				if (!fieldAllowNulls[t] && values[t] == null) {
					throw new CommonException("表" + tableName + ",数据行第" + lineIndex + "行,字段" + fieldNames[t] + "的值不允许为空");
				}
			}
			RowStatus status = ((RowLineSet) lines.get(i)).getStatus();
			if (status == RowStatus.Add) {
				addLines.add((RowLineSet) lines.get(i));
			}
			else if (status == RowStatus.Modify) {
				modifyLines.add((RowLineSet) lines.get(i));
			}
		}
	}

	@Override
	public Map<String, Integer> getFieldIndexes() {
		return fieldIndexes;
	}

	@Override
	public long getVersion() throws CommonException {
		Integer fieldIndex = fieldIndexes.get(dbProperties.getVersionField());
		Object value = ReflectUtils.convertValue(ClassType.Long, getSystemObject(fieldIndex));
		return value != null ? ((Long) value).longValue() : 0;
	}

	@Override
	public boolean isDeleted() throws CommonException {
		Integer fieldIndex = fieldIndexes.get(dbProperties.getDeletedField());
		Object value = ReflectUtils.convertValue(ClassType.Boolean, getSystemObject(fieldIndex.intValue()));
		return value != null ? (Boolean) value : false;
	}

	private Object getSystemObject(int fieldIndex) throws CommonException {
		checkLineIndex();
		return lines.get(lineIndex - 1).get(fieldIndex);
	}

	@Override
	public Date getCreateDate() throws CommonException {
		Integer fieldIndex = fieldIndexes.get(dbProperties.getCreateTimeField());
		return (Date) ReflectUtils.convertValue(ClassType.Date, getSystemObject(fieldIndex));
	}

	@Override
	public Date getUpdateDate() throws CommonException {
		Integer fieldIndex = fieldIndexes.get(dbProperties.getUpdateTimeField());
		return (Date) ReflectUtils.convertValue(ClassType.Date, getSystemObject(fieldIndex));
	}

	@Override
	public boolean isUseCache() {
		return useCache;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}