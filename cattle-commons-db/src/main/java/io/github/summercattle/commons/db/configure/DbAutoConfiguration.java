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

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import io.github.summercattle.commons.db.datasource.DataSourceHandler;
import io.github.summercattle.commons.db.datasource.configure.DataSourceProperties;
import io.github.summercattle.commons.db.datasource.druid.DruidDataSourceAutoConfiguration;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.dialect.DialectFactory;
import io.github.summercattle.commons.db.runner.DbStartupRunner;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassUtils;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@PropertySource("classpath:/io/github/summercattle/commons/db/configure/db.properties")
@EnableConfigurationProperties({ DbProperties.class, DataSourceProperties.class })
@Import(DruidDataSourceAutoConfiguration.class)
@ComponentScan(basePackageClasses = DbStartupRunner.class)
public class DbAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(DbAutoConfiguration.class);

	@Bean(name = "dataSource")
	public DataSource dataSource() {
		Class<DataSourceHandler> dataSourceHandlerClass = ClassUtils.getSubTypesOfByLoadLevel(DataSourceHandler.class);
		if (null == dataSourceHandlerClass) {
			throw new CommonRuntimeException("数据源处理类为空");
		}
		try {
			DataSourceHandler dataSourceHandler = ClassUtils.instance(dataSourceHandlerClass);
			DataSource dataSource = dataSourceHandler.getDataSource();
			if (null == dataSource) {
				throw new CommonRuntimeException("数据源为空");
			}
			return dataSource;
		}
		catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Bean
	@DependsOn("dataSource")
	public Dialect dialect(DataSource dataSource) {
		Connection conn = null;
		try {
			conn = DataSourceUtils.getConnection(dataSource);
			return DialectFactory.getDialect(conn);
		}
		catch (CommonException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}
}