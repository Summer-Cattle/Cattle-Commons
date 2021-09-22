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
import java.sql.Types;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.constants.DataConstants;
import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.handle.DbMetaModel;
import io.github.summercattle.commons.db.handle.DbStruct;
import io.github.summercattle.commons.db.handle.DbTransaction;
import io.github.summercattle.commons.db.handle.SimpleDalContext;
import io.github.summercattle.commons.db.meta.FieldMeta;
import io.github.summercattle.commons.db.meta.FieldMetaMode;
import io.github.summercattle.commons.db.meta.FixedFieldMeta;
import io.github.summercattle.commons.db.meta.IndexFieldMeta;
import io.github.summercattle.commons.db.meta.IndexMeta;
import io.github.summercattle.commons.db.meta.ReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.db.struct.TableFieldStruct;
import io.github.summercattle.commons.db.struct.TableIndexStruct;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.db.utils.JdbcUtils;
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

	private static final String PRIMARY_FIELD_COMMENT = "主键";

	private static final String CREATE_TIME_FIELD_COMMENT = "创建时间";

	private static final String UPDATE_TIME_FIELD_COMMENT = "最后修改时间";

	private static final String VERSION_FIELD_COMMENT = "版本";

	private static final String DELETED_FIELD_COMMENT = "删除标识";

	@Override
	public TableObjectStruct getTableStruct(String name) throws CommonException {
		TableMeta tableMeta = dbMetaModel.getTable(name);
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
				TableObjectStruct tableStruct = dbTransaction.doSimpleDal(ctx -> {
					if (!ctx.getDialect().getStructHandler().supportsTable()) {
						throw new CommonException("不支持数据表结构的查询");
					}
					TableObjectStruct lTableStruct = ctx.getDialect().getStructHandler().getTable(ctx.getConnection(), tableMeta.getName());
					//检查表的字段
					FieldMeta[] fieldMetas = tableMeta.getFields();
					for (FieldMeta fieldMeta : fieldMetas) {
						int length;
						int scale = 0;
						DataType dataType;
						if (FieldMetaMode.Fixed == fieldMeta.getMode()) {
							dataType = ((FixedFieldMeta) fieldMeta).getType();
							length = ((FixedFieldMeta) fieldMeta).getLength();
							scale = ((FixedFieldMeta) fieldMeta).getScale();
						}
						else if (FieldMetaMode.Reference == fieldMeta.getMode()) {
							TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) fieldMeta).getReferenceTableName());
							if (referenceTable.isPrimaryKeyUseNumber()) {
								dataType = DataType.Number;
								length = 10;
							}
							else {
								dataType = DataType.NString;
								length = 50;
							}
						}
						else {
							throw new CommonException(
									"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
						}
						TableFieldStruct fieldStruct = (TableFieldStruct) lTableStruct.getField(fieldMeta.getName());
						if (fieldStruct != null) {
							checkTableColumn(ctx, tableMeta.getName(), fieldStruct, dataType, length, scale);
						}
						else {
							throw new CommonException("数据表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'不存在");
						}
					}
					TableFieldStruct createDateFieldStruct = (TableFieldStruct) lTableStruct.getField(dbProperties.getCreateTimeField());
					if (createDateFieldStruct != null) {
						checkTableColumn(ctx, tableMeta.getName(), createDateFieldStruct, DataType.Timestamp, 0, 0);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的创建时间字段" + dbProperties.getCreateTimeField());
					}
					TableFieldStruct modifyDateFieldStruct = (TableFieldStruct) lTableStruct.getField(dbProperties.getUpdateTimeField());
					if (modifyDateFieldStruct != null) {
						checkTableColumn(ctx, tableMeta.getName(), modifyDateFieldStruct, DataType.Timestamp, 0, 0);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的修改时间字段" + dbProperties.getUpdateTimeField());
					}
					TableFieldStruct versionFieldStruct = (TableFieldStruct) lTableStruct.getField(dbProperties.getVersionField());
					if (versionFieldStruct != null) {
						checkTableColumn(ctx, tableMeta.getName(), versionFieldStruct, DataType.Number, 10, 0);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的版本字段" + dbProperties.getVersionField());
					}
					TableFieldStruct deletedFieldStruct = (TableFieldStruct) lTableStruct.getField(dbProperties.getDeletedField());
					if (deletedFieldStruct != null) {
						checkTableColumn(ctx, tableMeta.getName(), deletedFieldStruct, DataType.Boolean, 0, 0);
					}
					else {
						throw new CommonException("没有找到表'" + tableName + "'的标识删除字段" + dbProperties.getDeletedField());
					}
					TableFieldStruct primaryFieldStruct = (TableFieldStruct) lTableStruct.getField(dbProperties.getPrimaryField());
					if (primaryFieldStruct != null) {
						if (tableMeta.isPrimaryKeyUseNumber()) {
							checkTableColumn(ctx, tableMeta.getName(), primaryFieldStruct, DataType.Number, 10, 0);
						}
						else {
							checkTableColumn(ctx, tableMeta.getName(), primaryFieldStruct, DataType.NString, 50, 0);
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
	public void check(DbProperties dbProperties) throws CommonException {
		Dialect dialect = SpringContext.getBean(Dialect.class);
		if (!dialect.getStructHandler().supportsTable()) {
			return;
		}
		logger.info("检查数据表开始");
		TableMeta[] tableMetas = dbMetaModel.getTables();
		for (TableMeta tableMeta : tableMetas) {
			dbTransaction.doSimpleDal(ctx -> {
				logger.info(
						"检查数据表名:" + tableMeta.getName() + (StringUtils.isNotBlank(tableMeta.getComment()) ? "(" + tableMeta.getComment() + ")" : ""));
				if (!ctx.getDialect().getStructHandler().existTable(ctx.getConnection(), tableMeta.getName())) {
					create(dbProperties, ctx, tableMeta);
				}
				else {
					TableObjectStruct tableStruct = ctx.getDialect().getStructHandler().getTable(ctx.getConnection(), tableMeta.getName());
					checkModify(dbProperties, ctx, tableMeta, tableStruct);
				}
				return null;
			});
		}
		if (!dialect.supportsSequences()) {
			dbTransaction.doSimpleDal(ctx -> {
				if (!ctx.getDialect().getStructHandler().existTable(ctx.getConnection(), DataConstants.SEQUENCE_TABLE_NAME)) {
					createCustomSequenceTable(ctx);
				}
				return null;
			});
		}
		logger.info("检查数据表结束");
	}

	private void checkModify(DbProperties dbProperties, SimpleDalContext ctx, TableMeta tableMeta, TableObjectStruct tableStruct)
			throws CommonException {
		//检查表信息
		if (!io.github.summercattle.commons.utils.auxiliary.StringUtils.equals(tableMeta.getComment(), tableStruct.getComment())) {
			String tableComment = ctx.getDialect().getTableComment(tableMeta.getComment());
			if (StringUtils.isNotBlank(tableComment)) {
				String sql = ctx.getDialect().getAlterTableString(tableMeta.getName()) + tableComment;
				ctx.execute(sql);
			}
			if (ctx.getDialect().supportsCommentOn()) {
				commentTable(ctx, tableMeta.getName(), tableMeta.getComment());
			}
		}
		//检查表的字段
		FieldMeta[] fieldMetas = tableMeta.getFields();
		for (FieldMeta fieldMeta : fieldMetas) {
			DataType dataType;
			int length;
			int scale = 0;
			String defaultValue = "";
			if (FieldMetaMode.Fixed == fieldMeta.getMode()) {
				dataType = ((FixedFieldMeta) fieldMeta).getType();
				length = ((FixedFieldMeta) fieldMeta).getLength();
				scale = ((FixedFieldMeta) fieldMeta).getScale();
				defaultValue = ((FixedFieldMeta) fieldMeta).getDefault();
			}
			else if (FieldMetaMode.Reference == fieldMeta.getMode()) {
				TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) fieldMeta).getReferenceTableName());
				if (referenceTable.isPrimaryKeyUseNumber()) {
					dataType = DataType.Number;
					length = 10;
				}
				else {
					dataType = DataType.NString;
					length = 50;
				}
			}
			else {
				throw new CommonException(
						"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
			}
			TableFieldStruct fieldStruct = (TableFieldStruct) tableStruct.getField(fieldMeta.getName());
			if (fieldStruct != null) {
				processTableColumn(ctx, tableMeta.getName(), fieldStruct, dataType, length, scale, fieldMeta.allowNull(), defaultValue,
						fieldMeta.getComment());
			}
			else {
				addTableColumn(ctx, tableMeta.getName(), fieldMeta.getName(), dataType, length, scale, fieldMeta.allowNull(), defaultValue,
						fieldMeta.getComment());
			}
		}
		TableFieldStruct createDateFieldStruct = (TableFieldStruct) tableStruct.getField(dbProperties.getCreateTimeField());
		if (createDateFieldStruct != null) {
			processTableColumn(ctx, tableMeta.getName(), createDateFieldStruct, DataType.Timestamp, 0, 0, true, null, CREATE_TIME_FIELD_COMMENT);
		}
		else {
			addTableColumn(ctx, tableMeta.getName(), dbProperties.getCreateTimeField(), DataType.Timestamp, 0, 0, true, null,
					CREATE_TIME_FIELD_COMMENT);
		}
		TableFieldStruct modifyDateFieldStruct = (TableFieldStruct) tableStruct.getField(dbProperties.getUpdateTimeField());
		if (modifyDateFieldStruct != null) {
			processTableColumn(ctx, tableMeta.getName(), modifyDateFieldStruct, DataType.Timestamp, 0, 0, true, null, UPDATE_TIME_FIELD_COMMENT);
		}
		else {
			addTableColumn(ctx, tableMeta.getName(), dbProperties.getUpdateTimeField(), DataType.Timestamp, 0, 0, true, null,
					UPDATE_TIME_FIELD_COMMENT);
		}
		TableFieldStruct versionFieldStruct = (TableFieldStruct) tableStruct.getField(dbProperties.getVersionField());
		if (versionFieldStruct != null) {
			processTableColumn(ctx, tableMeta.getName(), versionFieldStruct, DataType.Number, 10, 0, false, "0", VERSION_FIELD_COMMENT);
		}
		else {
			addTableColumn(ctx, tableMeta.getName(), dbProperties.getVersionField(), DataType.Number, 10, 0, false, "0", VERSION_FIELD_COMMENT);
		}
		TableFieldStruct deletedFieldStruct = (TableFieldStruct) tableStruct.getField(dbProperties.getDeletedField());
		if (deletedFieldStruct != null) {
			processTableColumn(ctx, tableMeta.getName(), deletedFieldStruct, DataType.Boolean, 0, 0, false, "0", DELETED_FIELD_COMMENT);
		}
		else {
			addTableColumn(ctx, tableMeta.getName(), dbProperties.getDeletedField(), DataType.Boolean, 0, 0, false, "0", DELETED_FIELD_COMMENT);
		}
		TableFieldStruct primaryFieldStruct = (TableFieldStruct) tableStruct.getField(dbProperties.getPrimaryField());
		if (primaryFieldStruct != null) {
			if (tableMeta.isPrimaryKeyUseNumber()) {
				processTableColumn(ctx, tableMeta.getName(), primaryFieldStruct, DataType.Number, 10, 0, false, null, PRIMARY_FIELD_COMMENT);
			}
			else {
				processTableColumn(ctx, tableMeta.getName(), primaryFieldStruct, DataType.NString, 50, 0, false, null, PRIMARY_FIELD_COMMENT);
			}
		}
		else {
			throw new CommonException("表" + tableMeta.getName() + "的主键字段" + dbProperties.getPrimaryField() + "不存在");
		}
		//检查表的主键
		if (tableStruct.getPrimaryKey() != null) {
			if (!tableStruct.getPrimaryKey().getFields().equalsIgnoreCase(dbProperties.getPrimaryField())) {
				String sql = ctx.getDialect().getAlterTableString(tableMeta.getName())
						+ ctx.getDialect().getDropPrimaryKeyString(tableStruct.getPrimaryKey().getName());
				ctx.execute(sql);
				createTablePrimaryKey(dbProperties, ctx, tableMeta);
			}
		}
		else {
			createTablePrimaryKey(dbProperties, ctx, tableMeta);
		}
		//检查表的索引
		IndexMeta[] indexMetas = tableMeta.getIndexes();
		for (IndexMeta indexMeta : indexMetas) {
			String metaIndexFields = "";
			IndexFieldMeta[] fields = indexMeta.getFields();
			for (int t = 0; t < fields.length; t++) {
				if (t > 0) {
					metaIndexFields += ",";
				}
				metaIndexFields += tableMeta.getField(fields[t].getField()).getName();
				metaIndexFields += ":" + fields[t].getOrder();
			}
			String idxName = DataConstants.INDEX_KEY_PREFIX + io.github.summercattle.commons.utils.auxiliary.StringUtils.getHashName("Table'"
					+ tableMeta.getName() + "'Unique'" + BooleanUtils.toStringTrueFalse(indexMeta.isUnique()) + "'Columns'" + metaIndexFields + "'");
			TableIndexStruct indexStruct = tableStruct.getIndex(idxName);
			if (indexStruct != null) {
				if (!metaIndexFields.equalsIgnoreCase(indexStruct.getFields()) || indexMeta.isUnique() != indexStruct.isUnique()) {
					String sql = ctx.getDialect().getDropIndexCommand(tableMeta.getName(), idxName);
					ctx.execute(sql);
					createTableIndex(ctx, tableMeta, indexMeta);
				}
			}
			else {
				TableIndexStruct[] indexes = tableStruct.getIndexes();
				for (TableIndexStruct index : indexes) {
					if (index.isUnique() == indexMeta.isUnique() && metaIndexFields.equalsIgnoreCase(index.getFields())) {
						String sql = ctx.getDialect().getDropIndexCommand(tableMeta.getName(), index.getName());
						ctx.execute(sql);
						break;
					}
				}
				createTableIndex(ctx, tableMeta, indexMeta);
			}
		}
	}

	private void checkTableColumn(SimpleDalContext ctx, String tableName, TableFieldStruct fieldStruct, DataType dataType, int length, int scale)
			throws CommonException {
		String typeName = ctx.getDialect().getTypeSimpleName(dataType, length, scale);
		if (!typeName.equalsIgnoreCase(fieldStruct.getTypeName())) {
			boolean ignore = false;
			if (dataType == DataType.Boolean && JdbcUtils.isNumeric(fieldStruct.getJdbcType()) && fieldStruct.getLength() == 1
					&& fieldStruct.getScale() == 0) {
				ignore = true;
			}
			if (!ignore) {
				throw new CommonException("表'" + tableName + "'的字段'" + fieldStruct.getName() + "'的数据类型'" + fieldStruct.getTypeName() + "'与数据定义中的数据类型'"
						+ dataType.toString() + "'不一致");
			}
		}
	}

	private void addTableColumn(SimpleDalContext ctx, String tableName, String fieldName, DataType dataType, int length, int scale, boolean allowNull,
			String defaultValue, String comment) throws CommonException {
		StringBuffer buf = new StringBuffer(ctx.getDialect().getAlterTableString(tableName) + " " + ctx.getDialect().getAddColumnString() + " ");
		buf.append(ctx.getDialect().quote(fieldName) + " ").append(ctx.getDialect().getTypeName(dataType, length, scale));
		if (StringUtils.isNotBlank(defaultValue)) {
			buf.append(" default ");
			if (dataType == DataType.String || dataType == DataType.NString || dataType == DataType.LongString || dataType == DataType.Clob
					|| dataType == DataType.NClob) {
				buf.append("'" + defaultValue + "'");
			}
			else {
				buf.append(defaultValue);
			}
		}
		if (allowNull) {
			buf.append(ctx.getDialect().getNullColumnString());
		}
		else {
			buf.append(" not null");
		}
		if (StringUtils.isNotBlank(comment)) {
			buf.append(ctx.getDialect().getColumnComment(comment));
		}
		buf.append(ctx.getDialect().getAddColumnSuffixString());
		ctx.execute(buf.toString());
		if (ctx.getDialect().supportsCommentOn()) {
			commentColumn(ctx, tableName, fieldName, comment);
		}
	}

	private void processTableColumn(SimpleDalContext ctx, String tableName, TableFieldStruct fieldStruct, DataType dataType, int length, int scale,
			boolean allowNull, String defaultValue, String comment) throws CommonException {
		if (fieldStruct.getJdbcType() == Types.OTHER) {
			throw new CommonException("表" + tableName + "的字段" + fieldStruct.getName() + "的数据类型" + fieldStruct.getTypeName() + "不属于系统支持的数据类型");
		}
		boolean dataTypeChange = false;
		String typeName = ctx.getDialect().getTypeSimpleName(dataType, length, scale);
		if (!typeName.equalsIgnoreCase(fieldStruct.getTypeName())) {
			dataTypeChange = true;
			boolean ignore = false;
			if (dataType == DataType.Boolean && JdbcUtils.isNumeric(fieldStruct.getJdbcType()) && fieldStruct.getLength() == 1
					&& fieldStruct.getScale() == 0) {
				ignore = true;
				dataTypeChange = false;
			}
			if (!ignore) {
				throw new CommonException("表" + tableName + "的字段" + fieldStruct.getName() + "的数据类型" + fieldStruct.getTypeName() + "与数据定义中的数据类型"
						+ dataType.toString() + "不一致");
			}
		}
		else {
			if (dataType == DataType.Number) {
				if (length < fieldStruct.getLength()) {
					throw new CommonException("表" + tableName + "的字段" + fieldStruct.getName() + "长度不能改小");
				}
				if (length > fieldStruct.getLength()) {
					dataTypeChange = true;
				}
				if (scale != fieldStruct.getScale()) {
					dataTypeChange = true;
				}
			}
			else if (dataType == DataType.String || dataType == DataType.NString) {
				if (length < fieldStruct.getLength()) {
					throw new CommonException("表" + tableName + "的字段" + fieldStruct.getName() + "长度不能改小");
				}
				if (length > fieldStruct.getLength()) {
					dataTypeChange = true;
				}
			}
		}
		if (dataTypeChange) {
			String sql = ctx.getDialect().getModifyColumnDataTypeCommand(tableName, fieldStruct.getName(), dataType, length, scale, allowNull,
					defaultValue, comment);
			ctx.execute(sql);
		}
		if (fieldStruct.isNullable() != allowNull) {
			String sql = ctx.getDialect().getModifyColumnNullCommand(tableName, fieldStruct.getName(), dataType, length, scale, allowNull,
					defaultValue, comment);
			ctx.execute(sql);
		}
		boolean defaultChange = false;
		if (!io.github.summercattle.commons.utils.auxiliary.StringUtils.equals(fieldStruct.getDefaultValue(), defaultValue)) {
			if (StringUtils.isNotBlank(defaultValue)) {
				if (dataType == DataType.Number) {
					if (StringUtils.isNotBlank(fieldStruct.getDefaultValue())) {
						BigDecimal StructDefault = NumberUtils.toBigDecimal(fieldStruct.getDefaultValue());
						BigDecimal metaDefault = NumberUtils.toBigDecimal(defaultValue);
						if (StructDefault.compareTo(metaDefault) != 0) {
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
			String sql = ctx.getDialect().getModifyColumnDefaultCommand(tableName, fieldStruct.getName(), dataType, defaultValue);
			ctx.execute(sql);
		}
		if (!io.github.summercattle.commons.utils.auxiliary.StringUtils.equals(fieldStruct.getComment(), comment)) {
			String columnComment = ctx.getDialect().getColumnComment(comment);
			if (StringUtils.isNotBlank(columnComment)) {
				StringBuffer buf = new StringBuffer(ctx.getDialect().getAlterTableString(tableName));
				buf.append(" " + ctx.getDialect().getModifyColumnString());
				buf.append(" " + ctx.getDialect().quote(fieldStruct.getName()) + " " + ctx.getDialect().getTypeName(dataType, length, scale));
				if (allowNull) {
					buf.append(ctx.getDialect().getNullColumnString());
				}
				else {
					buf.append(" not null");
				}
				if (StringUtils.isNotBlank(defaultValue)) {
					buf.append(" default ");
					if (JdbcUtils.isString(fieldStruct.getJdbcType())) {
						buf.append("'");
					}
					buf.append(defaultValue);
					if (JdbcUtils.isString(fieldStruct.getJdbcType())) {
						buf.append("'");
					}
				}
				buf.append(columnComment);
				ctx.execute(buf.toString());
			}
			if (ctx.getDialect().supportsCommentOn()) {
				commentColumn(ctx, tableName, fieldStruct.getName(), comment);
			}
		}
	}

	private void create(DbProperties dbProperties, SimpleDalContext ctx, TableMeta tableMeta) throws CommonException {
		StringBuilder buf = new StringBuilder(ctx.getDialect().getCreateTableString() + " " + tableMeta.getName() + " (")
				.append(ctx.getDialect().quote(dbProperties.getPrimaryField()) + " "
						+ (tableMeta.isPrimaryKeyUseNumber() ? ctx.getDialect().getTypeName(DataType.Number, 10, 0)
								: ctx.getDialect().getTypeName(DataType.NString, 50, 0))
						+ " not null")
				.append(ctx.getDialect().getColumnComment(PRIMARY_FIELD_COMMENT) + ",");
		FieldMeta[] fieldMetas = tableMeta.getFields();
		for (FieldMeta fieldMeta : fieldMetas) {
			buf.append(ctx.getDialect().quote(fieldMeta.getName()) + " ");
			DataType dataType;
			int length;
			int scale = 0;
			String defaultValue = "";
			if (FieldMetaMode.Fixed == fieldMeta.getMode()) {
				dataType = ((FixedFieldMeta) fieldMeta).getType();
				length = ((FixedFieldMeta) fieldMeta).getLength();
				scale = ((FixedFieldMeta) fieldMeta).getScale();
				defaultValue = ((FixedFieldMeta) fieldMeta).getDefault();
			}
			else if (FieldMetaMode.Reference == fieldMeta.getMode()) {
				TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) fieldMeta).getReferenceTableName());
				if (referenceTable.isPrimaryKeyUseNumber()) {
					dataType = DataType.Number;
					length = 10;
				}
				else {
					dataType = DataType.NString;
					length = 50;
				}
			}
			else {
				throw new CommonException(
						"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
			}
			buf.append(ctx.getDialect().getTypeName(dataType, length, scale));
			if (StringUtils.isNotBlank(defaultValue)) {
				buf.append(" default ");
				if (dataType == DataType.String || dataType == DataType.NString || dataType == DataType.LongString || dataType == DataType.Clob
						|| dataType == DataType.NClob) {
					buf.append("'" + defaultValue + "'");
				}
				else {
					buf.append(defaultValue);
				}
			}
			if (fieldMeta.allowNull()) {
				buf.append(ctx.getDialect().getNullColumnString());
			}
			else {
				buf.append(" not null");
			}
			if (StringUtils.isNotBlank(fieldMeta.getComment())) {
				buf.append(ctx.getDialect().getColumnComment(fieldMeta.getComment()));
			}
			buf.append(",");
		}
		buf.append(ctx.getDialect().quote(dbProperties.getCreateTimeField()) + " " + ctx.getDialect().getTypeName(DataType.Timestamp))
				.append(ctx.getDialect().getNullColumnString()).append(ctx.getDialect().getColumnComment(CREATE_TIME_FIELD_COMMENT) + ",");
		buf.append(ctx.getDialect().quote(dbProperties.getUpdateTimeField()) + " " + ctx.getDialect().getTypeName(DataType.Timestamp))
				.append(ctx.getDialect().getNullColumnString()).append(ctx.getDialect().getColumnComment(UPDATE_TIME_FIELD_COMMENT) + ",");
		buf.append(ctx.getDialect().quote(dbProperties.getVersionField()) + " " + ctx.getDialect().getTypeName(DataType.Number, 10, 0)
				+ " default 0 not null" + ctx.getDialect().getColumnComment(VERSION_FIELD_COMMENT) + ",");
		buf.append(ctx.getDialect().quote(dbProperties.getDeletedField()) + " " + ctx.getDialect().getTypeName(DataType.Boolean)
				+ " default 0 not null" + ctx.getDialect().getColumnComment(DELETED_FIELD_COMMENT) + ")");
		if (StringUtils.isNotBlank(tableMeta.getComment())) {
			buf.append(ctx.getDialect().getTableComment(tableMeta.getComment()));
		}
		buf.append(ctx.getDialect().getTableTypeString());
		ctx.execute(buf.toString());
		createTablePrimaryKey(dbProperties, ctx, tableMeta);
		if (ctx.getDialect().supportsCommentOn()) {
			if (StringUtils.isNotBlank(tableMeta.getComment())) {
				commentTable(ctx, tableMeta.getName(), tableMeta.getComment());
			}
			commentColumn(ctx, tableMeta.getName(), dbProperties.getPrimaryField(), PRIMARY_FIELD_COMMENT);
			for (FieldMeta fieldMeta : fieldMetas) {
				if (StringUtils.isNotBlank(fieldMeta.getComment())) {
					commentColumn(ctx, tableMeta.getName(), fieldMeta.getName(), fieldMeta.getComment());
				}
			}
			commentColumn(ctx, tableMeta.getName(), dbProperties.getCreateTimeField(), CREATE_TIME_FIELD_COMMENT);
			commentColumn(ctx, tableMeta.getName(), dbProperties.getUpdateTimeField(), UPDATE_TIME_FIELD_COMMENT);
			commentColumn(ctx, tableMeta.getName(), dbProperties.getVersionField(), VERSION_FIELD_COMMENT);
			commentColumn(ctx, tableMeta.getName(), dbProperties.getDeletedField(), DELETED_FIELD_COMMENT);
		}
		IndexMeta[] indexMetas = tableMeta.getIndexes();
		for (IndexMeta indexMeta : indexMetas) {
			createTableIndex(ctx, tableMeta, indexMeta);
		}
	}

	private void createTablePrimaryKey(DbProperties dbProperties, SimpleDalContext ctx, TableMeta tableMeta) throws CommonException {
		String pkName = DataConstants.PRIMARY_KEY_PREFIX + io.github.summercattle.commons.utils.auxiliary.StringUtils
				.getHashName("Table'" + tableMeta.getName() + "'PrimaryKey'" + dbProperties.getPrimaryField() + "'");
		String sql = ctx.getDialect().getAlterTableString(tableMeta.getName()) + ctx.getDialect().getAddPrimaryKeyString(pkName) + "("
				+ ctx.getDialect().quote(dbProperties.getPrimaryField()) + ")";
		ctx.execute(sql);
	}

	private void createTableIndex(SimpleDalContext ctx, TableMeta tableMeta, IndexMeta indexMeta) throws CommonException {
		String metaIndexFields = "";
		IndexFieldMeta[] fields = indexMeta.getFields();
		for (int t = 0; t < fields.length; t++) {
			if (t > 0) {
				metaIndexFields += ",";
			}
			metaIndexFields += tableMeta.getField(fields[t].getField()).getName();
			metaIndexFields += ":" + fields[t].getOrder();
		}
		String idxName = DataConstants.INDEX_KEY_PREFIX + io.github.summercattle.commons.utils.auxiliary.StringUtils.getHashName("Table'"
				+ tableMeta.getName() + "'Unique'" + BooleanUtils.toStringTrueFalse(indexMeta.isUnique()) + "'Columns'" + metaIndexFields + "'");
		StringBuffer buf = new StringBuffer(
				"create " + (indexMeta.isUnique() ? "unique " : "") + "index " + idxName + " on " + tableMeta.getName() + " (");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0) {
				buf.append(",");
			}
			IndexFieldMeta indexFieldMeta = fields[i];
			FieldMeta field = tableMeta.getField(indexFieldMeta.getField());
			DataType fieldType;
			if (FieldMetaMode.Fixed == field.getMode()) {
				fieldType = ((FixedFieldMeta) field).getType();
			}
			else if (FieldMetaMode.Reference == field.getMode()) {
				TableMeta referenceTable = dbMetaModel.getTable(((ReferenceFieldMeta) field).getReferenceTableName());
				fieldType = referenceTable.isPrimaryKeyUseNumber() ? DataType.Number : DataType.NString;
			}
			else {
				throw new CommonException("表'" + tableMeta.getName() + "'的字段'" + field.getName() + "'模式'" + field.getMode().toString() + "'不支持");
			}
			if (fieldType == DataType.Clob || fieldType == DataType.NClob || fieldType == DataType.Blob) {
				throw new CommonException("表'" + tableMeta.getName() + "'的字段'" + field.getName() + "'的数据类型'" + fieldType.toString() + "',不能进行索引");
			}
			buf.append(ctx.getDialect().quote(field.getName()));
			buf.append(" " + indexFieldMeta.getOrder());
		}
		buf.append(")");
		ctx.execute(buf.toString());
	}

	private void createCustomSequenceTable(SimpleDalContext ctx) throws CommonException {
		String createSQL = ctx.getDialect().getCreateTableString() + " " + DataConstants.SEQUENCE_TABLE_NAME + " (";
		createSQL += ctx.getDialect().quote(DataConstants.SEQUENCE_FIELD_NAME) + " " + ctx.getDialect().getTypeName(DataType.NString, 50, 0)
				+ " not null,";
		createSQL += ctx.getDialect().quote(DataConstants.SEQUENCE_FIELD_VALUE) + " ";
		createSQL += ctx.getDialect().getTypeName(DataType.Number, 20, 0);
		createSQL += " default 0";
		createSQL += ")";
		if (StringUtils.isNotBlank(ctx.getDialect().getTableTypeString())) {
			createSQL += " " + ctx.getDialect().getTableTypeString();
		}
		ctx.execute(createSQL);
		String createPrimaryKeySQL = ctx.getDialect().getAlterTableString(DataConstants.SEQUENCE_TABLE_NAME)
				+ ctx.getDialect().getAddPrimaryKeyString(DataConstants.PRIMARY_KEY_PREFIX + DataConstants.SEQUENCE_TABLE_NAME) + "("
				+ ctx.getDialect().quote(DataConstants.SEQUENCE_FIELD_NAME) + ")";
		ctx.execute(createPrimaryKeySQL);
	}

	private void commentTable(SimpleDalContext ctx, String tableName, String comment) throws CommonException {
		String sql = "comment on table " + tableName + " is '" + comment + "'";
		ctx.execute(sql);
	}

	private void commentColumn(SimpleDalContext ctx, String tableName, String columnName, String comment) throws CommonException {
		String sql = "comment on column " + tableName + "." + ctx.getDialect().quote(columnName) + " is '" + comment + "'";
		ctx.execute(sql);
	}

	@Override
	public boolean existTable(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("表名为空");
		}
		return dbTransaction.doSimpleDal(ctx -> {
			if (!ctx.getDialect().getStructHandler().supportsTable()) {
				throw new CommonException("不支持数据表结构的查询");
			}
			return ctx.getDialect().getStructHandler().existTable(ctx.getConnection(), name);
		});
	}

	@Override
	public boolean existView(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("视图名为空");
		}
		return dbTransaction.doSimpleDal(ctx -> {
			if (!ctx.getDialect().getStructHandler().supportsView()) {
				throw new CommonException("不支持数据视图结构的查询");
			}
			return ctx.getDialect().getStructHandler().existView(ctx.getConnection(), name);
		});
	}

	@Override
	public ViewObjectStruct getViewStruct(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("视图名为空");
		}
		Cache cache = cacheManager.getCache(DataConstants.CAFFEINE_VIEW_STRUCT);
		ViewObjectStruct viewObjectStruct = (ViewObjectStruct) cache.get(name.toLowerCase());
		if (null == viewObjectStruct) {
			if (!existView(name)) {
				throw new CommonException("视图'" + name + "'不存在");
			}
			else {
				ViewObjectStruct viewStruct = dbTransaction.doSimpleDal(ctx -> {
					if (!ctx.getDialect().getStructHandler().supportsView()) {
						throw new CommonException("不支持数据视图结构的查询");
					}
					return ctx.getDialect().getStructHandler().getView(ctx.getConnection(), name);
				});
				cache.put(name.toLowerCase(), viewStruct);
				viewObjectStruct = (ViewObjectStruct) cache.get(name.toLowerCase());
			}
		}
		return viewObjectStruct;
	}
}