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

/**
 * 字段定义信息
 */
public interface FieldMeta {

	/**
	 * 字段名
	 * @return 字段名
	 */
	String getName();

	/**
	 * 备注
	 * @return 备注
	 */
	String getComment();

	/**
	 * 允许为空
	 * @return 允许为空
	 */
	boolean allowNull();

	/**
	 * 模式
	 * @return 模式
	 */
	default FieldMetaMode getMode() {
		return null;
	}
}