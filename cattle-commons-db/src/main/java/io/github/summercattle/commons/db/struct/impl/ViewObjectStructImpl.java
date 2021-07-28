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
package io.github.summercattle.commons.db.struct.impl;

import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.struct.FieldStruct;
import io.github.summercattle.commons.db.struct.ViewObjectStruct;
import io.github.summercattle.commons.exception.CommonException;

public class ViewObjectStructImpl extends ObjectStructImpl implements ViewObjectStruct {

	private String definition;

	public ViewObjectStructImpl(String name, String definition, ConcurrentMap<String, FieldStruct> fields) {
		super(name, fields);
		this.definition = definition;
	}

	public FieldStruct[] getFields() {
		return fields.values().toArray(new FieldStruct[0]);
	}

	public FieldStruct getField(String fieldName) throws CommonException {
		if (StringUtils.isBlank(fieldName)) {
			throw new CommonException("字段名为空");
		}
		return fields.get(fieldName.toUpperCase());
	}

	@Override
	public String getDefinition() {
		return definition;
	}
}