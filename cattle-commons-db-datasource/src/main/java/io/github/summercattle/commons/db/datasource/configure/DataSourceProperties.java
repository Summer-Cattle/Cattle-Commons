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
package io.github.summercattle.commons.db.datasource.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.summercattle.commons.db.datasource.constants.DataSourceConstants;

@ConfigurationProperties(DataSourceConstants.PROPERTY_PREFIX)
public class DataSourceProperties {

	private String driverClassName;

	private DataSourceInfo dataSource;

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public DataSourceInfo getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSourceInfo dataSource) {
		this.dataSource = dataSource;
	}
}