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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.db.DbUtils;
import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.constants.DataConstants;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.field.FieldTypes;
import io.github.summercattle.commons.db.handle.DalContext;
import io.github.summercattle.commons.db.meta.FieldMeta;
import io.github.summercattle.commons.db.meta.FieldMetaMode;
import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedFixedFieldMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedSystemFieldMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedTableMeta;
import io.github.summercattle.commons.db.object.DataQuery;
import io.github.summercattle.commons.db.object.DataTable;
import io.github.summercattle.commons.db.object.DynamicPageDataQuery;
import io.github.summercattle.commons.db.object.PageDataQuery;
import io.github.summercattle.commons.db.object.impl.DataQueryImpl;
import io.github.summercattle.commons.db.object.impl.DataTableImpl;
import io.github.summercattle.commons.db.object.impl.DynamicPageDataQueryImpl;
import io.github.summercattle.commons.db.object.impl.PageDataQueryImpl;
import io.github.summercattle.commons.db.object.internal.InternalDataTable;
import io.github.summercattle.commons.db.object.internal.RowLineSet;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.utils.JdbcUtils;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.ArrayUtils;
import io.github.summercattle.commons.utils.cache.Cache;
import io.github.summercattle.commons.utils.cache.CacheManager;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ClassUtils;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;
import io.github.summercattle.commons.utils.spring.SpringContext;

public class DalContextImpl extends AbstractDalContextImpl implements DalContext {

	private static final Logger logger = LoggerFactory.getLogger(DalContextImpl.class);

	private CacheManager cacheManager;

	public DalContextImpl(Dialect dialect, Connection conn, CacheManager cacheManager) {
		super(dialect, conn);
		this.cacheManager = cacheManager;
	}

