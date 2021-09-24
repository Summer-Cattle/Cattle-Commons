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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.gitlab.summercattle.commons.db.constants.DataType;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.auxiliary.StringUtils;

class TypeNames {

	private final Map<DataType, TypeName> defaults = new HashMap<DataType, TypeName>();

	private final Map<DataType, Map<Long, TypeName>> weighted = new HashMap<DataType, Map<Long, TypeName>>();

	public String get(DataType dataType) throws CommonException {
		TypeName result = defaults.get(dataType);
		if (null == result) {
			throw new CommonException("没有数据库方言映射针对JDBC类型:" + dataType.toString());
		}
		return result.getName();
	}

	public String getSimple(DataType dataType) throws CommonException {
		TypeName result = defaults.get(dataType);
		if (null == result) {
			throw new CommonException("没有数据库方言映射针对JDBC类型:" + dataType.toString());
		}
		return result.getSimpleName();
	}

	public String get(DataType dataType, long size, int scale) throws CommonException {
		Map<Long, TypeName> map = weighted.get(dataType);
		if (null != map && map.size() > 0) {
			for (Map.Entry<Long, TypeName> entry : map.entrySet()) {
				if (size < entry.getKey()) {
					return replace(entry.getValue().getName(), size, scale);
				}
			}
		}
		return replace(get(dataType), size, scale);
	}

	public String getSimple(DataType dataType, long size, int scale) throws CommonException {
		Map<Long, TypeName> map = weighted.get(dataType);
		if (null != map && map.size() > 0) {
			for (Map.Entry<Long, TypeName> entry : map.entrySet()) {
				if (size < entry.getKey()) {
					return entry.getValue().getSimpleName();
				}
			}
		}
		return getSimple(dataType);
	}

	private String replace(String type, long size, int scale) {
		type = StringUtils.replaceOnce(type, "$s", Integer.toString(scale));
		return StringUtils.replaceOnce(type, "$l", Long.toString(size));
	}

	public void put(DataType dataType, long capacity, String simpleName, String name) {
		Map<Long, TypeName> map = weighted.get(dataType);
		if (null == map) {
			map = new TreeMap<Long, TypeName>();
			weighted.put(dataType, map);
		}
		map.put(capacity, new TypeName(simpleName, name));
	}

	public void put(DataType dataType, String simpleName, String name) {
		defaults.put(dataType, new TypeName(simpleName, name));
	}
}