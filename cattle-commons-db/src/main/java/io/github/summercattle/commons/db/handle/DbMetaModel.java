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
package io.github.summercattle.commons.db.handle;

import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedTableMeta;
import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据定义模型
 */
public interface DbMetaModel {

	/**
	 * 根据数据表名得到数据表定义信息
	 * @param name 数据表名
	 * @return 数据表定义信息
	 * @throws CommonException 异常
	 */
	TableMeta getTable(String name) throws CommonException;

	/**
	 * 根据指定Bean类型
	 * @param clazz 类
	 * @return 数据表定义信息
	 * @throws CommonException 异常
	 */
	AnnotatedTableMeta getTableByBean(Class< ? > clazz) throws CommonException;

	/**
	 * 是否存在指定的数据表
	 * @param name 数据表名
	 * @return 是否存在数据表
	 * @throws CommonException 异常
	 */
	boolean existTable(String name) throws CommonException;

	/**
	 * 是否存在指定bean类型的数据表
	 * @param clazz 类
	 * @return 是否存在数据表
	 * @throws CommonException 异常
	 */
	boolean existTableByBean(Class< ? > clazz) throws CommonException;

	/**
	 * 得到所有数据表定义信息
	 * @return 所有数据表定义信息
	 * @throws CommonException 异常
	 */
	TableMeta[] getTables() throws CommonException;
}