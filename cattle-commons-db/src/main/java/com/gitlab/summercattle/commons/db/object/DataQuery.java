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
package com.gitlab.summercattle.commons.db.object;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import com.gitlab.summercattle.commons.exception.CommonException;

/**
 * 数据结果集
 */
public interface DataQuery {

	/**
	 * 所有字段名
	 * @return 所有字段名
	 */
	String[] getFieldNames();

	/**
	 * 移到第一条记录之前
	 */
	void beforeFirst();

	/**
	 * 移到第一条记录
	 * @return 是否成功
	 */
	boolean first();

	/**
	 * 移到最后一条记录
	 * @return 是否成功
	 */
	boolean last();

	/**
	 * 移到下一条记录
	 * @return 是否成功
	 */
	boolean next();

	/**
	 * 移到指定行号的记录
	 * @param row 行号
	 * @return 是否成功
	 */
	boolean absolute(int row);

	/**
	 * 记录数
	 * @return 记录数
	 */
	int size();

	/**
	 * 是否日期字段
	 * @param field 字段
	 * @return 是否日期字段
	 * @throws CommonException 异常
	 */
	boolean isDateTypeField(String field) throws CommonException;

	/**
	 * 是否日期字段
	 * @param fieldIndex 字段序号
	 * @return 是否日期字段
	 * @throws CommonException 异常
	 */
	boolean isDateTypeField(int fieldIndex) throws CommonException;

	/**
	 * 得到长整数值
	 * @param field 字段
	 * @return 长整数值
	 * @throws CommonException 异常
	 */
	long getLong(String field) throws CommonException;

	/**
	 * 得到长整数值
	 * @param fieldIndex 字段序号
	 * @return 长整数值
	 * @throws CommonException 异常
	 */
	long getLong(int fieldIndex) throws CommonException;

	/**
	 * 得到整数值
	 * @param field 字段
	 * @return 整数值
	 * @throws CommonException 异常
	 */
	int getInt(String field) throws CommonException;

	/**
	 * 得到整数值
	 * @param fieldIndex 字段序号
	 * @return 整数值
	 * @throws CommonException 异常
	 */
	int getInt(int fieldIndex) throws CommonException;

	/**
	 * 得到数值
	 * @param field 字段
	 * @return 数值
	 * @throws CommonException 异常
	 */
	BigDecimal getBigDecimal(String field) throws CommonException;

	/**
	 * 得到数值
	 * @param fieldIndex 字段序号
	 * @return 数值
	 * @throws CommonException 异常
	 */
	BigDecimal getBigDecimal(int fieldIndex) throws CommonException;

	/**
	 * 得到双精度浮点数
	 * @param field 字段
	 * @return 双精度浮点数
	 * @throws CommonException 异常
	 */
	double getDouble(String field) throws CommonException;

	/**
	 * 得到双精度浮点数
	 * @param fieldIndex 字段序号
	 * @return 双精度浮点数
	 * @throws CommonException 异常
	 */
	double getDouble(int fieldIndex) throws CommonException;

	/**
	 * 得到字符串
	 * @param field 字段
	 * @return 字符串
	 * @throws CommonException 异常
	 */
	String getString(String field) throws CommonException;

	/**
	 * 得到字符串
	 * @param fieldIndex 字段序号
	 * @return 字符串
	 * @throws CommonException 异常
	 */
	String getString(int fieldIndex) throws CommonException;

	/**
	 * 得到对象
	 * @param field 字段
	 * @return 对象
	 * @throws CommonException 异常
	 */
	Object getObject(String field) throws CommonException;

	/**
	 * 得到对象
	 * @param fieldIndex 字段序号
	 * @return 对象
	 * @throws CommonException 异常
	 */
	Object getObject(int fieldIndex) throws CommonException;

	/**
	 * 得到字符串
	 * @param field 字段
	 * @return 字符串
	 * @throws CommonException 异常
	 */
	String toString(String field) throws CommonException;

	/**
	 * 得到字符串
	 * @param fieldIndex 字段序号
	 * @return 字符串
	 * @throws CommonException 异常
	 */
	String toString(int fieldIndex) throws CommonException;

	/**
	 * 得到时间戳
	 * @param field 字段
	 * @return 时间戳
	 * @throws CommonException 异常
	 */
	Timestamp getTimestamp(String field) throws CommonException;

	/**
	 * 得到时间戳
	 * @param fieldIndex 字段序号
	 * @return 时间戳
	 * @throws CommonException 异常
	 */
	Timestamp getTimestamp(int fieldIndex) throws CommonException;

	/**
	 * 得到日期
	 * @param field 字段
	 * @return 日期
	 * @throws CommonException 异常
	 */
	Date getDate(String field) throws CommonException;

	/**
	 * 得到日期
	 * @param fieldIndex 字段序号
	 * @return 日期
	 * @throws CommonException 异常
	 */
	Date getDate(int fieldIndex) throws CommonException;

	/**
	 * 得到布尔值
	 * @param field 字段
	 * @return 布尔值
	 * @throws CommonException 异常
	 */
	boolean getBoolean(String field) throws CommonException;

	/**
	 * 得到布尔值
	 * @param fieldIndex 字段序号
	 * @return 布尔值
	 * @throws CommonException 异常
	 */
	boolean getBoolean(int fieldIndex) throws CommonException;

	/**
	 * 得到字节
	 * @param field 字段
	 * @return 字节
	 * @throws CommonException 异常
	 */
	byte[] getBytes(String field) throws CommonException;

	/**
	 * 得到字节
	 * @param fieldIndex 字段序号
	 * @return 字节
	 * @throws CommonException 异常
	 */
	byte[] getBytes(int fieldIndex) throws CommonException;
}