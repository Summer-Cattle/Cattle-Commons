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

import io.github.summercattle.commons.db.constants.DataType;

public interface FixedFieldMeta extends FieldMeta {

	@Override
	default FieldMetaMode getMode() {
		return FieldMetaMode.Fixed;
	}

	/**
	 * 数据类型
	 * @return 数据类型
	 */
	DataType getType();

	/**
	 * 长度
	 * @return 长度
	 */
	int getLength();

	/**
	 * 精度	
	 * @return 精度
	 */
	int getScale();

	/**
	 * 缺省值
	 * @return 缺省值
	 */
	String getDefault();
}