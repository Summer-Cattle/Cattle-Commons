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
package io.github.summercattle.commons.db.meta.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.spring.SpringContext;

class TableMetaUtils {

	static void checkTable(List<TableMeta> tableMetas, String resourceDescription, TableMeta tableMeta) throws CommonException {
		String name = tableMeta.getName();
		String alias = tableMeta.getAlias();
		boolean found = false;
		for (TableMeta lTableMeta : tableMetas) {
			if (name.equalsIgnoreCase(lTableMeta.getName())) {
				found = true;
				break;
			}
			if (name.equalsIgnoreCase(lTableMeta.getAlias())) {
				found = true;
				break;
			}
			if (StringUtils.isNotBlank(alias)) {
				if (alias.equalsIgnoreCase(lTableMeta.getName())) {
					found = true;
					break;
				}
				if (alias.equalsIgnoreCase(lTableMeta.getAlias())) {
					found = true;
					break;
				}
			}
		}
		if (found) {
			throw new CommonException("处理" + resourceDescription + "的数据表名或别名有重复");
		}
	}

	static boolean allowTable(Element rootElement) {
		List<String> propertyNames = new Vector<String>();
		String havingValue = "";
		boolean matchIfMissing = false;
		Element conditionElement = rootElement.element("Condition");
		if (null != conditionElement) {
			Element propertyNamesElement = conditionElement.element("PropertyNames");
			if (null != propertyNamesElement) {
				List<Element> propertyNameElements = propertyNamesElement.elements("PropertyName");
				for (Element propertyNameElement : propertyNameElements) {
					String propertyName = propertyNameElement.getText();
					if (StringUtils.isNotBlank(propertyName)) {
						propertyNames.add(propertyName);
					}
				}
			}
			havingValue = conditionElement.elementText("HavingValue");
			String strMatchIfMissing = conditionElement.elementText("MatchIfMissing");
			matchIfMissing = BooleanUtils.toBoolean(strMatchIfMissing);
		}
		return allowTable(propertyNames.toArray(new String[0]), havingValue, matchIfMissing);
	}

	public static boolean allowTable(String[] propertyNames, String havingValue, boolean matchIfMissing) {
		List<String> missing = new ArrayList<String>();
		List<String> noMatching = new ArrayList<String>();
		for (String propertyName : propertyNames) {
			if (SpringContext.containsProperty(propertyName)) {
				if (!isMatch(SpringContext.getProperty(propertyName), havingValue)) {
					noMatching.add(propertyName);
				}
			}
			else {
				if (!matchIfMissing) {
					missing.add(propertyName);
				}
			}
		}
		if (!noMatching.isEmpty() || !missing.isEmpty()) {
			return false;
		}
		return true;
	}

	private static boolean isMatch(String value, String havingValue) {
		if (StringUtils.isNotEmpty(havingValue)) {
			return havingValue.equalsIgnoreCase(value);
		}
		return !"false".equalsIgnoreCase(value);
	}
}