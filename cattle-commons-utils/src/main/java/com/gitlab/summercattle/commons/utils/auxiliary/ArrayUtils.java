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

public class ArrayUtils {

	public static String toString(Object[] array) {
		StringBuffer sb = new StringBuffer();
		if (null != array && array.length > 0) {
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					sb.append(",");
				}
				if (null != array[i]) {
					if (array[i] instanceof byte[]) {
						sb.append("[二进制数据]");
					}
					else {
						sb.append(array[i].toString());
					}
				}
				else {
					sb.append("NULL");
				}
			}
		}
		return sb.toString();
	}
}