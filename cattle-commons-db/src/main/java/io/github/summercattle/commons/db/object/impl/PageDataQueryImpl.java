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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.field.FieldTypes;
import io.github.summercattle.commons.db.object.PageDataQuery;
import io.github.summercattle.commons.db.object.internal.impl.RowLineImpl;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class PageDataQueryImpl extends DataQueryImpl implements PageDataQuery {

	private int totalRecords;

	private int perPageSize;

	private int page;

	public PageDataQueryImpl(Dialect dialect, ResultSet rs, boolean isCustomPage, int perPageSize, int page, int totalRecords)
			throws CommonException {
		this.page = page;
		this.perPageSize = perPageSize;
		this.totalRecords = totalRecords;
		initFieldsInfo(dialect, rs);
		initLines(rs, isCustomPage, perPageSize, page);
	}

	@Override
	protected void initFieldsInfo(Dialect dialect, ResultSet rs) throws CommonException {
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			boolean isFilterField = dialect.getLimitHandler().isFilterPageFields();
			String[] filterFields = null;
			int realColumnCount = columnCount;
			if (isFilterField) {
				filterFields = dialect.getLimitHandler().getFilterPageFields();
				int filterColumn = 0;
				for (int i = 0; i < columnCount; i++) {
					String jdbcColumnName = metaData.getColumnName(i + 1).toUpperCase();
					String jdbcColumnLabel = metaData.getColumnLabel(i + 1).toUpperCase();
					String columnName = jdbcColumnName.equals(jdbcColumnLabel) ? jdbcColumnName : jdbcColumnLabel;
					boolean isFilter = false;
					if (filterFields != null && filterFields.length > 0) {
						for (String filterField : filterFields) {
							if (columnName.equalsIgnoreCase(filterField)) {
								isFilter = true;
								break;
							}
						}
					}
					if (isFilter) {
						filterColumn++;
					}
				}
				realColumnCount = realColumnCount - filterColumn;
			}
			fieldNames = new String[realColumnCount];
			fieldTypes = new int[realColumnCount];
			int lIndex = 0;
			for (int i = 0; i < columnCount; i++) {
				String jdbcColumnName = metaData.getColumnName(i + 1).toUpperCase();
				String jdbcColumnLabel = metaData.getColumnLabel(i + 1).toUpperCase();
				String columnName = jdbcColumnName.equalsIgnoreCase(jdbcColumnLabel) ? jdbcColumnName : jdbcColumnLabel;
				int columnType = metaData.getColumnType(i + 1);
				columnType = getColumnType(columnName, columnType, metaData.getColumnTypeName(i + 1));
				boolean isFilter = false;
				if (isFilterField) {
					if (filterFields != null && filterFields.length > 0) {
						for (String filterField : filterFields) {
							if (columnName.equals(filterField.toUpperCase())) {
								isFilter = true;
								break;
							}
						}
					}
				}
				if (!isFilter) {
					fieldIndexes.put(columnName.toUpperCase(), lIndex);
					fieldNames[lIndex] = columnName;
					fieldTypes[lIndex] = columnType;
					lIndex++;
				}
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public int getTotalRecords() {
		return totalRecords;
	}

	@Override
	public int getPage() {
		return page;
	}

	@Override
	public int getPageSize() {
		if (perPageSize == 0) {
			return 0;
		}
		//先求得整除的结果
		int pageCount = totalRecords / perPageSize;
		//再求得模运算的结果
		int temp = totalRecords % perPageSize;
		//若模运算的结果不为零，则总页数为整除的结果加上模运算的结果
		if (temp > 0) {
			pageCount += 1;
		}
		return pageCount;
	}

	private void initLines(ResultSet rs, boolean isCustomPage, Integer perPageSize, Integer page) throws CommonException {
		try {
			if (isCustomPage) {
				int row = perPageSize * (page - 1);
				if (row > 0) {
					rs.absolute(row);
				}
			}
			int index = 0;
			while (rs.next()) {
				Object[] values = new Object[fieldNames.length];
				if (isCustomPage) {
					index++;
					if (index > perPageSize) {
						break;
					}
				}
				for (int i = 0; i < fieldNames.length; i++) {
					values[i] = FieldTypes.getType(null, fieldNames[i], fieldTypes[i]).nullSafeGet(rs, fieldNames[i]);
				}
				lines.add(new RowLineImpl(values));
			}
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}