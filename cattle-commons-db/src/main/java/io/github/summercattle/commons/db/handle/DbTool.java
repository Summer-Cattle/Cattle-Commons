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
package io.github.summercattle.commons.db.handle;

import java.util.Date;

import io.github.summercattle.commons.exception.CommonException;

/**
 * 数据工具
 */
public interface DbTool {

	/**
	 * 数据库的当前日期
	 * @return 数据库的当前日期
	 * @throws CommonException 异常
	 */
	Date getCurrentDate() throws CommonException;

	/**
	 * 序列的下一个值
	 * @param sequenceName 序列名称
	 * @return 序列的下一个值
	 * @throws CommonException 异常
	 */
	long getSequenceNextVal(String sequenceName) throws CommonException;

	/**
	 * 是否有效的数据库连接
	 * @return 是否有效的数据库连接
	 * @throws CommonException 异常
	 */
	boolean validConnection() throws CommonException;

	/**
	 * 保存配置
	 * @param name 配置名
	 * @param encrypt 是否加密
	 * @param value 配置值
	 * @throws CommonException 异常
	 */
	void saveConfig(String name, boolean encrypt, Object value) throws CommonException;

	/**
	 * 读取配置
	 * @param name 配置名
	 * @return 配置值
	 * @throws CommonException 异常
	 */
	Object getConfig(String name) throws CommonException;
}