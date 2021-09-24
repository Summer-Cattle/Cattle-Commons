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
package com.gitlab.summercattle.commons.db.dialect;

public interface DialectResolutionInfo {

	/**
	 * 没有版本号
	 */
	public static final int NO_VERSION = -999;

	/**
	 * 数据库名称
	 * @return 数据库名称
	 */
	String getDatabaseName();

	/**
	 * 数据库版本信息
	 * @return 数据库版本信息
	 */
	String getDatabaseVersion();

	/**
	 * 数据库主版本号
	 * @return 数据库主版本号
	 */
	int getDatabaseMajorVersion();

	/**
	 * 数据库次版本号
	 * @return 数据库次版本号
	 */
	int getDatabaseMinorVersion();

	/**
	 * 驱动名称
	 * @return 驱动名称
	 */
	String getDriverName();

	/**
	 * 驱动版本信息
	 * @return 驱动版本信息
	 */
	String getDriverVersion();

	/**
	 * 驱动主版本号
	 * @return 驱动主版本号
	 */
	int getDriverMajorVersion();

	/**
	 * 驱动次版本号
	 * @return 驱动次版本号
	 */
	int getDriverMinorVersion();
}