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
package io.github.summercattle.commons.db.configure;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.summercattle.commons.db.constants.DataConstants;

@ConfigurationProperties(prefix = DataConstants.PROPERTY_PREFIX)
public class DbProperties {

	private String primaryField;

	private String createTimeField;

	private String updateTimeField;

	private String versionField;

	private String deletedField;

	private boolean generate;

	private Map<String, String> cacheProps;

	public String getPrimaryField() {
		return null != primaryField ? primaryField.toUpperCase() : null;
	}

	public void setPrimaryField(String primaryField) {
		this.primaryField = primaryField;
	}

	public String getCreateTimeField() {
		return null != createTimeField ? createTimeField.toUpperCase() : null;
	}

	public void setCreateTimeField(String createTimeField) {
		this.createTimeField = createTimeField;
	}

	public String getUpdateTimeField() {
		return null != updateTimeField ? updateTimeField.toUpperCase() : null;
	}

	public void setUpdateTimeField(String updateTimeField) {
		this.updateTimeField = updateTimeField;
	}

	public String getVersionField() {
		return null != versionField ? versionField.toUpperCase() : null;
	}

	public void setVersionField(String versionField) {
		this.versionField = versionField;
	}

	public String getDeletedField() {
		return null != deletedField ? deletedField.toUpperCase() : null;
	}

	public void setDeletedField(String deletedField) {
		this.deletedField = deletedField;
	}

	public boolean isGenerate() {
		return generate;
	}

	public void setGenerate(boolean generate) {
		this.generate = generate;
	}

	public Map<String, String> getCacheProps() {
		return cacheProps;
	}

	public void setCacheProps(Map<String, String> cacheProps) {
		this.cacheProps = cacheProps;
	}
}