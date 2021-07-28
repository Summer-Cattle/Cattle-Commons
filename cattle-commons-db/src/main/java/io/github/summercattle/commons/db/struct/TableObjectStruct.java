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
 * 数据表的对象结构
 */
public interface TableObjectStruct extends ObjectStruct {

	@Override
	default ObjectStructType getType() {
		return ObjectStructType.Table;
	}

	/**
	 * 表的注释
	 * @return 表的注释
	 */
	String getComment();

	/**
	 * 主键信息
	 * @return 主键信息
	 */
	TablePrimaryKeyStruct getPrimaryKey();

	/**
	 * 所有索引的信息
	 * @return 所有索引的信息
	 */
	TableIndexStruct[] getIndexes();

	/**
	 * 指定索引的信息
	 * @param indexName 索引名
	 * @return 索引的信息
	 * @throws CommonException 异常
	 */
	TableIndexStruct getIndex(String indexName) throws CommonException;

	/**
	 * 所有字段的信息
	 * @return 所有字段的信息
	 */
	TableFieldStruct[] getFields();

	/**
	 * 指定字段的信息
	 * @param fieldName 字段名
	 * @return 字段的信息
	 * @throws CommonException 异常
	 */
	TableFieldStruct getField(String fieldName) throws CommonException;
}