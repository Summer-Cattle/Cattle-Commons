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

import java.sql.Connection;

import com.gitlab.summercattle.commons.db.struct.TableObjectStruct;
import com.gitlab.summercattle.commons.db.struct.ViewObjectStruct;
import com.gitlab.summercattle.commons.exception.CommonException;

public interface StructHandler {

	default boolean supportsTable() {
		return true;
	}
	
	boolean existTable(Connection conn, String name) throws CommonException;

	TableObjectStruct getTable(Connection conn, String name) throws CommonException;

	default boolean supportsView() {
		return true;
	}

	boolean existView(Connection conn, String name) throws CommonException;
	
	ViewObjectStruct getView(Connection conn, String name) throws CommonException;
}