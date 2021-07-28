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
package io.github.summercattle.commons.db.constants;

public enum DataType {

	/**
	 * Unicode文本
	 */
	NString,
	/**
	 * 文本
	 */
	String,
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
	 * Unicode大对象(文本)
	 */
	NClob,
	/**
	 * 大对象(文本)
	 */
	Clob,
	/**
	 * 大对象(二进制)
	 */
	Blob,
	/**
	 * 布尔值
	 */
	Boolean
}