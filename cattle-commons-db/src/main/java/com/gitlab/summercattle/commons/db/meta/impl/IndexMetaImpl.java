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
package com.gitlab.summercattle.commons.db.meta.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.meta.IndexFieldMeta;
import com.gitlab.summercattle.commons.db.meta.IndexMeta;

public class IndexMetaImpl implements IndexMeta {

	private String hash;

	protected boolean unique;

	protected IndexFieldMeta[] fields;

	@Override
	public boolean isUnique() {
		return unique;
	}

	@Override
	public IndexFieldMeta[] getFields() {
		return fields;
	}

	@Override
	public String toString() {
		if (StringUtils.isBlank(hash)) {
			StringBuffer buf = new StringBuffer("Unique'" + BooleanUtils.toStringTrueFalse(unique) + "'");
			for (int i = 0; i < fields.length; i++) {
				IndexFieldMeta indexFieldMeta = fields[i];
				buf.append("Column'" + indexFieldMeta.getField() + "'Order'" + indexFieldMeta.getOrder() + "'");
			}
			hash = com.gitlab.summercattle.commons.utils.auxiliary.StringUtils.getHashName(buf.toString());
		}
		return hash;
	}
}