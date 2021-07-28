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
package io.github.summercattle.commons.db.datasource.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.db.DbUtils;
import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.datasource.DataSourceHandler;
import io.github.summercattle.commons.db.datasource.configure.DataSourceProperties;
import io.github.summercattle.commons.db.datasource.utils.DataSourceUtils;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.spring.SpringContext;

public class DefaultDataSourceHandler implements DataSourceHandler {

	@Override
	public DataSource getDataSource() throws CommonException {
		DataSourceProperties dataSourceProperties = SpringContext.getBean(DataSourceProperties.class);
		String driverClassName = dataSourceProperties.getDriverClassName();
		if (StringUtils.isBlank(driverClassName)) {
			throw new CommonException("数据源驱动程序类名为空");
		}
		DataSource dataSource = DataSourceUtils.getDataSource(driverClassName, dataSourceProperties.getDataSource(), 0);
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		if (dbProperties.isGenerate()) {
			try (Connection conn = dataSource.getConnection()) {
				DbUtils.getDbStruct().checkTablesAndIndexes(dbProperties, conn);
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
		}
		return dataSource;
	}
}