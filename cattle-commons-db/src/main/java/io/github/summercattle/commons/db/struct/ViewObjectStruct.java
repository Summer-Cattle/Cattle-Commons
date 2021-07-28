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

import io.github.summercattle.commons.db.constants.ObjectStructType;
import io.github.summercattle.commons.exception.CommonException;

/**
 * 视图的对象结构
 */
public interface ViewObjectStruct extends ObjectStruct {

	@Override
	default ObjectStructType getType() {
		return ObjectStructType.View;
	}

	/**
	 * 所有字段的信息
	 * @return 所有字段的信息
	 */
	FieldStruct[] getFields();

	/**
	 * 指定字段的信息
	 * @param fieldName 字段名
	 * @return 字段的信息
	 * @throws CommonException 异常
	 */
	FieldStruct getField(String fieldName) throws CommonException;

	/**
	 * 得到视图定义
	 * @return 视图定义
	 */
	String getDefinition();
}