	@Override
	public DataQuery query(String sql, Object[] params) throws CommonException {
		checkQuerySQL(sql);
		sql = parserSQL(sql);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			String info = "执行SQL语句:" + sql + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
			if (params != null && params.length > 0) {
				setParams(ps, 1, params, info);
			}
			rs = JdbcUtils.executeQuery(ps, info);
			return new DataQueryImpl(getDialect(), rs);
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
	public PageDataQuery queryPage(String sql, Object[] params, int perPageSize, int page) throws CommonException {
		checkQuerySQL(sql);
		String lStrSQL = sql.trim();
		lStrSQL = parserSQL(lStrSQL);
		if (perPageSize <= 0) {
			throw new CommonException("每页查询的最大记录数必须大于0");
		}
		if (page <= 0) {
			throw new CommonException("查询的页码必须大于0");
		}
		boolean isCustomPage = false;
		if (!getDialect().supportsPageLimitOffset() && page > 1) {
			isCustomPage = true;
		}
		PreparedStatement psCount = null;
		PreparedStatement ps = null;
		ResultSet rsCount = null;
		ResultSet rs = null;
		try {
			int totalRecords = 0;
			String countSQL = "select count(*) from (" + lStrSQL + ") TMP_TAB";
			psCount = conn.prepareStatement(countSQL);
			String info = "执行SQL语句:" + countSQL + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
			if (params != null && params.length > 0) {
				setParams(psCount, 1, params, info);
			}
			rsCount = JdbcUtils.executeQuery(psCount, info);
			if (rsCount != null && rsCount.next()) {
				totalRecords = rsCount.getInt(1);
			}
			int pageCount = 0;
			if (perPageSize > 0 && totalRecords > 0) {
				//先求得整除的结果
				pageCount = totalRecords / perPageSize;
				//再求得模运算的结果
				int temp = totalRecords % perPageSize;
				//若模运算的结果不为零，则总页数为整除的结果加上模运算的结果
				if (temp > 0) {
					pageCount += 1;
				}
			}
			if (pageCount > 0 && page > pageCount) {
				throw new CommonException("查询的页码越界");
			}
			String executeInfo;
			if (isCustomPage) {
				executeInfo = "执行SQL语句:" + lStrSQL + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
				ps = conn.prepareStatement(lStrSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			}
			else {
				String tStrSQL = getDialect().getPageLimitString(lStrSQL.trim(), (page - 1) * perPageSize, perPageSize);
				executeInfo = "执行SQL语句:" + tStrSQL + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
				ps = conn.prepareStatement(tStrSQL);
			}
			if (params != null && params.length > 0) {
				setParams(ps, 1, params, executeInfo);
			}
			rs = JdbcUtils.executeQuery(ps, executeInfo);
			return new PageDataQueryImpl(getDialect(), rs, isCustomPage, perPageSize, page, totalRecords);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rsCount);
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(psCount);
			JdbcUtils.closeStatement(ps);
		}
	}

	@Override
	public DynamicPageDataQuery queryDynamicPage(String sql, Object[] params, int perPageSize, int page) throws CommonException {
		checkQuerySQL(sql);
		String lStrSQL = sql.trim();
		lStrSQL = parserSQL(lStrSQL);
		if (perPageSize <= 0) {
			throw new CommonException("每页查询的最大记录数必须大于0");
		}
		if (page <= 0) {
			throw new CommonException("查询的页码必须大于0");
		}
		boolean isCustomPage = false;
		if (!getDialect().supportsPageLimitOffset() && page > 1) {
			isCustomPage = true;
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String executeInfo;
			if (isCustomPage) {
				executeInfo = "执行SQL语句:" + lStrSQL + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
				ps = conn.prepareStatement(lStrSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			}
			else {
				String tStrSQL = getDialect().getPageLimitString(lStrSQL.trim(), (page - 1) * perPageSize, perPageSize + 1);
				executeInfo = "执行SQL语句:" + tStrSQL + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
				ps = conn.prepareStatement(tStrSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			}
			if (params != null && params.length > 0) {
				setParams(ps, 1, params, executeInfo);
			}
			rs = JdbcUtils.executeQuery(ps, executeInfo);
			return new DynamicPageDataQueryImpl(getDialect(), rs, isCustomPage, perPageSize, page);
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
	public DataTable select(String tableName, String condition, Object[] params) throws CommonException {
		return select(tableName, null, condition, params);
	}

	@Override
	public DataTable select(String tableName, String orderBy, String condition, Object[] params) throws CommonException {
		return select(tableName, orderBy, condition, params, false);
	}

	@Override
	public DataTable select(String tableName, Object primaryValue) throws CommonException {
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		return select(tableName, null, dbProperties.getPrimaryField() + "=?", new Object[] { primaryValue }, true);
	}

	@Override
	public DataTable select(String tableName) throws CommonException {
		return select(tableName, false);
	}

	@Override
	public DataTable select(String tableName, boolean includeDeleted) throws CommonException {
		return select(tableName, null, null, null, includeDeleted);
	}

	@Override
	public DataTable select(String tableName, String orderBy, String condition, Object[] params, boolean includeDeleted) throws CommonException {
		checkTableName(tableName);
		TableMeta tableMeta = DbUtils.getDbMetaModel().getTable(tableName);
		boolean useCache = tableMeta.isUseCache();
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		TableObjectStruct tableStruct = DbUtils.getDbStruct().getTableStruct(dbProperties, tableMeta);
		StringBuffer sb = new StringBuffer();
		sb.append("select ");
		sb.append(dialect.getSQLKeyword(dbProperties.getPrimaryField()) + ",");
		FieldMeta[] fieldMetas = tableMeta.getFields();
		for (FieldMeta fieldMeta : fieldMetas) {
			sb.append(dialect.getSQLKeyword(fieldMeta.getName()) + ",");
		}
		String name = tableMeta.getName();
		sb.append(dialect.getSQLKeyword(dbProperties.getCreateTimeField()) + ",");
		sb.append(dialect.getSQLKeyword(dbProperties.getUpdateTimeField()) + ",");
		sb.append(dialect.getSQLKeyword(dbProperties.getVersionField()) + ",");
		sb.append(dialect.getSQLKeyword(dbProperties.getDeletedField()));
		sb.append(" from " + dialect.getSQLKeyword(name));
		Object[] lParams = StringUtils.isNotBlank(condition) && params != null && params.length > 0
				? new Object[params.length + (includeDeleted ? 0 : 1)]
				: new Object[0 + (includeDeleted ? 0 : 1)];
		String filter = "";
		if (!includeDeleted) {
			filter += "(" + dialect.getSQLKeyword(dbProperties.getDeletedField()) + "=? or " + dialect.getSQLKeyword(dbProperties.getDeletedField())
					+ " is null)";
			lParams[0] = 0;
		}
		if (StringUtils.isNotBlank(condition)) {
			if (filter.length() > 0) {
				filter += " and (" + condition + ")";
			}
			else {
				filter += condition;
			}
			if (params != null && params.length > 0) {
				System.arraycopy(params, 0, lParams, includeDeleted ? 0 : 1, params.length);
			}
		}
		if (filter.length() > 0) {
			sb.append(" where " + filter);
		}
		if (StringUtils.isNotBlank(orderBy)) {
			sb.append(" order by " + orderBy.toUpperCase());
		}
		String strParam = lParams.length > 0 ? ArrayUtils.toString(lParams) : "无";
		DataTable dataTable = null;
		String cacheKey = null;
		if (useCache) {
			cacheKey = Hex.encodeHexString(DigestUtils.md5(lParams.length > 0 ? sb.toString() + strParam : sb.toString()));
			Cache cache = cacheManager.getCache(DataConstants.CAFFEINE_SELECT + "_" + name.toUpperCase(), dbProperties.getCacheProps());
			dataTable = (DataTable) cache.get(cacheKey);
			if (dataTable != null) {
				logger.debug("在数据库缓存'" + name + "'中读出数据,缓存Key'" + cacheKey + "'");
			}
		}
		if (dataTable == null) {
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				ps = conn.prepareStatement(sb.toString());
				String info = "执行SQL语句:" + sb.toString() + ",参数值:" + strParam;
				if (lParams.length > 0) {
					setParams(ps, 1, lParams, info);
				}
				rs = JdbcUtils.executeQuery(ps, info);
				dataTable = new DataTableImpl(dbProperties, rs, tableMeta, tableStruct);
				//有记录的情况下才做缓存
				if (useCache && dataTable.size() > 0) {
					Cache cache = cacheManager.getCache(DataConstants.CAFFEINE_SELECT + "_" + name.toUpperCase(), dbProperties.getCacheProps());
					cache.put(cacheKey, dataTable);
					logger.debug("在数据库缓存'" + name + "'中存入数据,缓存Key'" + cacheKey + "'");
				}
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			finally {
				JdbcUtils.closeResultSet(rs);
				JdbcUtils.closeStatement(ps);
			}
		}
		return dataTable;
	}

	@Override
	public DataTable create(String tableName) throws CommonException {
		checkTableName(tableName);
		TableMeta tableMeta = DbUtils.getDbMetaModel().getTable(tableName);
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		TableObjectStruct tableStruct = DbUtils.getDbStruct().getTableStruct(dbProperties, tableMeta);
		return new DataTableImpl(dbProperties, tableMeta, tableStruct);
	}

	@Override
	public void save(DataTable dataTable) throws CommonException {
		String[] fieldNames = dataTable.getFieldNames();
		int[] fieldTypes = ((InternalDataTable) dataTable).getFieldTypes();
		Map<String, Integer> fieldIndexes = ((InternalDataTable) dataTable).getFieldIndexes();
		String tableName = dataTable.getName();
		//处理删除
		RowLineSet[] deleteLines = ((InternalDataTable) dataTable).getDeleteLines();
		boolean[] fieldUseSQLKeyword = ((InternalDataTable) dataTable).getFieldUseSQLKeyword();
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		if (deleteLines.length > 0) {
			rowDelete(dbProperties, tableName, fieldNames, fieldTypes, fieldIndexes, deleteLines);
		}
		List<RowLineSet> addLines = new Vector<RowLineSet>();
		List<RowLineSet> modifyLines = new Vector<RowLineSet>();
		((InternalDataTable) dataTable).setAddAndModifyLines(addLines, modifyLines);
		if (addLines != null & addLines.size() > 0) {
			rowAdd(dbProperties, tableName, fieldNames, fieldTypes, fieldIndexes, addLines, fieldUseSQLKeyword);
		}
		if (modifyLines != null & modifyLines.size() > 0) {
			rowModify(dbProperties, tableName, fieldNames, fieldTypes, fieldIndexes, modifyLines, fieldUseSQLKeyword);
		}
		if (((InternalDataTable) dataTable).isUseCache()) {
			logger.debug("清除数据库缓存'" + tableName + "'");
			cacheManager.removeCache(DataConstants.CAFFEINE_SELECT + "_" + tableName.toUpperCase());
		}
	}

	private void rowAdd(DbProperties dbProperties, String tableName, String[] fieldNames, int[] fieldTypes, Map<String, Integer> fieldIndexes,
			List<RowLineSet> addLines, boolean[] fieldUseSQLKeyword) throws CommonException {
		Integer versionFieldIndex = fieldIndexes.get(dbProperties.getVersionField().toUpperCase());
		if (versionFieldIndex == null) {
			throw new CommonException("没有找到表" + tableName + "的版本字段" + dbProperties.getVersionField());
		}
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField().toUpperCase());
		if (primaryFieldIndex == null) {
			throw new CommonException("没有找到表" + tableName + "的主键字段" + dbProperties.getPrimaryField());
		}
		String lTableName = dialect.getSQLKeyword(tableName);
		String primaryField = dialect.getSQLKeyword(dbProperties.getPrimaryField());
		String versionField = dialect.getSQLKeyword(dbProperties.getVersionField());
		String createTimeField = dialect.getSQLKeyword(dbProperties.getCreateTimeField());
		PreparedStatement ps = null;
		try {
			StringBuffer sb = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			sb2.append(") values (?");
			sb.append("insert into " + lTableName + " (" + primaryField);
			for (int i = 0; i < fieldNames.length; i++) {
				sb.append(",");
				sb2.append(",");
				sb.append(dialect.getSQLKeyword(fieldNames[i], fieldUseSQLKeyword[i]));
				sb2.append("?");
			}
			sb.append("," + createTimeField + "," + versionField);
			sb.append(sb2);
			sb.append("," + dialect.getCurrentTimestampSQLFunctionName() + ",?");
			sb.append(")");
			ps = conn.prepareStatement(sb.toString());
			int addFrequency = 0;
			for (RowLineSet addLine : addLines) {
				Object[] values = addLine.getValues();
				Object[] lValues = new Object[fieldNames.length + 2];
				FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()]).nullSafeSet(getDialect(), ps,
						1, values[primaryFieldIndex.intValue()]);
				lValues[0] = values[primaryFieldIndex.intValue()];
				for (int i = 0; i < fieldNames.length; i++) {
					FieldTypes.getType(tableName, fieldNames[i], fieldTypes[i]).nullSafeSet(getDialect(), ps, i + 2, values[i]);
					lValues[i + 1] = values[i];
				}
				FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()]).nullSafeSet(getDialect(), ps,
						fieldNames.length + 2, 1);
				lValues[fieldNames.length + 1] = 1;
				if (addLines.size() > 1) {
					addFrequency = JdbcUtils.addBatch(ps, "执行SQL语句:" + sb.toString(), addFrequency);
				}
				else {
					JdbcUtils.executeUpdate(ps, "执行SQL语句:" + sb.toString() + ",参数值:" + ArrayUtils.toString(lValues));
				}
			}
			if (addLines.size() > 1) {
				JdbcUtils.completeBatch(ps, "执行SQL语句:" + sb.toString(), addFrequency);
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeStatement(ps);
		}
	}

	private void rowModify(DbProperties dbProperties, String tableName, String[] fieldNames, int[] fieldTypes, Map<String, Integer> fieldIndexes,
			List<RowLineSet> modifyLines, boolean[] fieldUseSQLKeyword) throws CommonException {
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField().toUpperCase());
		Integer versionFieldIndex = fieldIndexes.get(dbProperties.getVersionField().toUpperCase());
		String lTableName = dialect.getSQLKeyword(tableName);
		String primaryField = dialect.getSQLKeyword(dbProperties.getPrimaryField());
		String versionField = dialect.getSQLKeyword(dbProperties.getVersionField());
		String updateTimeField = dialect.getSQLKeyword(dbProperties.getUpdateTimeField());
		for (RowLineSet modifyLine : modifyLines) {
			Object[] values = modifyLine.getValues();
			Object id = values[primaryFieldIndex.intValue()];
			Long version = (Long) ReflectUtils.convertValue(ClassType.Long, values[versionFieldIndex]);
			if (version != null) {
				PreparedStatement queryPs = null;
				ResultSet queryRs = null;
				try {
					String sql = "select " + versionField + " from " + getDialect().appendLock(lTableName) + " where " + primaryField + "=?"
							+ getDialect().getForUpdateString();
					queryPs = conn.prepareStatement(sql);
					FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()]).nullSafeSet(getDialect(),
							queryPs, 1, id);
					queryRs = JdbcUtils.executeQuery(queryPs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { id }));
					if (!queryRs.next()) {
						throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录加锁失败");
					}
					long dbVersion = (long) ReflectUtils.convertValue(ClassType.Long, FieldTypes
							.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()]).nullSafeGet(queryRs, 1));
					if (version != dbVersion) {
						throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录已经被修改");
					}
				}
				catch (SQLException e) {
					throw ExceptionWrapUtils.wrap(e);
				}
				finally {
					JdbcUtils.closeResultSet(queryRs);
					JdbcUtils.closeStatement(queryPs);
				}
			}
			PreparedStatement updatePs = null;
			try {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < fieldNames.length; i++) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(dialect.getSQLKeyword(fieldNames[i], fieldUseSQLKeyword[i]));
					sb.append("=?");
				}
				sb.append("," + updateTimeField + "=" + dialect.getCurrentTimestampSQLFunctionName());
				sb.append("," + versionField + "=?");
				String sql = "update " + lTableName + " set " + sb.toString() + " where " + primaryField + "=?";
				if (version != null) {
					sql += " and " + versionField + "=?";
				}
				else {
					sql += " and " + versionField + " is null";
				}
				updatePs = conn.prepareStatement(sql);
				Object[] lValues = new Object[fieldNames.length + (version != null ? 3 : 2)];
				for (int i = 0; i < fieldNames.length; i++) {
					FieldTypes.getType(tableName, fieldNames[i], fieldTypes[i]).nullSafeSet(getDialect(), updatePs, i + 1, values[i]);
					lValues[i] = values[i];
				}
				FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()]).nullSafeSet(getDialect(),
						updatePs, fieldNames.length + 1, (version != null ? version.longValue() : 0) + 1);
				lValues[fieldNames.length] = (version != null ? version.longValue() : 0) + 1;
				FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()]).nullSafeSet(getDialect(),
						updatePs, fieldNames.length + 2, values[primaryFieldIndex.intValue()]);
				lValues[fieldNames.length + 1] = values[primaryFieldIndex.intValue()];
				if (version != null) {
					FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()]).nullSafeSet(getDialect(),
							updatePs, fieldNames.length + 3, version);
					lValues[fieldNames.length + 2] = version;
				}
				int rows = JdbcUtils.executeUpdate(updatePs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(lValues));
				if (rows == 0) {
					throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录已经被修改");
				}
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			finally {
				JdbcUtils.closeStatement(updatePs);
			}
		}
	}

	private void rowDelete(DbProperties dbProperties, String tableName, String[] fieldNames, int[] fieldTypes, Map<String, Integer> fieldIndexes,
			RowLineSet[] deleteLines) throws CommonException {
		Integer primaryFieldIndex = fieldIndexes.get(dbProperties.getPrimaryField().toUpperCase());
		Integer versionFieldIndex = fieldIndexes.get(dbProperties.getVersionField().toUpperCase());
		Integer deletedFieldIndex = fieldIndexes.get(dbProperties.getDeletedField().toUpperCase());
		String lTableName = dialect.getSQLKeyword(tableName);
		String primaryField = dialect.getSQLKeyword(dbProperties.getPrimaryField());
		String updateTimeField = dialect.getSQLKeyword(dbProperties.getUpdateTimeField());
		String versionField = dialect.getSQLKeyword(dbProperties.getVersionField());
		String deletedField = dialect.getSQLKeyword(dbProperties.getDeletedField());
		for (RowLineSet deleteLine : deleteLines) {
			Object[] values = deleteLine.getValues();
			Object id = values[primaryFieldIndex.intValue()];
			if (hasRelationDatas(tableName, id)) {
				Long version = (Long) ReflectUtils.convertValue(ClassType.Long, values[versionFieldIndex]);
				if (version != null) {
					PreparedStatement queryPs = null;
					ResultSet queryRs = null;
					try {
						String sql = "select " + versionField + " from " + getDialect().appendLock(lTableName) + " where " + primaryField + "=?"
								+ getDialect().getForUpdateString();
						queryPs = conn.prepareStatement(sql);
						FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()])
								.nullSafeSet(getDialect(), queryPs, 1, id);
						queryRs = JdbcUtils.executeQuery(queryPs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { id }));
						if (!queryRs.next()) {
							throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录加锁失败");
						}
						long dbVersion = (long) ReflectUtils.convertValue(ClassType.Long,
								FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()])
										.nullSafeGet(queryRs, 1));
						if (version != dbVersion) {
							throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录已经被修改");
						}
					}
					catch (SQLException e) {
						throw ExceptionWrapUtils.wrap(e);
					}
					finally {
						JdbcUtils.closeResultSet(queryRs);
						JdbcUtils.closeStatement(queryPs);
					}
				}
				PreparedStatement updatePs = null;
				try {
					String strUpdateSQL = "update " + lTableName + " set " + deletedField + "=?," + versionField + "=?," + updateTimeField + "="
							+ dialect.getCurrentTimestampSQLFunctionName() + " where " + primaryField + "=?";
					if (version != null) {
						strUpdateSQL += " and " + versionField + "=?";
					}
					else {
						strUpdateSQL += " and " + versionField + " is null";
					}
					updatePs = conn.prepareStatement(strUpdateSQL);
					FieldTypes.getType(tableName, dbProperties.getDeletedField(), fieldTypes[deletedFieldIndex.intValue()]).nullSafeSet(getDialect(),
							updatePs, 1, true);
					FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()]).nullSafeSet(getDialect(),
							updatePs, 2, (version != null ? version.longValue() : 0) + 1);
					FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()]).nullSafeSet(getDialect(),
							updatePs, 3, id);
					if (version != null) {
						FieldTypes.getType(tableName, dbProperties.getVersionField(), fieldTypes[versionFieldIndex.intValue()])
								.nullSafeSet(getDialect(), updatePs, 4, version.longValue());
					}
					int rows = JdbcUtils.executeUpdate(updatePs,
							"执行SQL语句:" + strUpdateSQL + ",参数值:"
									+ ArrayUtils.toString(version != null ? new Object[] { true, version.longValue() + 1, id, version.longValue() }
											: new Object[] { true, 1, id }));
					if (rows == 0) {
						throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录已经被修改");
					}
				}
				catch (SQLException e) {
					throw ExceptionWrapUtils.wrap(e);
				}
				finally {
					JdbcUtils.closeStatement(updatePs);
				}
			}
			else {
				PreparedStatement deletePs = null;
				try {
					String strDeleteSQL = "delete from " + lTableName + " where " + primaryField + "=?";
					deletePs = conn.prepareStatement(strDeleteSQL);
					FieldTypes.getType(tableName, dbProperties.getPrimaryField(), fieldTypes[primaryFieldIndex.intValue()]).nullSafeSet(getDialect(),
							deletePs, 1, id);
					int rows = JdbcUtils.executeUpdate(deletePs, "执行SQL语句:" + strDeleteSQL + ",参数值:" + ArrayUtils.toString(new Object[] { id }));
					if (rows == 0) {
						throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录删除失败");
					}
				}
				catch (SQLException e) {
					throw ExceptionWrapUtils.wrap(e);
				}
				finally {
					JdbcUtils.closeStatement(deletePs);
				}
			}
		}
	}

	@Override
	public void delete(String tableName, Object primaryValue) throws CommonException {
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		delete(tableName, dbProperties.getPrimaryField() + "=?", new Object[] { primaryValue });
	}

	@Override
	public void delete(String tableName, String condition, Object[] params) throws CommonException {
		checkTableName(tableName);
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		TableMeta tableMeta = DbUtils.getDbMetaModel().getTable(tableName);
		TableObjectStruct tableStruct = DbUtils.getDbStruct().getTableStruct(dbProperties, tableMeta);
		String name = tableMeta.getName();
		String lTableName = dialect.getSQLKeyword(name);
		String primaryField = dialect.getSQLKeyword(dbProperties.getPrimaryField());
		String deletedField = dialect.getSQLKeyword(dbProperties.getDeletedField());
		String versionField = dialect.getSQLKeyword(dbProperties.getVersionField());
		String updateTimeField = dialect.getSQLKeyword(dbProperties.getUpdateTimeField());
		int primaryFieldType = tableStruct.getField(dbProperties.getPrimaryField()).getJdbcType();
		int deleteFieldType = tableStruct.getField(dbProperties.getDeletedField()).getJdbcType();
		int versionFieldType = tableStruct.getField(dbProperties.getVersionField()).getJdbcType();
		StringBuffer sb = new StringBuffer();
		sb.append("select " + primaryField + "," + deletedField + "," + versionField + "," + updateTimeField + " from "
				+ getDialect().appendLock(lTableName));
		boolean isParams = false;
		if (StringUtils.isNotBlank(condition)) {
			sb.append(" where " + condition);
			if (params != null && params.length > 0) {
				isParams = true;
			}
		}
		sb.append(getDialect().getForUpdateString());
		String strParam = isParams ? ArrayUtils.toString(params) : "无";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sb.toString());
			String info = "执行SQL语句:" + sb.toString() + ",参数值:" + strParam;
			if (isParams) {
				setParams(ps, 1, params, info);
			}
			rs = JdbcUtils.executeQuery(ps, info);
			while (rs.next()) {
				Object id = FieldTypes.getType(tableName, dbProperties.getPrimaryField(), primaryFieldType).nullSafeGet(rs,
						dbProperties.getPrimaryField());
				boolean deleted = (boolean) ReflectUtils.convertValue(ClassType.Boolean, FieldTypes
						.getType(tableName, dbProperties.getDeletedField(), deleteFieldType).nullSafeGet(rs, dbProperties.getDeletedField()));
				Long version = (Long) ReflectUtils.convertValue(ClassType.Long, FieldTypes
						.getType(tableName, dbProperties.getVersionField(), versionFieldType).nullSafeGet(rs, dbProperties.getVersionField()));
				if (!deleted) {
					if (hasRelationDatas(tableName, id)) {
						PreparedStatement updatePs = null;
						try {
							String strUpdateSQL = "update " + lTableName + " set " + deletedField + "=?," + versionField + "=?," + updateTimeField
									+ "=" + dialect.getCurrentTimestampSQLFunctionName() + " where " + primaryField + "=?";
							if (version != null) {
								strUpdateSQL += " and " + versionField + "=?";
							}
							else {
								strUpdateSQL += " and " + versionField + " is null";
							}
							updatePs = conn.prepareStatement(strUpdateSQL);
							FieldTypes.getType(tableName, dbProperties.getDeletedField(), deleteFieldType).nullSafeSet(getDialect(), updatePs, 1,
									true);
							FieldTypes.getType(tableName, dbProperties.getVersionField(), versionFieldType).nullSafeSet(getDialect(), updatePs, 2,
									(version != null ? version.longValue() : 0) + 1);
							FieldTypes.getType(tableName, dbProperties.getPrimaryField(), primaryFieldType).nullSafeSet(getDialect(), updatePs, 3,
									id);
							if (version != null) {
								FieldTypes.getType(tableName, dbProperties.getVersionField(), versionFieldType).nullSafeSet(getDialect(), updatePs, 4,
										version);
							}
							int rows = JdbcUtils.executeUpdate(updatePs, "执行SQL语句:" + strUpdateSQL + ",参数值:" + ArrayUtils
									.toString(version != null ? new Object[] { true, version + 1, id, version } : new Object[] { true, 1, id }));
							if (rows == 0) {
								throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录已经被修改");
							}
						}
						finally {
							JdbcUtils.closeStatement(updatePs);
						}
					}
					else {
						PreparedStatement deletePs = null;
						try {
							String strDeleteSQL = "delete from " + lTableName + " where " + primaryField + "=?";
							deletePs = conn.prepareStatement(strDeleteSQL);
							FieldTypes.getType(tableName, dbProperties.getPrimaryField(), primaryFieldType).nullSafeSet(getDialect(), deletePs, 1,
									id);
							int rows = JdbcUtils.executeUpdate(deletePs,
									"执行SQL语句:" + strDeleteSQL + ",参数值:" + ArrayUtils.toString(new Object[] { id }));
							if (rows == 0) {
								throw new CommonException("表" + tableName + "的主键字段值" + id.toString() + "的记录删除失败");
							}
						}
						finally {
							JdbcUtils.closeStatement(deletePs);
						}
					}
				}
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
		if (tableMeta.isUseCache()) {
			logger.debug("清除数据库缓存'" + name + "'");
			cacheManager.removeCache(DataConstants.CAFFEINE_SELECT + "_" + name.toUpperCase());
		}
	}

	private void checkQuerySQL(String strSQL) throws CommonException {
		checkSQL(strSQL);
		String sql = strSQL.trim().toLowerCase();
		if (sql.startsWith("insert") || sql.startsWith("update") || sql.startsWith("delete") || sql.startsWith("drop") || sql.startsWith("alter")
				|| sql.startsWith("create")) {
			throw new CommonException("只允许使用SELECT语句");
		}
	}

	private void checkTableName(String tableName) throws CommonException {
		if (StringUtils.isBlank(tableName)) {
			throw new CommonException("表名为空");
		}
	}

	private boolean hasRelationDatas(String tableName, Object id) {
		return false;
	}

	@Override
	public <T> T select(Class<T> beanType, Object primaryValue) throws CommonException {
		if (null == primaryValue) {
			throw new CommonException("主键字段的值为空");
		}
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		List<T> beans = select(beanType, null, dbProperties.getPrimaryField() + "=?", new Object[] { primaryValue }, true, true);
		if (beans.size() > 0) {
			return beans.get(0);
		}
		return null;
	}

	@Override
	public <T> List<T> select(Class<T> beanType, String orderBy, String condition, Object[] params, boolean includeDeleted) throws CommonException {
		return select(beanType, orderBy, condition, params, includeDeleted, false);
	}

	private <T> List<T> select(Class<T> beanType, String orderBy, String condition, Object[] params, boolean includeDeleted, boolean singleRecord)
			throws CommonException {
		AnnotatedTableMeta tableMeta = DbUtils.getDbMetaModel().getTableByBean(beanType);
		DataTable dt = select(tableMeta.getName(), orderBy, condition, params, includeDeleted);
		if (dt.size() == 0) {
			return null;
		}
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		List<T> beans = new Vector<T>();
		dt.beforeFirst();
		while (dt.next()) {
			T bean = ClassUtils.instanceEmpty(beanType);
			AnnotatedSystemFieldMeta primaryField = tableMeta.getPrimaryField();
			if (null != primaryField) {
				Object primaryValue = getSystemFieldValue(dbProperties, dt, primaryField.getSystemFieldName());
				ReflectUtils.setFieldValue(bean, primaryField.getClassFieldName(), primaryValue);
			}
			FieldMeta[] fieldMetas = tableMeta.getFields();
			for (FieldMeta fieldMeta : fieldMetas) {
				Object value = dt.getObject(fieldMeta.getName());
				FieldMetaMode mode = fieldMeta.getMode();
				String classFieldName;
				if (FieldMetaMode.Fixed.equals(mode)) {
					classFieldName = ((AnnotatedFixedFieldMeta) fieldMeta).getClassFieldName();
				}
				else if (FieldMetaMode.Reference.equals(mode)) {
					classFieldName = ((AnnotatedReferenceFieldMeta) fieldMeta).getClassFieldName();
				}
				else {
					throw new CommonException(
							"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
				}
				ReflectUtils.setFieldValue(bean, classFieldName, value);
			}
			AnnotatedSystemFieldMeta[] systemFields = tableMeta.getSystemFields();
			for (AnnotatedSystemFieldMeta systemField : systemFields) {
				Object value = getSystemFieldValue(dbProperties, dt, systemField.getSystemFieldName());
				ReflectUtils.setFieldValue(bean, systemField.getClassFieldName(), value);
			}
			beans.add(bean);
			if (singleRecord) {
				break;
			}
		}
		return beans;
	}

	private Object getSystemFieldValue(DbProperties dbProperties, DataTable dt, String fieldName) throws CommonException {
		if (dbProperties.getPrimaryField().equals(fieldName)) {
			return dt.getPrimaryValue();
		}
		else if (dbProperties.getCreateTimeField().equals(fieldName)) {
			return dt.getCreateDate();
		}
		else if (dbProperties.getUpdateTimeField().equals(fieldName)) {
			return dt.getUpdateDate();
		}
		else if (dbProperties.getVersionField().equals(fieldName)) {
			return dt.getVersion();
		}
		else if (dbProperties.getDeletedField().equals(fieldName)) {
			return dt.isDeleted();
		}
		throw new CommonException("表'" + dt.getName() + "'的字段'" + fieldName + "'不是系统字段");
	}

	@Override
	public Object save(Object bean) throws CommonException {
		if (null != bean) {
			AnnotatedTableMeta tableMeta = DbUtils.getDbMetaModel().getTableByBean(bean.getClass());
			DataTable dt = create(tableMeta.getName());
			dt.insert();
			FieldMeta[] fieldMetas = tableMeta.getFields();
			for (FieldMeta fieldMeta : fieldMetas) {
				FieldMetaMode mode = fieldMeta.getMode();
				String classFieldName;
				if (FieldMetaMode.Fixed.equals(mode)) {
					classFieldName = ((AnnotatedFixedFieldMeta) fieldMeta).getClassFieldName();
				}
				else if (FieldMetaMode.Reference.equals(mode)) {
					classFieldName = ((AnnotatedReferenceFieldMeta) fieldMeta).getClassFieldName();
				}
				else {
					throw new CommonException(
							"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
				}
				Object value = ReflectUtils.getFieldValue(bean, classFieldName);
				dt.setObject(fieldMeta.getName(), value);
			}
			Object primaryValue = dt.getPrimaryValue();
			save(dt);
			return primaryValue;
		}
		return null;
	}

	@Override
	public void update(Object bean) throws CommonException {
		if (null != bean) {
			AnnotatedTableMeta tableMeta = DbUtils.getDbMetaModel().getTableByBean(bean.getClass());
			Object primaryValue = ReflectUtils.getFieldValue(bean, tableMeta.getPrimaryField().getClassFieldName());
			if (null == primaryValue) {
				throw new CommonException("类'" + bean.getClass().getName() + "'中没有主键字段'" + tableMeta.getPrimaryField().getClassFieldName() + "'的值为空");
			}
			DataTable dt = select(tableMeta.getName(), primaryValue);
			if (!dt.first()) {
				throw new CommonException("类'" + bean.getClass().getName() + "'中没有主键字段'" + tableMeta.getPrimaryField().getClassFieldName() + "'的值'"
						+ primaryValue.toString() + "'没有找到相应记录");
			}
			FieldMeta[] fieldMetas = tableMeta.getFields();
			for (FieldMeta fieldMeta : fieldMetas) {
				FieldMetaMode mode = fieldMeta.getMode();
				String classFieldName;
				if (FieldMetaMode.Fixed.equals(mode)) {
					classFieldName = ((AnnotatedFixedFieldMeta) fieldMeta).getClassFieldName();
				}
				else if (FieldMetaMode.Reference.equals(mode)) {
					classFieldName = ((AnnotatedReferenceFieldMeta) fieldMeta).getClassFieldName();
				}
				else {
					throw new CommonException(
							"表'" + tableMeta.getName() + "'的字段'" + fieldMeta.getName() + "'模式'" + fieldMeta.getMode().toString() + "'不支持");
				}
				Object value = ReflectUtils.getFieldValue(bean, classFieldName);
				dt.setObject(fieldMeta.getName(), value);
			}
			save(dt);
		}
	}
}