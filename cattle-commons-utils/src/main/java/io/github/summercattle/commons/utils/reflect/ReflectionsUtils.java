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
package io.github.summercattle.commons.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.parser.ParserConfig;

public class ReflectionsUtils {

	private static final String DEFAULT_SCAN_PACKAGE = "io.github.summercattle";

	static Reflections reflections;

	public synchronized static void init(String[] scanPackages) {
		List<String> packages = new Vector<String>();
		packages.add(DEFAULT_SCAN_PACKAGE);
		if (null != scanPackages && scanPackages.length > 0) {
			for (String scanPackage : scanPackages) {
				if (StringUtils.isNotBlank(scanPackage)) {
					boolean found = false;
					for (String l : packages) {
						if (scanPackage.startsWith(l)) {
							found = true;
							break;
						}
					}
					if (!found) {
						packages.add(scanPackage);
					}
				}
			}
		}
		if (packages.size() > 0) {
			reflections = new Reflections(packages.toArray(new String[0]));
		}
		else {
			reflections = new Reflections();
		}
		ParserConfig parserConfig = ParserConfig.getGlobalInstance();
		parserConfig.setAutoTypeSupport(true);
		parserConfig.addAccept("org.springframework");
		for (String lPackage : packages) {
			parserConfig.addAccept(lPackage);
		}
	}

	static Reflections getReflections() {
		if (null == reflections) {
			init(null);
		}
		return reflections;
	}

	static <T> Set<Class< ? extends T>> getSubTypesOf(final Class<T> type) {
		return getReflections().getSubTypesOf(type);
	}

	static Set<Class< ? >> getTypesAnnotatedWith(final Class< ? extends Annotation> annotation) {
		return getReflections().getTypesAnnotatedWith(annotation);
	}

	static Set<Method> getMethodsAnnotatedWith(final Class< ? extends Annotation> annotation) {
		return getReflections().getMethodsAnnotatedWith(annotation);
	}
}