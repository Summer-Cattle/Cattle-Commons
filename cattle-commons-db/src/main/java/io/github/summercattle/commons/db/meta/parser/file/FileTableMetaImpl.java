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

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.meta.FieldMeta;
import io.github.summercattle.commons.db.meta.FieldMetaMode;
import io.github.summercattle.commons.db.meta.TableMetaSource;
import io.github.summercattle.commons.db.meta.file.FileFixedFieldMeta;
import io.github.summercattle.commons.db.meta.file.FileIndexMeta;
import io.github.summercattle.commons.db.meta.file.FileReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.file.FileTableMeta;
import io.github.summercattle.commons.db.meta.impl.TableMetaImpl;
import io.github.summercattle.commons.exception.CommonException;

public class FileTableMetaImpl extends TableMetaImpl implements FileTableMeta {

	@Override
	public void from(DbProperties dbProperties, Element tableElement) throws CommonException {
		name = tableElement.attributeValue("name");
		if (StringUtils.isBlank(name)) {
			throw new CommonException("数据表名为空");
		}
		name = name.toUpperCase();
		alias = tableElement.attributeValue("alias");
		if (name.equalsIgnoreCase(alias)) {
			alias = null;
		}
		useCache = BooleanUtils.toBoolean(tableElement.attributeValue("useCache"));
		comment = tableElement.attributeValue("comment");
		primaryKeyUseNumber = BooleanUtils.toBoolean(tableElement.attributeValue("primaryKeyUseNumber"));
		primaryKeyName = tableElement.attributeValue("primaryKeyName");
		if (StringUtils.isBlank(primaryKeyName)) {
			primaryKeyName = "PK_" + name;
		}
		primaryKeyName = primaryKeyName.toUpperCase();
		Element fieldsElement = tableElement.element("Fields");
		if (fieldsElement != null) {
			List<Element> fieldElements = fieldsElement.elements("Field");
			for (Element fieldElement : fieldElements) {
				String fieldName = fieldElement.attributeValue("name");
				if (StringUtils.isBlank(fieldName)) {
					throw new CommonException("数据表表'" + name + "'字段名为空");
				}
				fieldName = fieldName.toUpperCase();
				FieldMetaMode fieldMode = FieldMetaMode.Fixed;
				String strFieldMode = fieldElement.attributeValue("mode");
				if (StringUtils.isNotBlank(strFieldMode)) {
					fieldMode = FieldMetaMode.valueOf(strFieldMode);
				}
				FieldMeta fieldMeta;
				if (FieldMetaMode.Fixed == fieldMode) {
					fieldMeta = new FileFixedFieldMetaImpl();
					((FileFixedFieldMeta) fieldMeta).from(fieldName, fieldElement);
				}
				else if (FieldMetaMode.Reference == fieldMode) {
					fieldMeta = new FileReferenceFieldMetaImpl();
					((FileReferenceFieldMeta) fieldMeta).from(fieldName, fieldElement);
				}
				else {
					throw new CommonException("数据表表'" + name + "'字段名'" + fieldName + "'的未知模式'" + fieldMode.toString() + "'");
				}
				if (hasFieldName(fieldMeta.getName())) {
					throw new CommonException("数据表定名'" + name + "'已经存在字段名'" + fieldMeta.getName() + "'");
				}
				if (!checkFieldName(dbProperties, fieldMeta.getName())) {
					throw new CommonException("数据表定义名'" + name + "'中字段名'" + fieldMeta.getName() + "'是系统保留字段名");
				}
				fields.add(fieldMeta);
			}
		}
		if (fields.size() == 0) {
			throw new CommonException("数据表字段定义没有");
		}
		Element indexesElement = tableElement.element("Indexes");
		if (indexesElement != null) {
			List<Element> indexElements = indexesElement.elements("Index");
			for (Element indexElement : indexElements) {
				FileIndexMeta index = new FileIndexMetaImpl();
				index.from(indexElement);
				if (hasIndexName(index.getName())) {
					throw new CommonException("已经存在数据表索引名'" + index.getName() + "'");
				}
				indexes.add(index);
			}
		}
	}

	@Override
	public TableMetaSource getSource() {
		return TableMetaSource.File;
	}
}