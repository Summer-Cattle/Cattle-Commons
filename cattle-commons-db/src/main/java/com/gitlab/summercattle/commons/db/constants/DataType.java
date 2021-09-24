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
package com.gitlab.summercattle.commons.db.constants;

public enum DataType {

	/**
	 * 文本
	 */
	String,
	/**
	 * Unicode文本
	 */
	NString,
	/**
	 * 长文本
	 */
	LongString,
	/**
	 * 二进制
	 */
	Binary,
	/**
	 * 长二进制
	 */
	LongBinary,
	/**
	 * 数值
	 */
	Number,
	/**
	 * 日期
	 */
	Date,
	/**
	 * 时间
	 */
	Time,
	/**
	 * 日期(时间戳)
	 */
	Timestamp,
	/**
	 * 大对象(文本)
	 */
	Clob,
	/**
	 * Unicode大对象(文本)
	 */
	NClob,
	/**
	 * 大对象(二进制)
	 */
	Blob,
	/**
	 * 布尔值
	 */
	Boolean,
	/**
	 * 双精度浮点
	 */
	Double
}