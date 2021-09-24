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
package com.gitlab.summercattle.commons.db.guice;

import com.gitlab.summercattle.commons.db.handle.DbMetaModel;
import com.gitlab.summercattle.commons.db.handle.DbSecurityKey;
import com.gitlab.summercattle.commons.db.handle.DbStruct;
import com.gitlab.summercattle.commons.db.handle.DbTool;
import com.gitlab.summercattle.commons.db.handle.DbTransaction;
import com.gitlab.summercattle.commons.db.meta.TableMetaParser;
import com.gitlab.summercattle.commons.utils.guice.annotation.GuiceModule;
import com.gitlab.summercattle.commons.utils.guice.module.GuiceAbstractModule;

@GuiceModule
public class DbModule extends GuiceAbstractModule {

	@Override
	public void configure() {
		bindClass(DbMetaModel.class);
		bindClass(DbTransaction.class);
		bindClass(DbStruct.class);
		bindClass(DbTool.class);
		bindClass(DbSecurityKey.class);
		bindMultiClass(TableMetaParser.class);
	}
}