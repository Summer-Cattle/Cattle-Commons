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
package com.gitlab.summercattle.commons.db.handle.impl;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.annotation.Table;
import com.gitlab.summercattle.commons.db.handle.DbMetaModel;
import com.gitlab.summercattle.commons.db.meta.TableMeta;
import com.gitlab.summercattle.commons.db.meta.TableMetaParser;
import com.gitlab.summercattle.commons.db.meta.TableMetaSource;
import com.gitlab.summercattle.commons.db.meta.annotation.AnnotatedTableMeta;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.google.inject.Inject;

public class DbMetaIModelImpl implements DbMetaModel {

	private final List<TableMeta> tableMetas = new Vector<TableMeta>();

	@Inject
	private Set<TableMetaParser> tableMetaParsers;

	private boolean initialization = false;

	private synchronized void initialize() throws CommonException {
		if (!initialization) {
			for (TableMetaParser tableMetaParser : tableMetaParsers) {
				tableMetaParser.parser(tableMetas);
			}
			initialization = true;
		}
	}

	@Override
	public TableMeta getTable(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("名称为空");
		}
		if (!initialization) {
			initialize();
		}
		TableMeta result = null;
		for (TableMeta tableMeta : tableMetas) {
			if (name.equalsIgnoreCase(tableMeta.getName()) || name.equalsIgnoreCase(tableMeta.getAlias())) {
				result = tableMeta;
				break;
			}
		}
		if (null == result) {
			throw new CommonException("不存在数据表名称或别名'" + name + "'");
		}
		return result;
	}

	@Override
	public TableMeta[] getTables() throws CommonException {
		if (!initialization) {
			initialize();
		}
		return tableMetas.toArray(new TableMeta[0]);
	}

	@Override
	public AnnotatedTableMeta getTableByBean(Class< ? > clazz) throws CommonException {
		if (null == clazz) {
			throw new CommonException("类为空");
		}
		if (null == clazz.getAnnotation(Table.class)) {
			throw new CommonException("类'" + clazz.getName() + "'没有数据表注解");
		}
		String className = clazz.getName();
		if (!initialization) {
			initialize();
		}
		AnnotatedTableMeta result = null;
		for (TableMeta tableMeta : tableMetas) {
			if (tableMeta.getSource() == TableMetaSource.Annotated && ((AnnotatedTableMeta) tableMeta).getClassTypeName().equals(className)) {
				result = (AnnotatedTableMeta) tableMeta;
				break;
			}
		}
		if (null == result) {
			throw new CommonException("类'" + className + "'没有找到相应的数据表注解信息");
		}
		return result;
	}

	@Override
	public boolean existTable(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("名称为空");
		}
		if (!initialization) {
			initialize();
		}
		boolean result = false;
		for (TableMeta tableMeta : tableMetas) {
			if (name.equalsIgnoreCase(tableMeta.getName()) || name.equalsIgnoreCase(tableMeta.getAlias())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean existTableByBean(Class< ? > clazz) throws CommonException {
		if (null == clazz) {
			throw new CommonException("类为空");
		}
		if (null == clazz.getAnnotation(Table.class)) {
			throw new CommonException("类'" + clazz.getName() + "'没有数据表注解");
		}
		String className = clazz.getName();
		boolean result = false;
		for (TableMeta tableMeta : tableMetas) {
			if (tableMeta.getSource() == TableMetaSource.Annotated && ((AnnotatedTableMeta) tableMeta).getClassTypeName().equals(className)) {
				result = true;
				break;
			}
		}
		return result;
	}
}