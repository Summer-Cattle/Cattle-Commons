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
package com.gitlab.summercattle.commons.db.object;

/**
 * 动态分页数据结果集
 */
public interface DynamicPageDataQuery extends DataQuery {

	/**
	 * 页码
	 * @return 页码
	 */
	int getPage();

	/**
	 * 是否有下一页
	 * @return 是否有下一页
	 */
	boolean isNextPage();
}