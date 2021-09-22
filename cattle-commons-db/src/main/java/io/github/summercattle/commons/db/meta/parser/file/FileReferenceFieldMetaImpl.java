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

import io.github.summercattle.commons.db.meta.file.FileReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.impl.ReferenceFieldMetaImpl;
import io.github.summercattle.commons.exception.CommonException;

public class FileReferenceFieldMetaImpl extends ReferenceFieldMetaImpl implements FileReferenceFieldMeta {

	@Override
	public void from(String name, Element fieldElement) throws CommonException {
		this.name = name;
		comment = fieldElement.attributeValue("comment", "");
		String strAllowNull = fieldElement.attributeValue("allowNull");
		allowNull = StringUtils.isNotBlank(strAllowNull) ? BooleanUtils.toBoolean(strAllowNull) : true;
		referenceTableName = fieldElement.attributeValue("referenceTableName");
		if (StringUtils.isBlank(referenceTableName)) {
			throw new CommonException("引用表名为空");
		}
		referenceTableName = referenceTableName.toUpperCase();
	}
}