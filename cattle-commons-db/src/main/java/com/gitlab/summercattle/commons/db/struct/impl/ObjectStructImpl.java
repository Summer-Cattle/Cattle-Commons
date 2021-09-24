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

import com.gitlab.summercattle.commons.db.struct.FieldStruct;
import com.gitlab.summercattle.commons.db.struct.ObjectStruct;

public abstract class ObjectStructImpl implements ObjectStruct {

	private String name;

	protected ConcurrentMap<String, FieldStruct> fields;

	public ObjectStructImpl(String name, ConcurrentMap<String, FieldStruct> fields) {
		this.name = name;
		this.fields = fields;
	}

	@Override
	public String getName() {
		return name;
	}
}