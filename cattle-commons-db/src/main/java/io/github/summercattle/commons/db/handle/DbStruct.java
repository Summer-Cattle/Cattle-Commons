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

import java.sql.Connection;

import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据结构
 */
public interface DbStruct {

	/**
	 * 得到表结构
	 * @param tableName 表名
	 * @return 表结构
	 * @throws CommonException 异常
	 */
	TableObjectStruct getTableStruct(String tableName) throws CommonException;

	/**
	 * 得到表结构
	 * @param dbProperties 数据配置
	 * @param tableMeta 表定义
	 * @return 表结构
	 * @throws CommonException 异常
	 */
	TableObjectStruct getTableStruct(DbProperties dbProperties, TableMeta tableMeta) throws CommonException;

	/**
	 * 检查表与索引
	 * @param dbProperties 数据配置
	 * @param conn 数据库连接
	 * @throws CommonException 异常
	 */
	void checkTablesAndIndexes(DbProperties dbProperties, Connection conn) throws CommonException;

	/**
	 * 表是否存在
	 * @param tableName 表名
	 * @return 表是否存在
	 * @throws CommonException 异常
	 */
	boolean existTable(String tableName) throws CommonException;

	/**
	 * 视图是否存在
	 * @param viewName 视图名
	 * @return 视图是否存在
	 * @throws CommonException 异常
	 */
	boolean existView(String viewName) throws CommonException;

	/**
	 * 得到视图结构
	 * @param viewName 视图名称
	 * @return 视图结构
	 * @throws CommonException 异常
	 */
	ViewObjectStruct getViewStruct(String viewName) throws CommonException;
}