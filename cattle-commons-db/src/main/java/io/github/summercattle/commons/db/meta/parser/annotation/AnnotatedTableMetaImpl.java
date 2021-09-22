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

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.annotation.CreateTime;
import io.github.summercattle.commons.db.annotation.Deleted;
import io.github.summercattle.commons.db.annotation.FixedField;
import io.github.summercattle.commons.db.annotation.Index;
import io.github.summercattle.commons.db.annotation.Primary;
import io.github.summercattle.commons.db.annotation.ReferenceField;
import io.github.summercattle.commons.db.annotation.Table;
import io.github.summercattle.commons.db.annotation.UpdateTime;
import io.github.summercattle.commons.db.annotation.Version;
import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.meta.IndexFieldMeta;
import io.github.summercattle.commons.db.meta.TableMetaSource;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedFixedFieldMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedIndexMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedReferenceFieldMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedSystemFieldMeta;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedTableMeta;
import io.github.summercattle.commons.db.meta.impl.ReferenceFieldInfoImpl;
import io.github.summercattle.commons.db.meta.impl.TableMetaImpl;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class AnnotatedTableMetaImpl extends TableMetaImpl implements AnnotatedTableMeta {

	private final List<AnnotatedSystemFieldMeta> systemFields = new Vector<AnnotatedSystemFieldMeta>();

	private String classTypeName;

	private AnnotatedSystemFieldMeta primaryField;

	@Override
	public void from(DbProperties dbProperties, Table table, Class< ? > classType) throws CommonException {
		name = table.name();
		if (StringUtils.isBlank(name)) {
			throw new CommonException("类'" + classType.getName() + "'的数据表名为空");
		}
		name = name.toUpperCase();
		classTypeName = classType.getName();
		alias = table.alias();
		if (name.equalsIgnoreCase(alias)) {
			alias = null;
		}
		useCache = table.useCache();
		comment = table.comment();
		primaryKeyUseNumber = table.primaryKeyUseNumber();
		List<java.lang.reflect.Field> classFields = ReflectUtils.getFields(classType);
		for (java.lang.reflect.Field classField : classFields) {
			if (null != classField.getAnnotation(Primary.class)) {
				if (null != primaryField) {
					throw new CommonException("类'" + classType.getName() + "'中字段'" + classField.getName() + "'设置的系统缺省字段注解有重复");
				}
				primaryField = new AnnotatedSystemFieldMetaImpl(dbProperties.getPrimaryField(), classField.getName());
			}
			else if (null != classField.getAnnotation(CreateTime.class)) {
				addSystemField(classType, dbProperties.getCreateTimeField(), classField.getName());
			}
			else if (null != classField.getAnnotation(UpdateTime.class)) {
				addSystemField(classType, dbProperties.getUpdateTimeField(), classField.getName());
			}
			else if (null != classField.getAnnotation(Version.class)) {
				addSystemField(classType, dbProperties.getVersionField(), classField.getName());
			}
			else if (null != classField.getAnnotation(Deleted.class)) {
				addSystemField(classType, dbProperties.getDeletedField(), classField.getName());
			}
			else {
				FixedField fixedField = classField.getAnnotation(FixedField.class);
				ReferenceField referenceField = classField.getAnnotation(ReferenceField.class);
				if (null != fixedField && null != referenceField) {
					throw new CommonException("固定字段注解及引用字段注解不能同时使用");
				}
				if (null != fixedField) {
					String classFieldName = classField.getName();
					AnnotatedFixedFieldMeta annotatedField = new AnnotatedFixedFieldMetaImpl();
					annotatedField.from(fixedField, classFieldName);
					if (hasFieldName(annotatedField.getName())) {
						throw new CommonException("类'" + classType.getName() + "'中已经存在字段名'" + annotatedField.getName() + "'");
					}
					if (!checkFieldName(dbProperties, annotatedField.getName())) {
						throw new CommonException("类'" + classType.getName() + "'中字段名'" + annotatedField.getName() + "'是系统保留字段名");
					}
					fields.add(annotatedField);
				}
				if (null != referenceField) {
					String classFieldName = classField.getName();
					AnnotatedReferenceFieldMeta annotatedField = new AnnotatedReferenceFieldMetaImpl();
					annotatedField.from(referenceField, classFieldName);
					if (hasFieldName(annotatedField.getName())) {
						throw new CommonException("类'" + classType.getName() + "'中已经存在字段名'" + annotatedField.getName() + "'");
					}
					if (!checkFieldName(dbProperties, annotatedField.getName())) {
						throw new CommonException("类'" + classType.getName() + "'中字段名'" + annotatedField.getName() + "'是系统保留字段名");
					}
					referenceFieldInfos.add(new ReferenceFieldInfoImpl(annotatedField.getReferenceTableName(), annotatedField.getName()));
					fields.add(annotatedField);
				}
			}
		}
		if (fields.size() == 0) {
			throw new CommonException("类'" + classType.getName() + "'的数据表字段定义没有");
		}
		Index[] indexes = table.indexes();
		if (null != indexes && indexes.length > 0) {
			for (Index index : indexes) {
				if (null != index) {
					handleIndex(index, classType);
				}
			}
		}
	}

	@Override
	public AnnotatedSystemFieldMeta getPrimaryField() {
		return primaryField;
	}

	private void addSystemField(Class< ? > classType, String systemFieldName, String classFieldName) throws CommonException {
		if (hasSystemFieldName(systemFieldName)) {
			throw new CommonException("类'" + classType.getName() + "'中字段'" + classFieldName + "'设置的系统缺省字段注解有重复");
		}
		systemFields.add(new AnnotatedSystemFieldMetaImpl(systemFieldName, classFieldName));
	}

	private boolean hasSystemFieldName(String name) {
		boolean found = false;
		for (AnnotatedSystemFieldMeta systemField : systemFields) {
			if (systemField.getSystemFieldName().equalsIgnoreCase(name)) {
				found = true;
				break;
			}
		}
		return found;
	}

	private void handleIndex(Index index, Class< ? > classType) throws CommonException {
		AnnotatedIndexMeta indexMeta = new AnnotatedIndexMetaImpl();
		indexMeta.from(index);
		for (IndexFieldMeta indexFieldMeta : indexMeta.getFields()) {
			if (!fields.stream().anyMatch(p -> p.getName().equals(indexFieldMeta.getField()))) {
				throw new CommonException("表'" + name + "'不存在字段名'" + indexFieldMeta.getField() + "'");
			}
		}
		if (hasIndexHash(indexMeta.toString())) {
			throw new CommonException("类'" + classType.getName() + "'中已经存在数据表索引名'" + indexMeta.toString() + "'");
		}
		indexes.add(indexMeta);
	}

	@Override
	public AnnotatedSystemFieldMeta[] getSystemFields() {
		return systemFields.toArray(new AnnotatedSystemFieldMeta[0]);
	}

	@Override
	public String getClassTypeName() {
		return classTypeName;
	}

	@Override
	public TableMetaSource getSource() {
		return TableMetaSource.Annotated;
	}
}