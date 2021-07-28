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
package io.github.summercattle.commons.db.handle.impl;

import java.math.BigDecimal;
import java.sql.Connection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.constants.DataConstants;
import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.constants.DatabaseType;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.dialect.DialectFactory;
import io.github.summercattle.commons.db.handle.DbMetaModel;
import io.github.summercattle.commons.db.handle.DbStruct;
import io.github.summercattle.commons.db.handle.DbTransaction;
import io.github.summercattle.commons.db.handle.SimpleDalContext;
import io.github.summercattle.commons.db.meta.FieldMeta;
import io.github.summercattle.commons.db.meta.FieldMetaMode;
import io.github.summercattle.commons.db.meta.FixedFieldMeta;
import io.github.summercattle.commons.db.meta.IndexMeta;
import io.github.summercattle.commons.db.meta.ReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.db.struct.TableFieldStruct;
import io.github.summercattle.commons.db.struct.TableIndexStruct;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.NumberUtils;
import io.github.summercattle.commons.utils.cache.Cache;
import io.github.summercattle.commons.utils.cache.CacheManager;
import io.github.summercattle.commons.utils.spring.SpringContext;

public class DbStructImpl implements DbStruct {

	private static final Logger logger = LoggerFactory.getLogger(DbStructImpl.class);

	@Inject
	private DbTransaction dbTransaction;

	@Inject
	private DbMetaModel dbMetaModel;

	@Inject
	private CacheManager cacheManager;

	@Override
	public TableObjectStruct getTableStruct(String tableName) throws CommonException {
		TableMeta tableMeta = dbMetaModel.getTable(tableName);
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		return getTableStruct(dbProperties, tableMeta);
	}

