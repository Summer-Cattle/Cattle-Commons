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
package io.github.summercattle.commons.db.dialect;

import java.sql.Connection;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.constants.DatabaseType;
import io.github.summercattle.commons.db.struct.TableObjectStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据库方言
 */
public interface Dialect {

	/**
	 * 数据库类型
	 * @return 数据库类型
	 */
	DatabaseType getType();

	/**
	 * 数据库模式名
	 * @return 数据库模式名
	 */
	String getSchema();

	/**
	 * 数据库有效语句
	 * @return 数据库有效语句
	 */
	String getValidateQuery();

	/**
	 * 是否支持Unicode字符串类型
	 * @return 是否支持Unicode字符串类型
	 */
	boolean supportsUnicodeStringType();

	/**
	 * 是否支持当前时间查询语句
	 * @return 是否支持当前时间查询语句
	 */
	boolean supportsCurrentTimestampSelection();

	/**
	 * 当前时间查询语句
	 * @return 当前时间查询语句
	 */
	String getCurrentTimestampSelectString();

	/**
	 * 是否支持当前时间调用语句
	 * @return 是否支持当前时间调用语句
	 */
	boolean isCurrentTimestampSelectStringCallable();

	/**
	 * 当前时间调用语句
	 * @return 当前时间调用语句
	 * @throws CommonException 异常
	 */
	String getCurrentTimestampCallString() throws CommonException;

	/**
	 * 是否支持分页查询
	 * @return 是否支持分页查询
	 */
	boolean supportsPageLimitOffset();

	/**
	 * 分⻚语句
	 * @param esql ESQL语句
	 * @param startRowNum 开始行
	 * @param perPageSize 每页大小
	 * @return 分页语句
	 * @throws CommonException 异常
	 */
	String getPageLimitString(String esql, int startRowNum, int perPageSize) throws CommonException;

	/**
	 * 是否过滤分页字段
	 * @return 是否过滤分页字段
	 */
	boolean isFilterPageFields();

	/**
	 * 过滤分页字段
	 * @return 过滤分页字段
	 */
	String[] getFilterPageFields();

	/**
	 * 是否支持序列
	 * @return 是否支持序列
	 */
	boolean supportsSequences();

	/**
	 * 创建序列语句
	 * @param sequenceName 序列名
	 * @return 创建序列语句
	 */
	String getCreateSequenceString(String sequenceName);

	/**
	 * 查询序列语句
	 * @return 查询序列语句
	 */
	String getQuerySequencesString();

	/**
	 * 查询下一个序列值语句
	 * @param sequenceName 序列名
	 * @return 查询下一个序列值语句
	 */
	String getSequenceNextValString(String sequenceName);

	/**
	 * 表类型
	 * @return 表类型
	 */
	String getTableTypeString();

	/**
	 * 是否为保留关键字
	 * @param str 内容
	 * @return 是否为保留关键字
	 */
	boolean isSQLKeyword(String str);

	/**
	 * 得到保留关键字
	 * @param str 内容
	 * @return 得到保留关键字
	 */
	String getSQLKeyword(String str);

	String getSQLKeyword(String str, boolean sqlKeyword);

	String getSQLKeywordMarks();

	/**
	 * 注册列数据类型
	 * @param dataType 数据类型
	 * @param name 名称
	 */
	void registerColumnType(DataType dataType, String name);

	/**
	 * 注册列数据类型
	 * @param dataType 数据类型
	 * @param capacity 容量
	 * @param name 名称
	 */
	void registerColumnType(DataType dataType, long capacity, String name);

	/**
	 * 得到类型名称
	 * @param dataType 数据类型
	 * @return 类型名称
	 * @throws CommonException 异常
	 */
	String getTypeName(DataType dataType) throws CommonException;

	/**
	 * 得到类型名称
	 * @param dataType 数据类型
	 * @param length 长度
	 * @param precision 精度
	 * @return 类型名称 
	 * @throws CommonException 异常
	 */
	String getTypeName(DataType dataType, long length, int precision) throws CommonException;

	/**
	 * 得到增加列的关键字
	 * @return 增加列的关键字
	 */
	String getAddColumnString();

	/**
	 * 得到修改列的关键字
	 * @return 修改列的关键字
	 */
	String getModifyColumnString();

	/**
	 * 得到增加主键的关键字
	 * @param constraintName 约束名
	 * @return 增加主键的关键字
	 */
	String getAddPrimaryKeyConstraintString(String constraintName);

	/**
	 * 得到删除主键的关键字
	 * @param constraintName 约束名
	 * @return 删除主键的关键字
	 */
	String getDropPrimaryKeyConstraintString(String constraintName);

	/**
	 * 得到删除索引语句
	 * @param tableName 表名
	 * @param indexName 索引名
	 * @return 删除索引语句
	 */
	String getDropIndexString(String tableName, String indexName);

	/**
	 * 得到行锁的关键字
	 * @return 行锁的关键字
	 */
	String getForUpdateString();

	/**
	 * 给指定表增加锁的关键字
	 * @param tableName 表名
	 * @return 给指定表增加锁的关键字
	 */
	String appendLock(String tableName);

	/**
	 * 得到指定表名的数据表结构
	 * @param conn 数据库连接
	 * @param tableName 表名
	 * @return 数据表结构
	 * @throws CommonException 异常
	 */
	TableObjectStruct getTableStruct(Connection conn, String tableName) throws CommonException;

	/**
	 * 是否存在指定表
	 * @param conn 数据库连接
	 * @param tableName 表名
	 * @return 是否存在指定表 
	 * @throws CommonException 异常
	 */
	boolean existTable(Connection conn, String tableName) throws CommonException;

	/**
	 * 得到指定视图名的视图结构
	 * @param conn 数据库连接
	 * @param viewName 视图名
	 * @return 视图结构
	 * @throws CommonException 异常
	 */
	ViewObjectStruct getViewStruct(Connection conn, String viewName) throws CommonException;

	/**
	 * 是否指定视图
	 * @return 是否存在指定视图
	 * @throws CommonException 异常
	 */
	boolean existView(Connection conn, String viewName) throws CommonException;

	/**
	 * 当前时间函数名
	 * @return 当前时间函数名
	 */
	String getCurrentTimestampSQLFunctionName();
}