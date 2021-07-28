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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.db.dialect.impl.AbstractDialect;
import io.github.summercattle.commons.db.dialect.impl.MySQLDialect;
import io.github.summercattle.commons.db.dialect.impl.Oracle12cDialect;
import io.github.summercattle.commons.db.dialect.impl.Oracle8iDialect;
import io.github.summercattle.commons.db.dialect.impl.Oracle9iDialect;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class DialectFactory {

	private static final Logger logger = LoggerFactory.getLogger(DialectFactory.class);

	public static Dialect getDialect(Connection conn) throws CommonException {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			String databaseName = metaData.getDatabaseProductName();
			int databaseMajorVersion = getDatabaseMajorVersion(conn.getMetaData(), databaseName);
			logger.debug("RDBMS:" + databaseName + ",version:" + metaData.getDatabaseProductVersion());
			logger.debug("JDBC driver:" + metaData.getDriverName() + ",version:" + metaData.getDriverVersion());
			return determineDialect(databaseName, databaseMajorVersion, metaData.getSQLKeywords(), conn);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	private static Dialect determineDialect(String databaseName, int databaseMajorVersion, String sqlKeywords, Connection conn)
			throws CommonException {
		if (StringUtils.isEmpty(databaseName)) {
			throw new CommonException("没有数据库产品名称");
		}
		AbstractDialect dialect = null;
		if ("MySQL".equals(databaseName)) {
			if (StringUtils.isNotBlank(sqlKeywords)) {
				sqlKeywords += ",DESCRIBE";
			}
			dialect = new MySQLDialect(conn, sqlKeywords);
		}
		else if ("Oracle".equals(databaseName)) {
			if (databaseMajorVersion >= 12) {
				dialect = new Oracle12cDialect(conn, sqlKeywords);
			}
			else if (databaseMajorVersion >= 9) {
				dialect = new Oracle9iDialect(conn, sqlKeywords);
			}
			else if (databaseMajorVersion == 8) {
				dialect = new Oracle8iDialect(conn, sqlKeywords);
			}
			else {
				throw new CommonException("Oracle数据库未知的版本:" + databaseMajorVersion);
			}
		}
		if (null == dialect) {
			throw new CommonException("未知的数据库:" + databaseName);
		}
		return dialect;
	}

	private static int getDatabaseMajorVersion(DatabaseMetaData meta, String databaseName) {
		try {
			Method method = ReflectUtils.getMethod(meta.getClass(), "getDatabaseMajorVersion");
			return ((Integer) ReflectUtils.invokeObjectMethod(method, meta)).intValue();
		}
		catch (Throwable e) {
			if (databaseName.equals("Oracle")) {
				try {
					Connection conn = meta.getConnection();
					Method method = ReflectUtils.getMethod(conn.getClass(), "getVersionNumber");
					short version = ((Short) ReflectUtils.invokeObjectMethod(method, meta)).shortValue();
					return version / 1000;
				}
				catch (Throwable e1) {
					logger.error(e1.getMessage(), e1);
					return 0;
				}
			}
			else {
				logger.error(e.getMessage(), e);
				return 0;
			}
		}
	}
}