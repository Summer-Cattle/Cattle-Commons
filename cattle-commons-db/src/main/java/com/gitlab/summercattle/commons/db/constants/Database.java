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
package com.gitlab.summercattle.commons.db.constants;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.dialect.DialectResolutionInfo;
import com.gitlab.summercattle.commons.db.dialect.impl.MySQL55Dialect;
import com.gitlab.summercattle.commons.db.dialect.impl.MySQL57Dialect;
import com.gitlab.summercattle.commons.db.dialect.impl.MySQL5Dialect;
import com.gitlab.summercattle.commons.db.dialect.impl.MySQLDialect;
import com.gitlab.summercattle.commons.db.dialect.impl.Oracle10gDialect;
import com.gitlab.summercattle.commons.db.dialect.impl.Oracle12cDialect;
import com.gitlab.summercattle.commons.db.dialect.impl.Oracle8iDialect;
import com.gitlab.summercattle.commons.db.dialect.impl.Oracle9iDialect;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

/**
 * 数据库类型
 */
public enum Database {

	/**
	 * MySQL数据库
	 */
	MySQL {

		@Override
		public Class< ? extends Dialect> latestDialect() {
			return MySQL57Dialect.class;
		}

		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			String databaseName = info.getDatabaseName();
			if ("MySQL".equals(databaseName)) {
				int majorVersion = info.getDatabaseMajorVersion();
				int minorVersion = info.getDatabaseMinorVersion();
				if (majorVersion < 5) {
					return new MySQLDialect();
				}
				else if (majorVersion == 5) {
					if (minorVersion < 5) {
						return new MySQL5Dialect();
					}
					else if (minorVersion < 7) {
						return new MySQL55Dialect();
					}
					else {
						return new MySQL57Dialect();
					}
				}
				else if (majorVersion < 8) {
					// There is no MySQL 6 or 7.
					// Adding this just in case.
					return new MySQL57Dialect();
				}
				return latestDialectInstance(this);
			}
			return null;
		}
	},
	/**
	* Oracle数据库
	*/
	Oracle {

		@Override
		public Class< ? extends Dialect> latestDialect() {
			return Oracle12cDialect.class;
		}

		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			String databaseName = info.getDatabaseName();
			if ("Oracle".equals(databaseName)) {
				int majorVersion = info.getDatabaseMajorVersion();
				switch (majorVersion) {
					case 12:
						return new Oracle12cDialect();
					case 11:
						// fall through
					case 10:
						return new Oracle10gDialect();
					case 9:
						return new Oracle9iDialect();
					case 8:
						return new Oracle8iDialect();
					default:
						return latestDialectInstance(this);
				}
			}
			return null;
		}
	};

	public abstract Class< ? extends Dialect> latestDialect();

	public abstract Dialect resolveDialect(DialectResolutionInfo info);

	private static Dialect latestDialectInstance(Database database) {
		try {
			return database.latestDialect().newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}
}