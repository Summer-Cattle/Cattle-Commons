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
package com.gitlab.summercattle.commons.db.dialect.impl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.gitlab.summercattle.commons.db.dialect.DialectResolutionInfo;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class DialectResolutionInfoImpl implements DialectResolutionInfo {

	private DatabaseMetaData databaseMetaData;

	public DialectResolutionInfoImpl(DatabaseMetaData databaseMetaData) {
		this.databaseMetaData = databaseMetaData;
	}

	@Override
	public String getDatabaseName() {
		try {
			return databaseMetaData.getDatabaseProductName();
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public String getDatabaseVersion() {
		try {
			return databaseMetaData.getDatabaseProductVersion();
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public int getDatabaseMajorVersion() {
		try {
			return interpretVersion(databaseMetaData.getDatabaseMajorVersion());
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public int getDatabaseMinorVersion() {
		try {
			return interpretVersion(databaseMetaData.getDatabaseMinorVersion());
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public String getDriverName() {
		try {
			return databaseMetaData.getDriverName();
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public String getDriverVersion() {
		try {
			return databaseMetaData.getDriverVersion();
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public int getDriverMajorVersion() {
		return interpretVersion(databaseMetaData.getDriverMajorVersion());
	}

	@Override
	public int getDriverMinorVersion() {
		return interpretVersion(databaseMetaData.getDriverMinorVersion());
	}

	private static int interpretVersion(int result) {
		return result < 0 ? NO_VERSION : result;
	}
}