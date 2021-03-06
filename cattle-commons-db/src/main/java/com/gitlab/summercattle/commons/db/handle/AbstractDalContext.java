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
package com.gitlab.summercattle.commons.db.handle;

import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.exception.CommonException;

public interface AbstractDalContext {

	/**
	 * 得到数据库方言
	 * @return 数据库方言
	 */
	Dialect getDialect();

	/**
	 * 执行
	 * @param sql sql语句
	 * @param params 参数
	 * @throws CommonException 异常
	 */
	void execute(String sql, Object... params) throws CommonException;
}