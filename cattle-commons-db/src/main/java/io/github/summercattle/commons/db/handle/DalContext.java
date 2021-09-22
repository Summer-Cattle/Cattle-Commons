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

import java.util.List;

import io.github.summercattle.commons.db.object.DataQuery;
import io.github.summercattle.commons.db.object.DataTable;
import io.github.summercattle.commons.db.object.DynamicPageDataQuery;
import io.github.summercattle.commons.db.object.PageDataQuery;
import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据库操作上下文
 */
public interface DalContext extends AbstractDalContext {

	/**
	 * 查询
	 * @param sql SQL语句
	 * @param params 参数
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataQuery query(String sql, Object[] params) throws CommonException;

	/**
	 * 分页查询
	 * @param sql SQL语句
	 * @param params 参数
	 * @param perPageSize 每页大小
	 * @param page 页码
	 * @return 分页数据结果集
	 * @throws CommonException 异常
	 */
	PageDataQuery queryPage(String sql, Object[] params, int perPageSize, int page) throws CommonException;

	/**
	 * 动态分页查询
	 * @param sql SQL语句
	 * @param params 参数
	 * @param perPageSize 每页大小
	 * @param page 页码
	 * @return 动态分页数据结果集
	 * @throws CommonException 异常
	 */
	DynamicPageDataQuery queryDynamicPage(String sql, Object[] params, int perPageSize, int page) throws CommonException;

	/**
	 * 查询
	 * @param name 表名
	 * @param condition 条件
	 * @param params 参数
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable select(String name, String condition, Object[] params) throws CommonException;

	/**
	 * 查询
	 * @param name 表名
	 * @param orderBy 排序
	 * @param condition 条件
	 * @param params 参数
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable select(String name, String orderBy, String condition, Object[] params) throws CommonException;

	/**
	 * 查询
	 * @param name 表名
	 * @param orderBy 排序
	 * @param condition 条件
	 * @param params 参数
	 * @param includeDeleted 包括删除
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable select(String name, String orderBy, String condition, Object[] params, boolean includeDeleted) throws CommonException;

	/**
	 * 查询
	 * @param name 表名
	 * @param primaryValue 主键值
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable select(String name, Object primaryValue) throws CommonException;

	/**
	 * 查询
	 * @param name 表名
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable select(String name) throws CommonException;

	/**
	 * 查询
	 * @param name 表名
	 * @param includeDeleted 包括删除
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable select(String name, boolean includeDeleted) throws CommonException;

	/**
	 * 创建空数据结果集
	 * @param name 表名
	 * @return 数据结果集
	 * @throws CommonException 异常
	 */
	DataTable create(String name) throws CommonException;

	/**
	 * 保存
	 * @param dataTable 数据结果集
	 * @throws CommonException 异常
	 */
	void save(DataTable dataTable) throws CommonException;

	/**
	 * 删除
	 * @param name 表名
	 * @param condition 条件
	 * @param params 参数
	 * @throws CommonException 异常
	 */
	void delete(String name, String condition, Object[] params) throws CommonException;

	/**
	 * 删除
	 * @param name 表名
	 * @param primaryValue 主键值
	 * @throws CommonException 异常
	 */
	void delete(String name, Object primaryValue) throws CommonException;

	/**
	 * 查询
	 * @param beanType bean类型 
	 * @param primaryValue 主键值
	 * @return 结果Bean
	 * @throws CommonException 异常
	 */
	<T> T select(Class<T> beanType, Object primaryValue) throws CommonException;

	/**
	 * 查询
	 * @param beanType bean类型 
	 * @param orderBy 排序
	 * @param condition 条件
	 * @param params 参数
	 * @param includeDeleted 包括删除
	 * @return 结果Bean
	 * @throws CommonException 异常
	 */
	<T> List<T> select(Class<T> beanType, String orderBy, String condition, Object[] params, boolean includeDeleted) throws CommonException;

	/**
	 * 新增保存
	 * @param bean bean对象
	 * @return 主键值
	 * @throws CommonException 异常
	 */
	Object save(Object bean) throws CommonException;

	/**
	 * 修改保存
	 * @param bean bean对象
	 * @throws CommonException 异常
	 */
	void update(Object bean) throws CommonException;
}