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
package io.github.summercattle.commons.db.meta.impl;

import io.github.summercattle.commons.db.meta.IndexMeta;

public class IndexMetaImpl implements IndexMeta {

	protected String name;

	protected boolean unique;

	protected String fields;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isUnique() {
		return unique;
	}

	@Override
	public String getFields() {
		return fields;
	}
}