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
package com.gitlab.summercattle.commons.db.struct.impl;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.struct.FieldStruct;
import com.gitlab.summercattle.commons.db.struct.TableFieldStruct;
import com.gitlab.summercattle.commons.db.struct.TableIndexStruct;
import com.gitlab.summercattle.commons.db.struct.TableObjectStruct;
import com.gitlab.summercattle.commons.db.struct.TablePrimaryKeyStruct;
import com.gitlab.summercattle.commons.exception.CommonException;

public class TableObjectStructImpl extends ObjectStructImpl implements TableObjectStruct {

	private TablePrimaryKeyStruct primaryKey;

	private ConcurrentMap<String, TableIndexStruct> indexes;

	private String comment;

	public TableObjectStructImpl(String name, String comment, ConcurrentMap<String, FieldStruct> fields, TablePrimaryKeyStruct primaryKey,
			ConcurrentMap<String, TableIndexStruct> indexes) {
		super(name, fields);
		this.comment = comment;
		this.primaryKey = primaryKey;
		this.indexes = indexes;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public TablePrimaryKeyStruct getPrimaryKey() {
		return primaryKey;
	}

	@Override
	public TableIndexStruct[] getIndexes() {
		return indexes.values().toArray(new TableIndexStructImpl[0]);
	}

	@Override
	public TableIndexStruct getIndex(String indexName) throws CommonException {
		if (StringUtils.isBlank(indexName)) {
			throw new CommonException("索引名为空");
		}
		return indexes.get(indexName.toUpperCase());
	}

	@Override
	public TableFieldStruct[] getFields() {
		return fields.values().toArray(new TableFieldStruct[0]);
	}

	@Override
	public TableFieldStruct getField(String fieldName) throws CommonException {
		if (StringUtils.isBlank(fieldName)) {
			throw new CommonException("字段名为空");
		}
		return (TableFieldStruct) fields.get(fieldName.toUpperCase());
	}
}