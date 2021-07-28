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
package io.github.summercattle.commons.db;

import io.github.summercattle.commons.db.handle.DbMetaModel;
import io.github.summercattle.commons.db.handle.DbSecurityKey;
import io.github.summercattle.commons.db.handle.DbStruct;
import io.github.summercattle.commons.db.handle.DbTool;
import io.github.summercattle.commons.db.handle.DbTransaction;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.guice.GuiceUtils;

public class DbUtils {

	public static DbMetaModel getDbMetaModel() throws CommonException {
		DbMetaModel dbMetaModel = GuiceUtils.getInstance(DbMetaModel.class);
		if (null == dbMetaModel) {
			throw new CommonException("数据定义模型实现类为空");
		}
		return dbMetaModel;
	}

	public static DbTransaction getDbTransaction() throws CommonException {
		DbTransaction dbTransaction = GuiceUtils.getInstance(DbTransaction.class);
		if (null == dbTransaction) {
			throw new CommonException("数据事务实现类为空");
		}
		return dbTransaction;
	}

	public static DbSecurityKey getDbSecurityKey() throws CommonException {
		DbSecurityKey dbSecurityKey = GuiceUtils.getInstance(DbSecurityKey.class);
		if (null == dbSecurityKey) {
			throw new CommonException("数据密钥实现类为空");
		}
		return dbSecurityKey;
	}

	public static DbStruct getDbStruct() throws CommonException {
		DbStruct dbStruct = GuiceUtils.getInstance(DbStruct.class);
		if (null == dbStruct) {
			throw new CommonException("数据结构实现类为空");
		}
		return dbStruct;
	}

	public static DbTool getDbTool() throws CommonException {
		DbTool dbTool = GuiceUtils.getInstance(DbTool.class);
		if (null == dbTool) {
			throw new CommonException("数据工具实现类为空");
		}
		return dbTool;
	}
}