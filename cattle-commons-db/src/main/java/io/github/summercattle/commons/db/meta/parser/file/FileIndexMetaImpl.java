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
package io.github.summercattle.commons.db.meta.parser.file;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import io.github.summercattle.commons.db.meta.file.FileIndexMeta;
import io.github.summercattle.commons.db.meta.impl.IndexMetaImpl;
import io.github.summercattle.commons.exception.CommonException;

public class FileIndexMetaImpl extends IndexMetaImpl implements FileIndexMeta {

	@Override
	public void from(Element indexElement) throws CommonException {
		name = indexElement.attributeValue("name").toUpperCase();
		String lFields = indexElement.attributeValue("fields");
		if (StringUtils.isNotBlank(lFields)) {
			String kField = "";
			String[] vFields = lFields.split(",");
			for (int i = 0; i < vFields.length; i++) {
				if (i > 0) {
					kField += ",";
				}
				String[] indexFields = vFields[i].split(":");
				if (indexFields.length == 0 || StringUtils.isBlank(indexFields[0])) {
					throw new CommonException("索引'" + name + "'的字段为空");
				}
				kField += indexFields[0].trim().toUpperCase();
				if (indexFields.length == 2 && StringUtils.isNotBlank(indexFields[1])) {
					String ascAndDesc = indexFields[1].trim();
					if (ascAndDesc.equalsIgnoreCase("asc") || ascAndDesc.equalsIgnoreCase("desc")) {
						kField += ":" + ascAndDesc.toLowerCase();
					}
					else {
						throw new CommonException("无效的升降序关键字:" + ascAndDesc);
					}
				}
				else {
					kField += ":asc";
				}
			}
			fields = kField;
		}
		String strUnique = indexElement.attributeValue("unique");
		unique = BooleanUtils.toBoolean(strUnique);
		if (StringUtils.isBlank(name)) {
			throw new CommonException("数据表索引名称为空");
		}
		if (StringUtils.isBlank(fields)) {
			throw new CommonException("数据表索引字段信息为空");
		}
	}
}