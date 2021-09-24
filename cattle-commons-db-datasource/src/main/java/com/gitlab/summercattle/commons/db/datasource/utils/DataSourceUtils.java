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
package com.gitlab.summercattle.commons.db.datasource.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.datasource.configure.DataSourceInfo;
import com.gitlab.summercattle.commons.db.datasource.druid.DruidDataSourceWrapper;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.reflect.ClassType;
import com.gitlab.summercattle.commons.utils.reflect.ClassUtils;
import com.gitlab.summercattle.commons.utils.reflect.ReflectUtils;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceUtils {

	private static final String DRUID_CLASS_NAME = "com.gitlab.summercattle.commons.db.datasource.druid.DruidDataSourceWrapper";

	private static final String HIKARI_CLASS_NAME = "com.zaxxer.hikari.HikariDataSource";

	private static Class< ? > getDataSourceClass() throws CommonException {
		Class< ? > dataSourceClass = null;
		try {
			dataSourceClass = Class.forName(DRUID_CLASS_NAME);
		}
		catch (Throwable e) {
		}
		if (null == dataSourceClass) {
			try {
				dataSourceClass = Class.forName(HIKARI_CLASS_NAME);
			}
			catch (Throwable e) {
			}
		}
		if (null == dataSourceClass) {
			throw new CommonException("没有找到数据源连接池");
		}
		return dataSourceClass;
	}

	public static DataSource getDataSource(String driverClassName, DataSourceInfo dataSourceInfo, int index) throws CommonException {
		if (null == dataSourceInfo) {
			throw new CommonException((index != 0 ? "第" + index + "个" : "") + "数据源信息为空");
		}
		if (StringUtils.isBlank(dataSourceInfo.getJdbcUrl())) {
			throw new CommonException((index != 0 ? "第" + index + "个" : "") + "数据源JDBC URL为空");
		}
		Class< ? > dataSourceClass = getDataSourceClass();
		DataSource dataSource = (DataSource) ClassUtils.instance(dataSourceClass);
		if (null != dataSourceInfo.getProps()) {
			setDataSource(dataSource, dataSourceInfo.getProps());
		}
		String className = dataSourceClass.getName();
		if (DRUID_CLASS_NAME.equals(className)) {
			((DruidDataSourceWrapper) dataSource).setDriverClassName(driverClassName);
			((DruidDataSourceWrapper) dataSource).setUrl(dataSourceInfo.getJdbcUrl());
			((DruidDataSourceWrapper) dataSource).setUsername(dataSourceInfo.getUsername());
			((DruidDataSourceWrapper) dataSource).setPassword(dataSourceInfo.getPassword());
		}
		else if (HIKARI_CLASS_NAME.equals(className)) {
			((HikariDataSource) dataSource).setDriverClassName(driverClassName);
			((HikariDataSource) dataSource).setJdbcUrl(dataSourceInfo.getJdbcUrl());
			((HikariDataSource) dataSource).setUsername(dataSourceInfo.getUsername());
			((HikariDataSource) dataSource).setPassword(dataSourceInfo.getPassword());
		}
		else {
			throw new CommonException("未知数据源连接池的类:" + dataSourceClass.getName());
		}
		return dataSource;
	}

	private static void setDataSource(DataSource dataSource, Map<String, String> props) throws CommonException {
		Iterator<String> iterator = props.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = props.get(key);
			String name = getKey(key);
			Method method = ReflectUtils.findSetMethod(dataSource.getClass(), name);
			if (null != method) {
				Parameter parameter = method.getParameters()[0];
				ClassType classType = ReflectUtils.getClassType(parameter.getType());
				ReflectUtils.invokeObjectMethod(method, dataSource, ReflectUtils.convertValue(classType, value));
			}
		}
	}

	private static String getKey(String key) {
		StringBuffer sb = new StringBuffer();
		int len = key.length();
		for (int i = 0; i < len; i++) {
			String var = key.substring(i, i + 1);
			if ("-".equals(var)) {
				if (i + 1 < len) {
					var = key.substring(i + 1, i + 2).toUpperCase();
					sb.append(var);
					i++;
				}
				else {
					sb.append(var);
				}
			}
			else {
				sb.append(var);
			}
		}
		return sb.toString();
	}
}