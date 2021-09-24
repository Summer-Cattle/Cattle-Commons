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
package com.gitlab.summercattle.commons.db.object.internal;

import java.util.List;
import java.util.Map;

import com.gitlab.summercattle.commons.exception.CommonException;

public interface InternalDataTable {

	RowLineSet[] getDeleteLines();

	int[] getFieldTypes();

	void setAddAndModifyLines(List<RowLineSet> addLines, List<RowLineSet> modifyLines) throws CommonException;

	Map<String, Integer> getFieldIndexes();

	boolean isUseCache();
}