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
package io.github.summercattle.commons.db.struct;

/**
 * 字段结构
 */
public interface FieldStruct {

	/**
	 * 字段名
	 * @return 字段名
	 */
	String getName();

	/**
	 * Jdbc数据类型
	 * @return Jdbc数据类型
	 */
	int getJdbcType();

	/**
	 * 类型名称
	 * @return 类型名称
	 */
	String getTypeName();

	/**
	 * 长度
	 * @return 长度
	 */
	long getLength();

	/**
	 * 精度
	 * @return 精度
	 */
	int getScale();
}