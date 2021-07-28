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
package io.github.summercattle.commons.db.meta.parser.annotation;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.annotation.ReferenceField;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.impl.ReferenceFieldMetaImpl;
import io.github.summercattle.commons.exception.CommonException;

public class AnnotatedReferenceFieldMetaImpl extends ReferenceFieldMetaImpl implements AnnotatedReferenceFieldMeta {

	private String classFieldName;

	@Override
	public void from(ReferenceField field, String classFieldName) throws CommonException {
		if (StringUtils.isBlank(field.name())) {
			throw new CommonException("类字段名'" + classFieldName + "'的数据表字段名为空");
		}
		this.classFieldName = classFieldName;
		name = field.name().toUpperCase();
		comment = field.comment();
		allowNull = field.allowNull();
		referenceTableName = field.referenceTableName();
		if (StringUtils.isBlank(referenceTableName)) {
			throw new CommonException("引用表名为空");
		}
		referenceTableName = referenceTableName.toUpperCase();
	}

	@Override
	public String getClassFieldName() {
		return classFieldName;
	}
}