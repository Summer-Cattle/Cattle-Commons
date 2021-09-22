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
package io.github.summercattle.commons.db.object;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据结果集
 */
public interface DataTable extends DataQuery {

	/**
	 * 表定义名
	 * @return 表定义名
	 */
	String getName();
	
	/**
	 * 表定义别名
	 * @return 表定义别名
	 */
	String getAlias();

	/**
	 * 插入记录
	 * @throws CommonException 异常
	 */
	void insert() throws CommonException;

	/**
	 * 插入记录
	 * @param primaryValue 指定主键值
	 * @throws CommonException 异常
	 */
	void insert(Object primaryValue) throws CommonException;

	/**
	 * 删除当前记录
	 * @throws CommonException 异常
	 */
	void delete() throws CommonException;

	/**
	 * 得到主键值
	 * @return 主键值
	 * @throws CommonException 异常
	 */
	Object getPrimaryValue() throws CommonException;

	/**
	 * 设置长整数值
	 * @param field 字段
	 * @param value 长整数值
	 * @throws CommonException 异常
	 */
	void setLong(String field, long value) throws CommonException;

	/**
	 * 设置长整数值
	 * @param fieldIndex 字段序号
	 * @param value 长整数值
	 * @throws CommonException 异常
	 */
	void setLong(int fieldIndex, long value) throws CommonException;

	/**
	 * 设置整数值
	 * @param field 字段
	 * @param value 整数值
	 * @throws CommonException 异常
	 */
	void setInt(String field, int value) throws CommonException;

	/**
	 * 设置整数值
	 * @param fieldIndex 字段序号
	 * @param value 整数值
	 * @throws CommonException 异常
	 */
	void setInt(int fieldIndex, int value) throws CommonException;

	/**
	 * 设置数值
	 * @param field 字段
	 * @param value 数值
	 * @throws CommonException 异常
	 */
	void setBigDecimal(String field, BigDecimal value) throws CommonException;

	/**
	 * 设置数值
	 * @param fieldIndex 字段序号
	 * @param value 数值
	 * @throws CommonException 异常
	 */
	void setBigDecimal(int fieldIndex, BigDecimal value) throws CommonException;

	/**
	 * 设置双精度浮点数
	 * @param field 字段
	 * @param value 双精度浮点数
	 * @throws CommonException 异常
	 */
	void setDouble(String field, double value) throws CommonException;

	/**
	 * 设置双精度浮点数
	 * @param fieldIndex 字段序号
	 * @param value 双精度浮点数
	 * @throws CommonException 异常
	 */
	void setDouble(int fieldIndex, double value) throws CommonException;

	/**
	 * 设置字符串
	 * @param field 字段
	 * @param value 字符串
	 * @throws CommonException 异常
	 */
	void setString(String field, String value) throws CommonException;

	/**
	 * 设置字符串
	 * @param fieldIndex 字段序号
	 * @param value 字符串
	 * @throws CommonException 异常
	 */
	void setString(int fieldIndex, String value) throws CommonException;

	/**
	 * 设置对象
	 * @param field 字段
	 * @param value 对象
	 * @throws CommonException 异常
	 */
	void setObject(String field, Object value) throws CommonException;

	/**
	 * 设置对象
	 * @param fieldIndex 字段序号
	 * @param value 对象
	 * @throws CommonException 异常
	 */
	void setObject(int fieldIndex, Object value) throws CommonException;

	/**
	 * 设置时间戳
	 * @param field 字段
	 * @param value 时间戳
	 * @throws CommonException 异常
	 */
	void setTimestamp(String field, Timestamp value) throws CommonException;

	/**
	 * 设置时间戳
	 * @param fieldIndex 字段序号
	 * @param value 时间戳
	 * @throws CommonException 异常
	 */
	void setTimestamp(int fieldIndex, Timestamp value) throws CommonException;

	/**
	 * 设置日期
	 * @param field 字段
	 * @param value 日期
	 * @throws CommonException 异常
	 */
	void setDate(String field, Date value) throws CommonException;

	/**
	 * 设置日期
	 * @param fieldIndex 字段序号
	 * @param value 日期
	 * @throws CommonException 异常
	 */
	void setDate(int fieldIndex, Date value) throws CommonException;

	/**
	 * 设置布尔值
	 * @param field 字段
	 * @param value 布尔值
	 * @throws CommonException 异常
	 */
	void setBoolean(String field, boolean value) throws CommonException;

	/**
	 * 设置布尔值
	 * @param fieldIndex 字段序号
	 * @param value 布尔值
	 * @throws CommonException 异常
	 */
	void setBoolean(int fieldIndex, boolean value) throws CommonException;

	/**
	 * 设置字节
	 * @param field 字段
	 * @param value 字节
	 * @throws CommonException 异常
	 */
	void setBytes(String field, byte[] value) throws CommonException;

	/**
	 * 设置字节
	 * @param fieldIndex 字段序号
	 * @param value 字节
	 * @throws CommonException 异常
	 */
	void setBytes(int fieldIndex, byte[] value) throws CommonException;

	/**
	 * 得到数据版本
	 * @return 数据版本
	 * @throws CommonException 异常
	 */
	long getVersion() throws CommonException;

	/**
	 * 是否删除
	 * @return 是否删除
	 * @throws CommonException 异常
	 */
	boolean isDeleted() throws CommonException;

	/**
	 * 得到创建时间
	 * @return 创建时间
	 * @throws CommonException 异常
	 */
	Date getCreateDate() throws CommonException;

	/**
	 * 得到最后修改时间
	 * @return 最后修改时间
	 * @throws CommonException 异常
	 */
	Date getUpdateDate() throws CommonException;
}