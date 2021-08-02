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
package io.github.summercattle.commons.db.meta;

import java.util.List;

import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据表定义信息
 */
public interface TableMeta {

	/**
	 * 表名
	 * @return 表名
	 */
	String getName();

	/**
	 * 别名
	 * @return 别名
	 */
	String getAlias();

	/**
	 * 是否使用缓存
	 * @return 是否使用缓存
	 */
	boolean isUseCache();

	/**
	 * 备注
	 * @return 备注
	 */
	String getComment();

	/**
	 * 主键是否使用数值
	 * @return 主键是否使用数值
	 */
	boolean isPrimaryKeyUseNumber();

	/**
	 * 主键约束名称
	 * @return 主键约束名称
	 */
	String getPrimaryKeyName();

	/**
	 * 得到所有字段定义信息
	 * @return 字段定义信息
	 */
	FieldMeta[] getFields();

	/**
	 * 根据指定名称得到字段定义信息
	 * @param name 字段定义名称
	 * @return 字段定义信息
	 * @throws CommonException 异常
	 */
	FieldMeta getField(String name) throws CommonException;

	/**
	 * 得到关联字段信息
	 * @return 关联字段信息
	 * @throws CommonException 异常
	 */
	List<ReferenceFieldInfo> getReferenceFieldInfos() throws CommonException;

	/**
	 * 得到所有索引定义信息
	 * @return 索引定义信息
	 */
	IndexMeta[] getIndexes();

	/**
	 * 得到数据表定义来源
	 * @return 数据表定义来源
	 */
	TableMetaSource getSource();
}