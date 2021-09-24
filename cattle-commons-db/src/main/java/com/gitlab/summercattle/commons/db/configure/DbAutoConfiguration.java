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
package com.gitlab.summercattle.commons.db.configure;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.dialect.DialectFactory;
import com.gitlab.summercattle.commons.db.runner.DbStartupRunner;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@PropertySource("classpath:/com/gitlab/summercattle/commons/db/configure/db.properties")
@EnableConfigurationProperties({ DbProperties.class })
@ComponentScan(basePackageClasses = DbStartupRunner.class)
public class DbAutoConfiguration {

	@Bean
	@DependsOn("dataSource")
	public Dialect dialect(DataSource dataSource) {
		Connection conn = null;
		try {
			conn = DataSourceUtils.getConnection(dataSource);
			return DialectFactory.buildDialect(conn);
		}
		catch (CommonException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}
}