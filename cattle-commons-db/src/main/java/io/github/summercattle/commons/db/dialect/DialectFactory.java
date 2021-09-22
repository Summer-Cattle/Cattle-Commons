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
package io.github.summercattle.commons.db.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.db.constants.Database;
import io.github.summercattle.commons.db.dialect.impl.DialectResolutionInfoImpl;
import io.github.summercattle.commons.db.utils.JdbcUtils;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class DialectFactory {

	private static final Logger logger = LoggerFactory.getLogger(DialectFactory.class);

	public static Dialect buildDialect(Connection conn) throws CommonException {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			DialectResolutionInfo info = new DialectResolutionInfoImpl(metaData);
			logger.debug("RDBMS:" + info.getDatabaseName() + ",version:" + info.getDatabaseVersion());
			logger.debug("JDBC driver:" + info.getDriverName() + ",version:" + info.getDriverVersion());
			return determineDialect(conn, info);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private static Dialect determineDialect(Connection conn, DialectResolutionInfo info) throws CommonException {
		for (Database database : Database.values()) {
			Dialect dialect = database.resolveDialect(info);
			if (null != dialect) {
				String sql = dialect.getCurrentSchemaCommand();
				if (StringUtils.isNotBlank(sql)) {
					String schema = (String) JdbcUtils.getSingleValue(conn, sql);
					if (StringUtils.isNotBlank(schema)) {
						ReflectUtils.setFieldValue(dialect, "schema", schema);
					}
				}
				return dialect;
			}
		}
		throw new CommonException("未知的数据库:" + info.getDatabaseName());
	}
}