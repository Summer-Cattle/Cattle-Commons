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

import java.util.List;
import java.util.Vector;

import org.apache.commons.codec.binary.StringUtils;

import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.meta.FieldMeta;
import io.github.summercattle.commons.db.meta.IndexMeta;
import io.github.summercattle.commons.db.meta.ReferenceFieldInfo;
import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.exception.CommonException;

public abstract class TableMetaImpl implements TableMeta {

	protected String name;

	protected String alias;

	protected boolean useCache;

	protected String comment;

	protected boolean primaryKeyUseNumber;

	protected final List<FieldMeta> fields = new Vector<FieldMeta>();

	protected final List<IndexMeta> indexes = new Vector<IndexMeta>();

	protected final List<ReferenceFieldInfo> referenceFieldInfos = new Vector<ReferenceFieldInfo>();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public boolean isUseCache() {
		return useCache;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public boolean isPrimaryKeyUseNumber() {
		return primaryKeyUseNumber;
	}

	@Override
	public FieldMeta[] getFields() {
		return fields.toArray(new FieldMeta[0]);
	}

	@Override
	public IndexMeta[] getIndexes() {
		return indexes.toArray(new IndexMeta[0]);
	}

	@Override
	public FieldMeta getField(String name) throws CommonException {
		FieldMeta fieldMeta = null;
		for (FieldMeta field : fields) {
			if (field.getName().equalsIgnoreCase(name)) {
				fieldMeta = field;
				break;
			}
		}
		if (null == fieldMeta) {
			throw new CommonException("数据表定义名'" + this.name + "'不存在字段定义名'" + name + "'");
		}
		return fieldMeta;
	}

	protected boolean hasFieldName(String name) {
		boolean found = false;
		for (FieldMeta field : fields) {
			if (field.getName().equalsIgnoreCase(name)) {
				found = true;
				break;
			}
		}
		return found;
	}

	protected boolean hasIndexHash(String hash) {
		return indexes.stream().anyMatch(p -> p.toString().equals(hash));
	}

	protected boolean checkFieldName(DbProperties dbProperties, String fieldName) {
		return !(StringUtils.equals(dbProperties.getPrimaryField() != null ? dbProperties.getPrimaryField().toUpperCase() : null,
				fieldName.toUpperCase())
				|| StringUtils.equals(dbProperties.getCreateTimeField() != null ? dbProperties.getCreateTimeField().toUpperCase() : null,
						fieldName.toUpperCase())
				|| StringUtils.equals(dbProperties.getUpdateTimeField() != null ? dbProperties.getUpdateTimeField().toUpperCase() : null,
						fieldName.toUpperCase())
				|| StringUtils.equals(dbProperties.getVersionField() != null ? dbProperties.getVersionField().toUpperCase() : null,
						fieldName.toUpperCase())
				|| StringUtils.equals(dbProperties.getDeletedField() != null ? dbProperties.getDeletedField().toUpperCase() : null,
						fieldName.toUpperCase()));
	}

	@Override
	public List<ReferenceFieldInfo> getReferenceFieldInfos() throws CommonException {
		return referenceFieldInfos;
	}
}