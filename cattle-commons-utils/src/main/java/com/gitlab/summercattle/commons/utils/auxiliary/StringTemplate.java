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
package com.gitlab.summercattle.commons.utils.auxiliary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gitlab.summercattle.commons.exception.CommonException;

public class StringTemplate {

	/**Java 风格的模板变量引用字符串: ${...}*/
	public static final String REGEX_PATTERN_JAVA_STYLE = "\\$\\{([^\\}]*)\\}";

	/**Unix shell 风格的模板变量引用字符串: $...*/
	public static final String REGEX_PATTERN_UNIX_SHELL_STYLE = "\\$([a-zA-Z_]+)";

	private Map<String, String> varExpMap = new HashMap<String, String>(); //Map <varName:String, varExpression:String>

	private Map<String, String> varValueMap = new HashMap<String, String>(); //Map <varName:String, varValue:String>

	private String templateString;

	public StringTemplate(String templateString, String templateRegex) throws CommonException {
		Pattern p = Pattern.compile(templateRegex);
		Matcher m = p.matcher(templateString);
		while (m.find()) {
			if (m.groupCount() == 1) {
				//group[0]: the all match string, group[1]: the group
				varExpMap.put(m.group(1), m.group(0));
			}
			else {
				throw new CommonException("模式'" + templateRegex + "'是无效的,它必须包含且只包含一个正则表达式");
			}
		}
		this.templateString = templateString;
	}

	public String[] getVariables() {
		return varExpMap.keySet().toArray(new String[0]);
	}

	public void setVariable(String varName, String varValue) {
		varValueMap.put(varName, varValue);
	}

	public String getParseResult() {
		Iterator<Entry<String, String>> itr = varExpMap.entrySet().iterator();
		String tmp = this.templateString;
		while (itr.hasNext()) {
			Entry<String, String> en = itr.next();
			String varName = en.getKey();
			String varExp = en.getValue();
			String val = varValueMap.get(varName);
			if (val != null) {
				tmp = StringUtils.replace(tmp, varExp, val);
			}
		}
		return tmp;
	}

	public void reset() {
		this.varValueMap.clear();
	}
}