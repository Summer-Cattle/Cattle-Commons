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
package com.gitlab.summercattle.commons.db.datasource.impl;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.datasource.DataSourceHandler;
import com.gitlab.summercattle.commons.db.datasource.configure.DataSourceProperties;
import com.gitlab.summercattle.commons.db.datasource.utils.DataSourceUtils;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;

public class DefaultDataSourceHandler implements DataSourceHandler {

	@Override
	public DataSource getDataSource() throws CommonException {
		DataSourceProperties dataSourceProperties = SpringContext.getBean(DataSourceProperties.class);
		String driverClassName = dataSourceProperties.getDriverClassName();
		if (StringUtils.isBlank(driverClassName)) {
			throw new CommonException("数据源驱动程序类名为空");
		}
		return DataSourceUtils.getDataSource(driverClassName, dataSourceProperties.getDataSource(), 0);
	}
}