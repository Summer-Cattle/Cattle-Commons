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
package com.gitlab.summercattle.commons.db.datasource.configure;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.gitlab.summercattle.commons.db.datasource.DataSourceHandler;
import com.gitlab.summercattle.commons.db.datasource.druid.DruidDataSourceAutoConfiguration;
import com.gitlab.summercattle.commons.exception.CommonRuntimeException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.reflect.ClassUtils;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@EnableConfigurationProperties(DataSourceProperties.class)
@Import(DruidDataSourceAutoConfiguration.class)
public class DbDataSourceAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(DbDataSourceAutoConfiguration.class);

	@Bean(name = "dataSource")
	public DataSource dataSource() {
		Class<DataSourceHandler> dataSourceHandlerClass = ClassUtils.getSubTypesOfByLoadLevel(DataSourceHandler.class);
		if (null == dataSourceHandlerClass) {
			throw new CommonRuntimeException("????????????????????????");
		}
		try {
			DataSourceHandler dataSourceHandler = ClassUtils.instance(dataSourceHandlerClass);
			DataSource dataSource = dataSourceHandler.getDataSource();
			if (null == dataSource) {
				throw new CommonRuntimeException("???????????????");
			}
			return dataSource;
		}
		catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}
}