	@Override
	public TableObjectStruct getTableStruct(DbProperties dbProperties, TableMeta tableMeta) throws CommonException {
		String tableName = tableMeta.getName();
		Cache cache = cacheManager.getCache(DataConstants.CAFFEINE_TABLE_STRUCT);
		TableObjectStruct tableObjectStruct = (TableObjectStruct) cache.get(tableName.toLowerCase());
		if (null == tableObjectStruct) {
			if (!existTable(tableName)) {
				throw new CommonException("数据表'" + tableMeta.getName() + "'不存在");
			}
			else {
				TableObjectStruct tableStruct = dbTransaction.doSimpleDal(context -> {
					TableObjectStruct lTableStruct = context.getDialect().getTableStruct(context.getConnection(), tableMeta.getName());
					//检查表的字段
					FieldMeta[] fieldMetas = tableMeta.getFields();
					for (FieldMeta fieldMeta : fieldMetas) {
						DataType dataType;
						if (FieldMetaMode.Fixed == fieldMeta.getMode()) {
							dataType = ((FixedFieldMeta) fieldMeta).getType();
						}
						else if (FieldMetaMode.Reference == fieldMeta.getMode()) {
							TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) fieldMeta).getReferenceTableName());
							if (referenceTable.isPrimaryKeyUseNumber()) {
								dataType = DataType.Number;
							}
							else {
								dataType = context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String;
							}
						}
						else {
							throw new CommonException(
									"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
						}
						if ((dataType == DataType.NString || dataType == DataType.NClob) && !context.getDialect().supportsUnicodeStringType()) {
							dataType = dataType == DataType.NString ? DataType.String : DataType.Clob;
						}
						TableFieldStruct fieldStructure = (TableFieldStruct) lTableStruct.getField(fieldMeta.getName());
						if (fieldStructure != null) {
							checkTableColumn(context, tableMeta.getName(), fieldStructure, dataType);
						}
						else {
							throw new CommonException("数据表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'不存在");
						}
					}
					TableFieldStruct createDateFieldStructure = (TableFieldStruct) lTableStruct.getField(dbProperties.getCreateTimeField());
					if (createDateFieldStructure != null) {
						checkTableColumn(context, tableMeta.getName(), createDateFieldStructure, DataType.Timestamp);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的创建时间字段" + dbProperties.getCreateTimeField());
					}
					TableFieldStruct modifyDateFieldStructure = (TableFieldStruct) lTableStruct.getField(dbProperties.getUpdateTimeField());
					if (modifyDateFieldStructure != null) {
						checkTableColumn(context, tableMeta.getName(), modifyDateFieldStructure, DataType.Timestamp);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的修改时间字段" + dbProperties.getUpdateTimeField());
					}
					TableFieldStruct versionFieldStructure = (TableFieldStruct) lTableStruct.getField(dbProperties.getVersionField());
					if (versionFieldStructure != null) {
						checkTableColumn(context, tableMeta.getName(), versionFieldStructure, DataType.Number);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的版本字段" + dbProperties.getVersionField());
					}
					TableFieldStruct deletedFieldStructure = (TableFieldStruct) lTableStruct.getField(dbProperties.getDeletedField());
					if (deletedFieldStructure != null) {
						checkTableColumn(context, tableMeta.getName(), deletedFieldStructure, DataType.Boolean);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的标识删除字段" + dbProperties.getDeletedField());
					}
					TableFieldStruct primaryFieldStructure = (TableFieldStruct) lTableStruct.getField(dbProperties.getPrimaryField());
					if (primaryFieldStructure != null) {
						if (tableMeta.isPrimaryKeyUseNumber()) {
							checkTableColumn(context, tableMeta.getName(), primaryFieldStructure, DataType.Number);
						}
						else {
							checkTableColumn(context, tableMeta.getName(), primaryFieldStructure,
									context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String);
						}
					}
					else {
						throw new CommonException("表'" + tableMeta.getName() + "'的主键字段" + dbProperties.getPrimaryField() + "不存在");
					}
					return lTableStruct;
				});
				cache.put(tableName.toLowerCase(), tableStruct);
				tableObjectStruct = (TableObjectStruct) cache.get(tableName.toLowerCase());
			}
		}
		return tableObjectStruct;
	}

	@Override
	public void checkTablesAndIndexes(DbProperties dbProperties, Connection conn) throws CommonException {
		Dialect dialect = DialectFactory.getDialect(conn);
		SimpleDalContext context = new SimpleDalContextImpl(dialect, conn);
		logger.info("检查数据表开始");
		TableMeta[] tableMetas = dbMetaModel.getTables();
		for (TableMeta tableMeta : tableMetas) {
			logger.info("检查数据表名:" + tableMeta.getName() + (StringUtils.isNotBlank(tableMeta.getComment()) ? "(" + tableMeta.getComment() + ")" : ""));
			if (!context.getDialect().existTable(context.getConnection(), tableMeta.getName())) {
				createTablesAndIndexes(dbProperties, context, tableMeta);
			}
			else {
				TableObjectStruct tableStructure = context.getDialect().getTableStruct(context.getConnection(), tableMeta.getName());
				//检查表信息
				if (!io.github.summercattle.commons.utils.auxiliary.StringUtils.equals(tableMeta.getComment(), tableStructure.getComment())) {
					if (context.getDialect().getType() == DatabaseType.MySQL) {
						String sql = "alter table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " comment='"
								+ (StringUtils.isNotBlank(tableMeta.getComment()) ? tableMeta.getComment() : "") + "'";
						context.execute(sql);
					}
					else {
						String sql = "comment on table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " is '"
								+ (StringUtils.isNotBlank(tableMeta.getComment()) ? tableMeta.getComment() : "") + "'";
						context.execute(sql);
					}
				}
				//检查表的字段
				FieldMeta[] fieldMetas = tableMeta.getFields();
				for (FieldMeta fieldMeta : fieldMetas) {
					DataType dataType;
					int length;
					int precision = 0;
					String defaultValue = "";
					if (FieldMetaMode.Fixed == fieldMeta.getMode()) {
						dataType = ((FixedFieldMeta) fieldMeta).getType();
						length = ((FixedFieldMeta) fieldMeta).getLength();
						precision = ((FixedFieldMeta) fieldMeta).getPrecision();
						defaultValue = ((FixedFieldMeta) fieldMeta).getDefault();
					}
					else if (FieldMetaMode.Reference == fieldMeta.getMode()) {
						TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) fieldMeta).getReferenceTableName());
						if (referenceTable.isPrimaryKeyUseNumber()) {
							dataType = DataType.Number;
							length = 10;
						}
						else {
							dataType = context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String;
							length = 50;
						}
					}
					else {
						throw new CommonException(
								"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
					}
					if ((dataType == DataType.NString || dataType == DataType.NClob) && !context.getDialect().supportsUnicodeStringType()) {
						dataType = dataType == DataType.NString ? DataType.String : DataType.Clob;
					}
					TableFieldStruct fieldStructure = (TableFieldStruct) tableStructure.getField(fieldMeta.getName());
					if (fieldStructure != null) {
						processTableColumn(context, tableMeta.getName(), fieldStructure, dataType, length, precision, fieldMeta.allowNull(),
								defaultValue, fieldMeta.getComment());
					}
					else {
						String sql = "alter table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " "
								+ context.getDialect().getAddColumnString() + " ";
						sql += context.getDialect().getSQLKeyword(fieldMeta.getName()) + " ";
						sql += context.getDialect().getTypeName(dataType, length, precision);
						if (StringUtils.isNotBlank(defaultValue)) {
							sql += " default ";
							if (dataType == DataType.String || dataType == DataType.NString || dataType == DataType.Clob
									|| dataType == DataType.NClob) {
								sql += "'" + defaultValue + "'";
							}
							else {
								sql += defaultValue;
							}
						}
						if (!fieldMeta.allowNull()) {
							sql += " not null";
						}
						if (context.getDialect().getType() == DatabaseType.MySQL && StringUtils.isNotBlank(fieldMeta.getComment())) {
							sql += " comment '" + fieldMeta.getComment() + "'";
						}
						context.execute(sql);
						if (StringUtils.isNotBlank(fieldMeta.getComment()) && (context.getDialect().getType() == DatabaseType.H2
								|| context.getDialect().getType() == DatabaseType.Oracle || context.getDialect().getType() == DatabaseType.DB2)) {
							sql = "comment on column " + context.getDialect().getSQLKeyword(tableMeta.getName()) + "."
									+ context.getDialect().getSQLKeyword(fieldMeta.getName()) + " is '" + fieldMeta.getComment() + "'";
							context.execute(sql);
						}
					}
				}
				TableFieldStruct createDateFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getCreateTimeField());
				if (createDateFieldStructure != null) {
					processTableColumn(context, tableMeta.getName(), createDateFieldStructure, DataType.Timestamp, 0, 0, true, null, null);
				}
				else {
					addTableColumn(context, tableMeta.getName(), dbProperties.getCreateTimeField(),
							context.getDialect().getTypeName(DataType.Timestamp, 0, 0), null);
				}
				TableFieldStruct modifyDateFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getUpdateTimeField());
				if (modifyDateFieldStructure != null) {
					processTableColumn(context, tableMeta.getName(), modifyDateFieldStructure, DataType.Timestamp, 0, 0, true, null, null);
				}
				else {
					addTableColumn(context, tableMeta.getName(), dbProperties.getUpdateTimeField(),
							context.getDialect().getTypeName(DataType.Timestamp, 0, 0), null);
				}
				TableFieldStruct versionFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getVersionField());
				if (versionFieldStructure != null) {
					processTableColumn(context, tableMeta.getName(), versionFieldStructure, DataType.Number, 10, 0, true, "0", null);
				}
				else {
					addTableColumn(context, tableMeta.getName(), dbProperties.getVersionField(),
							context.getDialect().getTypeName(DataType.Number, 10, 0), "0");
				}
				TableFieldStruct deletedFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getDeletedField());
				if (deletedFieldStructure != null) {
					processTableColumn(context, tableMeta.getName(), deletedFieldStructure, DataType.Boolean, 0, 0, true, "0", null);
				}
				else {
					addTableColumn(context, tableMeta.getName(), dbProperties.getDeletedField(),
							context.getDialect().getTypeName(DataType.Number, 1, 0), "0");
				}
				TableFieldStruct primaryFieldStructure = (TableFieldStruct) tableStructure.getField(dbProperties.getPrimaryField());
				if (primaryFieldStructure != null) {
					if (tableMeta.isPrimaryKeyUseNumber()) {
						processTableColumn(context, tableMeta.getName(), primaryFieldStructure, DataType.Number, 10, 0, false, null, null);
					}
					else {
						processTableColumn(context, tableMeta.getName(), primaryFieldStructure,
								context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String, 50, 0, false, null, null);
					}
				}
				else {
					throw new CommonException("表" + tableMeta.getName() + "的主键字段" + dbProperties.getPrimaryField() + "不存在");
				}
				//检查表的主键
				if (tableStructure.getPrimaryKey() != null) {
					if (!tableStructure.getPrimaryKey().getFields().equalsIgnoreCase(dbProperties.getPrimaryField())) {
						String sql = "alter table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " "
								+ context.getDialect().getDropPrimaryKeyConstraintString(tableMeta.getPrimaryKeyName());
						context.execute(sql);
						createTablePrimaryKey(dbProperties, context, tableMeta);
					}
				}
				else {
					createTablePrimaryKey(dbProperties, context, tableMeta);
				}
				//检查表的索引
				IndexMeta[] indexMetas = tableMeta.getIndexes();
				for (IndexMeta indexMeta : indexMetas) {
					TableIndexStruct indexStructure = tableStructure.getIndex(indexMeta.getName());
					String metaIndexFields = "";
					if (indexStructure != null) {
						String[] fields = indexMeta.getFields().split(",");
						for (int t = 0; t < fields.length; t++) {
							if (t > 0) {
								metaIndexFields += ",";
							}
							String[] indexFields = fields[t].split(":");
							metaIndexFields += tableMeta.getField(indexFields[0]).getName();
							if (indexFields.length == 2) {
								metaIndexFields += ":" + indexFields[1];
							}
						}
						if (!metaIndexFields.equalsIgnoreCase(indexStructure.getFields()) || indexMeta.isUnique() != indexStructure.isUnique()) {
							String sql = context.getDialect().getDropIndexString(tableMeta.getName(), indexMeta.getName());
							context.execute(sql);
							createTableIndex(context, tableMeta, indexMeta);
						}
					}
					else {
						createTableIndex(context, tableMeta, indexMeta);
					}
				}
			}
		}
		//在数据库不支持序列情况下,创建自定义序列表
		if (!context.getDialect().supportsSequences()
				&& !context.getDialect().existTable(context.getConnection(), DataConstants.SEQUENCE_TABLE_NAME)) {
			createCustomSequenceTable(context);
		}
		logger.info("检查数据表结束");
	}

	private void checkTableColumn(SimpleDalContext context, String tableName, TableFieldStruct fieldStructure, DataType dataType)
			throws CommonException {
		if (null == fieldStructure.getType()) {
			throw new CommonException(
					"表'" + tableName + "'的字段'" + fieldStructure.getName() + "'的数据类型'" + fieldStructure.getTypeName() + "'不属于系统支持的数据类型");
		}
		if (fieldStructure.getType() != dataType) {
			boolean ignore = false;
			if ((fieldStructure.getType() == DataType.String && dataType == DataType.NString)
					|| (fieldStructure.getType() == DataType.NString && dataType == DataType.String)) {
				ignore = true;
			}
			if (context.getDialect().getType() == DatabaseType.MySQL && ((fieldStructure.getType() == DataType.Clob && dataType == DataType.NClob)
					|| (fieldStructure.getType() == DataType.NClob && dataType == DataType.Clob))) {
				ignore = true;
			}
			if (context.getDialect().getType() == DatabaseType.MySQL && ((fieldStructure.getType() == DataType.NString && dataType == DataType.NClob)
					|| (fieldStructure.getType() == DataType.String && dataType == DataType.Clob))) {
				ignore = true;
			}
			if (dataType == DataType.Boolean && fieldStructure.getType() == DataType.Number && fieldStructure.getSize() == 1
					&& fieldStructure.getDecimalDigits() == 0) {
				ignore = true;
			}
			if (!ignore) {
				throw new CommonException("表'" + tableName + "'的字段'" + fieldStructure.getName() + "'的数据类型'" + fieldStructure.getType().toString()
						+ "'与数据定义中的数据类型'" + dataType.toString() + "'不一致");
			}
		}
	}

	private void processTableColumn(SimpleDalContext context, String tableName, TableFieldStruct fieldStructure, DataType dataType, int length,
			int precision, boolean allowNull, String defaultValue, String comment) throws CommonException {
		if (null == fieldStructure.getType()) {
			throw new CommonException("表" + tableName + "的字段" + fieldStructure.getName() + "的数据类型" + fieldStructure.getTypeName() + "不属于系统支持的数据类型");
		}
		boolean dataTypeChange = false;
		if (fieldStructure.getType() != dataType) {
			dataTypeChange = true;
			boolean ignore = false;
			if ((fieldStructure.getType() == DataType.String && dataType == DataType.NString)
					|| (fieldStructure.getType() == DataType.NString && dataType == DataType.String)) {
				ignore = true;
			}
			if (context.getDialect().getType() == DatabaseType.MySQL && ((fieldStructure.getType() == DataType.Clob && dataType == DataType.NClob)
					|| (fieldStructure.getType() == DataType.NClob && dataType == DataType.Clob))) {
				ignore = true;
			}
			if (context.getDialect().getType() == DatabaseType.MySQL && ((fieldStructure.getType() == DataType.NString && dataType == DataType.NClob)
					|| (fieldStructure.getType() == DataType.String && dataType == DataType.Clob))) {
				ignore = true;
			}
			if (dataType == DataType.Boolean && fieldStructure.getType() == DataType.Number && fieldStructure.getSize() == 1
					&& fieldStructure.getDecimalDigits() == 0) {
				ignore = true;
				dataTypeChange = false;
			}
			if (!ignore) {
				throw new CommonException("表" + tableName + "的字段" + fieldStructure.getName() + "的数据类型" + fieldStructure.getType().toString()
						+ "与数据定义中的数据类型" + dataType.toString() + "不一致");
			}
		}
		else {
			if (dataType == DataType.Number) {
				if (length < fieldStructure.getSize()) {
					throw new CommonException("表" + tableName + "的字段" + fieldStructure.getName() + "长度不能改小");
				}
				if (length > fieldStructure.getSize()) {
					if (context.getDialect().getType() == DatabaseType.DB2) {
						throw new CommonException(
								"表" + tableName + "的字段" + fieldStructure.getName() + "的数据类型" + fieldStructure.getType().toString() + "不能调整");
					}
					dataTypeChange = true;
				}
				if (precision != fieldStructure.getDecimalDigits()) {
					if (context.getDialect().getType() == DatabaseType.DB2) {
						throw new CommonException(
								"表" + tableName + "的字段" + fieldStructure.getName() + "的数据类型" + fieldStructure.getType().toString() + "不能调整");
					}
					dataTypeChange = true;
				}
			}
			else if (dataType == DataType.String || dataType == DataType.NString) {
				if (length < fieldStructure.getSize()) {
					throw new CommonException("表" + tableName + "的字段" + fieldStructure.getName() + "长度不能改小");
				}
				if (length > fieldStructure.getSize()) {
					dataTypeChange = true;
				}
			}
		}
		if (dataTypeChange) {
			String sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " " + context.getDialect().getModifyColumnString() + " ";
			sql += context.getDialect().getSQLKeyword(fieldStructure.getName()) + " ";
			if (context.getDialect().getType() == DatabaseType.DB2 || context.getDialect().getType() == DatabaseType.H2) {
				sql += "set data type ";
			}
			sql += context.getDialect().getTypeName(dataType, length, precision);
			if (context.getDialect().getType() == DatabaseType.MySQL) {
				if (!allowNull) {
					sql += " not null";
				}
				else {
					sql += " null";
				}
				if (StringUtils.isNotBlank(defaultValue)) {
					sql += " default ";
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
					sql += defaultValue;
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
				}
				sql += " comment '" + (StringUtils.isNotBlank(comment) ? comment : "") + "'";
			}
			context.execute(sql);
		}
		if (fieldStructure.isNullable() != allowNull) {
			String sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " " + context.getDialect().getModifyColumnString() + " ";
			sql += context.getDialect().getSQLKeyword(fieldStructure.getName()) + " ";
			if (context.getDialect().getType() == DatabaseType.DB2 || context.getDialect().getType() == DatabaseType.H2) {
				if (fieldStructure.isNullable() && !allowNull) {
					sql += "set not null";
				}
				else if (!fieldStructure.isNullable() && allowNull) {
					sql += "drop not null";
				}
			}
			else {
				sql += context.getDialect().getTypeName(dataType, length, precision);
				if (!allowNull) {
					sql += " not null";
				}
				else {
					sql += " null";
				}
				if (context.getDialect().getType() == DatabaseType.MySQL) {
					if (StringUtils.isNotBlank(defaultValue)) {
						sql += " default ";
						if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
								|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
							sql += "'";
						}
						sql += defaultValue;
						if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
								|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
							sql += "'";
						}
					}
					sql += " comment '" + (StringUtils.isNotBlank(comment) ? comment : "") + "'";
				}
			}
			context.execute(sql);
		}
		boolean defaultChange = false;
		if (!io.github.summercattle.commons.utils.auxiliary.StringUtils.equals(fieldStructure.getDefaultValue(), defaultValue)) {
			if (StringUtils.isNotBlank(defaultValue)) {
				if (dataType == DataType.Number) {
					if (StringUtils.isNotBlank(fieldStructure.getDefaultValue())) {
						BigDecimal structureDefault = NumberUtils.toBigDecimal(fieldStructure.getDefaultValue());
						BigDecimal metaDefault = NumberUtils.toBigDecimal(defaultValue);
						if (structureDefault.compareTo(metaDefault) != 0) {
							defaultChange = true;
						}
					}
				}
				else {
					defaultChange = true;
				}
			}
			else {
				defaultChange = true;
			}
		}
		if (defaultChange) {
			String sql;
			if (StringUtils.isNotBlank(defaultValue)) {
				if (context.getDialect().getType() == DatabaseType.MySQL) {
					sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " alter column "
							+ context.getDialect().getSQLKeyword(fieldStructure.getName()) + " set default ";
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
					sql += defaultValue;
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
				}
				else {
					sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " " + context.getDialect().getModifyColumnString() + " "
							+ context.getDialect().getSQLKeyword(fieldStructure.getName());
					if (context.getDialect().getType() == DatabaseType.DB2 || context.getDialect().getType() == DatabaseType.H2) {
						sql += " set";
					}
					sql += " default ";
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
					sql += defaultValue;
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
				}
			}
			else {
				if (context.getDialect().getType() == DatabaseType.MySQL) {
					sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " alter column "
							+ context.getDialect().getSQLKeyword(fieldStructure.getName()) + " drop default";
				}
				else {
					sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " " + context.getDialect().getModifyColumnString() + " "
							+ context.getDialect().getSQLKeyword(fieldStructure.getName())
							+ (context.getDialect().getType() == DatabaseType.DB2 || context.getDialect().getType() == DatabaseType.H2
									? " drop default"
									: " default null");
				}
			}
			context.execute(sql);
		}
		if (!io.github.summercattle.commons.utils.auxiliary.StringUtils.equals(fieldStructure.getComment(), comment)) {
			String sql;
			if (context.getDialect().getType() == DatabaseType.MySQL) {
				sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " " + context.getDialect().getModifyColumnString() + " "
						+ context.getDialect().getSQLKeyword(fieldStructure.getName()) + " ";
				sql += context.getDialect().getTypeName(dataType, length, precision);
				if (!allowNull) {
					sql += " not null";
				}
				else {
					sql += " null";
				}
				if (StringUtils.isNotBlank(defaultValue)) {
					sql += " default ";
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
					sql += defaultValue;
					if (fieldStructure.getType() == DataType.String || fieldStructure.getType() == DataType.NString
							|| fieldStructure.getType() == DataType.Clob || fieldStructure.getType() == DataType.NClob) {
						sql += "'";
					}
				}
				sql += " comment '" + (StringUtils.isNotBlank(comment) ? comment : "") + "'";
			}
			else {
				sql = "comment on column " + context.getDialect().getSQLKeyword(tableName) + "."
						+ context.getDialect().getSQLKeyword(fieldStructure.getName()) + " is '" + (StringUtils.isNotBlank(comment) ? comment : "")
						+ "'";
			}
			context.execute(sql);
		}
	}

	private void createTablesAndIndexes(DbProperties dbProperties, SimpleDalContext context, TableMeta tableMeta) throws CommonException {
		String createSQL = "create table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " (";
		createSQL += context.getDialect().getSQLKeyword(dbProperties.getPrimaryField()) + " " + (tableMeta.isPrimaryKeyUseNumber()
				? context.getDialect().getTypeName(DataType.Number, 10, 0)
				: context.getDialect().getTypeName(context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String, 50, 0))
				+ " not null,";
		FieldMeta[] fieldMetas = tableMeta.getFields();
		for (FieldMeta fieldMeta : fieldMetas) {
			createSQL += context.getDialect().getSQLKeyword(fieldMeta.getName()) + " ";
			DataType dataType;
			int length;
			int precision = 0;
			String defaultValue = "";
			if (FieldMetaMode.Fixed == fieldMeta.getMode()) {
				dataType = ((FixedFieldMeta) fieldMeta).getType();
				length = ((FixedFieldMeta) fieldMeta).getLength();
				precision = ((FixedFieldMeta) fieldMeta).getPrecision();
				defaultValue = ((FixedFieldMeta) fieldMeta).getDefault();
			}
			else if (FieldMetaMode.Reference == fieldMeta.getMode()) {
				TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) fieldMeta).getReferenceTableName());
				if (referenceTable.isPrimaryKeyUseNumber()) {
					dataType = DataType.Number;
					length = 10;
				}
				else {
					dataType = context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String;
					length = 50;
				}
			}
			else {
				throw new CommonException(
						"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
			}
			if ((dataType == DataType.NString || dataType == DataType.NClob) && !context.getDialect().supportsUnicodeStringType()) {
				dataType = dataType == DataType.NString ? DataType.String : DataType.Clob;
			}
			createSQL += context.getDialect().getTypeName(dataType, length, precision);
			if (StringUtils.isNotBlank(defaultValue)) {
				createSQL += " default ";
				if (dataType == DataType.String || dataType == DataType.NString || dataType == DataType.Clob || dataType == DataType.NClob) {
					createSQL += "'" + defaultValue + "'";
				}
				else {
					createSQL += defaultValue;
				}
			}
			if (!fieldMeta.allowNull()) {
				createSQL += " not null";
			}
			if (context.getDialect().getType() == DatabaseType.MySQL && StringUtils.isNotBlank(fieldMeta.getComment())) {
				createSQL += " comment '" + fieldMeta.getComment() + "'";
			}
			createSQL += ",";
		}
		createSQL += context.getDialect().getSQLKeyword(dbProperties.getCreateTimeField()) + " "
				+ context.getDialect().getTypeName(DataType.Timestamp, 0, 0) + ",";
		createSQL += context.getDialect().getSQLKeyword(dbProperties.getUpdateTimeField()) + " "
				+ context.getDialect().getTypeName(DataType.Timestamp, 0, 0) + ",";
		createSQL += context.getDialect().getSQLKeyword(dbProperties.getVersionField()) + " "
				+ context.getDialect().getTypeName(DataType.Number, 10, 0) + " default 0,";
		createSQL += context.getDialect().getSQLKeyword(dbProperties.getDeletedField()) + " "
				+ context.getDialect().getTypeName(DataType.Boolean, 0, 0) + " default 0";
		createSQL += ")";
		if (context.getDialect().getType() == DatabaseType.MySQL && StringUtils.isNotBlank(tableMeta.getComment())) {
			createSQL += " comment='" + tableMeta.getComment() + "'";
		}
		if (StringUtils.isNotBlank(context.getDialect().getTableTypeString())) {
			createSQL += " " + context.getDialect().getTableTypeString();
		}
		context.execute(createSQL);
		createTablePrimaryKey(dbProperties, context, tableMeta);
		IndexMeta[] indexMetas = tableMeta.getIndexes();
		for (IndexMeta indexMeta : indexMetas) {
			createTableIndex(context, tableMeta, indexMeta);
		}
		if (context.getDialect().getType() == DatabaseType.H2 || context.getDialect().getType() == DatabaseType.Oracle
				|| context.getDialect().getType() == DatabaseType.DB2) {
			String commentSQL;
			if (StringUtils.isNotBlank(tableMeta.getComment())) {
				commentSQL = "comment on table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " is '" + tableMeta.getComment() + "'";
				context.execute(commentSQL);
			}
			for (FieldMeta fieldMeta : fieldMetas) {
				if (StringUtils.isNotBlank(fieldMeta.getComment())) {
					commentSQL = "comment on column " + context.getDialect().getSQLKeyword(tableMeta.getName()) + "."
							+ context.getDialect().getSQLKeyword(fieldMeta.getName()) + " is '" + fieldMeta.getComment() + "'";
					context.execute(commentSQL);
				}
			}
		}
	}

	private void addTableColumn(SimpleDalContext context, String tableName, String fieldName, String typeName, String fieldDefault)
			throws CommonException {
		String sql = "alter table " + context.getDialect().getSQLKeyword(tableName) + " " + context.getDialect().getAddColumnString() + " "
				+ context.getDialect().getSQLKeyword(fieldName) + " " + typeName;
		if (StringUtils.isNotBlank(fieldDefault)) {
			sql += " default " + fieldDefault;
		}
		context.execute(sql);
	}

	private void createTablePrimaryKey(DbProperties dbProperties, SimpleDalContext context, TableMeta tableMeta) throws CommonException {
		String createPrimaryKeySQL = "alter table " + context.getDialect().getSQLKeyword(tableMeta.getName()) + " "
				+ context.getDialect().getAddPrimaryKeyConstraintString(tableMeta.getPrimaryKeyName()) + " ("
				+ context.getDialect().getSQLKeyword(dbProperties.getPrimaryField()) + ")";
		context.execute(createPrimaryKeySQL);
	}

	private void createTableIndex(SimpleDalContext context, TableMeta tableMeta, IndexMeta indexMeta) throws CommonException {
		String createIndexSQL = "create " + (indexMeta.isUnique() ? "unique " : "") + "index " + indexMeta.getName() + " on "
				+ context.getDialect().getSQLKeyword(tableMeta.getName()) + " (";
		String[] fields = indexMeta.getFields().split(",");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0) {
				createIndexSQL += ",";
			}
			String[] indexFields = fields[i].split(":");
			FieldMeta field = tableMeta.getField(indexFields[0]);
			DataType fieldType;
			if (FieldMetaMode.Fixed == field.getMode()) {
				fieldType = ((FixedFieldMeta) field).getType();
			}
			else if (FieldMetaMode.Reference == field.getMode()) {
				TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) field).getReferenceTableName());
				if (referenceTable.isPrimaryKeyUseNumber()) {
					fieldType = DataType.Number;
				}
				else {
					fieldType = context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String;
				}
			}
			else {
				throw new CommonException("表'" + tableMeta.getName() + "'的字段'" + field.getName() + "'模式'" + field.getMode().toString() + "'不支持");
			}
			if (context.getDialect().getType() == DatabaseType.DB2
					&& (fieldType == DataType.Clob || fieldType == DataType.NClob || fieldType == DataType.Blob)) {
				throw new CommonException("表'" + tableMeta.getName() + "'的字段'" + field.getName() + "'的数据类型'" + fieldType.toString() + "',不能进行索引");
			}
			createIndexSQL += context.getDialect().getSQLKeyword(field.getName());
			if (indexFields.length == 2) {
				createIndexSQL += " " + indexFields[1];
			}
		}
		createIndexSQL += ")";
		context.execute(createIndexSQL);
	}

	private void createCustomSequenceTable(SimpleDalContext context) throws CommonException {
		String createSQL = "create table " + DataConstants.SEQUENCE_TABLE_NAME + " (";
		createSQL += DataConstants.SEQUENCE_FIELD_NAME + " "
				+ context.getDialect().getTypeName(context.getDialect().supportsUnicodeStringType() ? DataType.NString : DataType.String, 50, 0)
				+ " not null,";
		createSQL += DataConstants.SEQUENCE_FIELD_VALUE + " ";
		createSQL += context.getDialect().getTypeName(DataType.Number, 20, 0);
		createSQL += " default 0";
		createSQL += ")";
		if (StringUtils.isNotBlank(context.getDialect().getTableTypeString())) {
			createSQL += " " + context.getDialect().getTableTypeString();
		}
		context.execute(createSQL);
		String createPrimaryKeySQL = "alter table " + DataConstants.SEQUENCE_TABLE_NAME + " "
				+ context.getDialect().getAddPrimaryKeyConstraintString("PK_" + DataConstants.SEQUENCE_TABLE_NAME) + " ("
				+ DataConstants.SEQUENCE_FIELD_NAME + ")";
		context.execute(createPrimaryKeySQL);
	}

	@Override
	public boolean existTable(String tableName) throws CommonException {
		if (StringUtils.isBlank(tableName)) {
			throw new CommonException("表名为空");
		}
		return dbTransaction.doSimpleDal(context -> context.getDialect().existTable(context.getConnection(), tableName));
	}

	@Override
	public boolean existView(String viewName) throws CommonException {
		if (StringUtils.isBlank(viewName)) {
			throw new CommonException("视图名为空");
		}
		return dbTransaction.doSimpleDal(context -> context.getDialect().existView(context.getConnection(), viewName));
	}

	@Override
	public ViewObjectStruct getViewStruct(String viewName) throws CommonException {
		if (StringUtils.isBlank(viewName)) {
			throw new CommonException("视图名为空");
		}
		Cache cache = cacheManager.getCache(DataConstants.CAFFEINE_VIEW_STRUCT);
		ViewObjectStruct viewObjectStruct = (ViewObjectStruct) cache.get(viewName.toLowerCase());
		if (null == viewObjectStruct) {
			if (!existView(viewName)) {
				throw new CommonException("视图'" + viewName + "'不存在");
			}
			else {
				ViewObjectStruct viewStruct = dbTransaction.doSimpleDal(context -> {
					return context.getDialect().getViewStruct(context.getConnection(), viewName);
				});
				cache.put(viewName.toLowerCase(), viewStruct);
				viewObjectStruct = (ViewObjectStruct) cache.get(viewName.toLowerCase());
			}
		}
		return viewObjectStruct;
	}
}