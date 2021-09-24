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
package com.gitlab.summercattle.commons.db.meta.parser.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.annotation.Index;
import com.gitlab.summercattle.commons.db.meta.IndexFieldMeta;
import com.gitlab.summercattle.commons.db.meta.annotation.AnnotatedIndexMeta;
import com.gitlab.summercattle.commons.db.meta.impl.IndexMetaImpl;
import com.gitlab.summercattle.commons.exception.CommonException;

public class AnnotatedIndexMetaImpl extends IndexMetaImpl implements AnnotatedIndexMeta {

	@Override
	public void from(Index index) throws CommonException {
		String lFields = index.fields();
		unique = index.unique();
		if (StringUtils.isNotBlank(lFields)) {
			String[] vFields = lFields.split(",");
			List<IndexFieldMeta> indexFieldMetas = new ArrayList<IndexFieldMeta>(vFields.length);
			for (int i = 0; i < vFields.length; i++) {
				String[] indexFields = vFields[i].split(":");
				if (indexFields.length == 0 || StringUtils.isBlank(indexFields[0])) {
					throw new CommonException("索引字段名为空");
				}
				String field = indexFields[0].trim().toUpperCase();
				String order = "asc";
				if (indexFields.length == 2 && StringUtils.isNotBlank(indexFields[1])) {
					String ascAndDesc = indexFields[1].trim();
					if (ascAndDesc.equalsIgnoreCase("asc") || ascAndDesc.equalsIgnoreCase("desc")) {
						order = ascAndDesc.toLowerCase();
					}
					else {
						throw new CommonException("无效的升降序关键字:" + ascAndDesc);
					}
				}
				if (!indexFieldMetas.stream().anyMatch(p -> p.getField().equals(field.trim().toUpperCase()))) {
					indexFieldMetas.add(new IndexFieldMeta(field, order));
				}
			}
			fields = indexFieldMetas.toArray(new IndexFieldMeta[0]);
		}
		if (null == fields || fields.length == 0) {
			throw new CommonException("数据表索引字段信息为空");
		}
	}
}