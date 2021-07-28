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
package io.github.summercattle.commons.db.dialect.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.StringUtils;

class TypeNames {

	private final Map<DataType, String> defaults = new HashMap<DataType, String>();

	private final Map<DataType, Map<Long, String>> weighted = new HashMap<DataType, Map<Long, String>>();

	public String get(DataType dataType) throws CommonException {
		String result = defaults.get(dataType);
		if (null == result) {
			throw new CommonException("没有数据库方言映射针对JDBC类型:" + dataType.toString());
		}
		return result;
	}

	public String get(DataType dataType, long length, int precision) throws CommonException {
		Map<Long, String> map = weighted.get(dataType);
		if (null != map && map.size() > 0) {
			for (Map.Entry<Long, String> entry : map.entrySet()) {
				if (length < entry.getKey()) {
					return replace(entry.getValue(), length, precision);
				}
			}
		}
		return replace(get(dataType), length, precision);
	}

	private String replace(String type, long length, int precision) {
		type = StringUtils.replaceOnce(type, "$s", Integer.toString(precision));
		type = StringUtils.replaceOnce(type, "$l", Long.toString(length));
		return StringUtils.replaceOnce(type, "$p", Long.toString(length));
	}

	public void put(DataType dataType, long capacity, String value) {
		Map<Long, String> map = weighted.get(dataType);
		if (null == map) {
			map = new TreeMap<Long, String>();
			weighted.put(dataType, map);
		}
		map.put(capacity, value);
	}

	public void put(DataType dataType, String value) {
		defaults.put(dataType, value);
	}
}