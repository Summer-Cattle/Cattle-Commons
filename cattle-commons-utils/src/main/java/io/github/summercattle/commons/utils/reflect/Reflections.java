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

import java.net.URL;
import java.util.List;

import org.reflections.ReflectionUtils;
import org.reflections.ReflectionsException;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Reflections extends org.reflections.Reflections {

	private static final Logger logger = LoggerFactory.getLogger(Reflections.class);

	private static List<String> primitiveNames;

	private static List<Class< ? >> primitiveTypes;

	private static List<String> primitiveDescriptors;

	public Reflections(String[] prefix) {
		super((Object[]) prefix);
	}

	public Reflections() {
		super(new Object[0]);
	}

	@Override
	public void expandSuperTypes() {
		if (store.keySet().contains(index(SubTypesScanner.class))) {
			Multimap<String, String> mmap = store.get(index(SubTypesScanner.class));
			Sets.SetView<String> keys = Sets.difference(mmap.keySet(), Sets.newHashSet(mmap.values()));
			Multimap<String, String> expand = HashMultimap.create();
			for (String key : keys) {
				final Class< ? > type = forName(key);
				if (type != null) {
					expandSupertypes(expand, key, type);
				}
			}
			mmap.putAll(expand);
		}
	}

	private static String index(Class< ? extends Scanner> scannerClass) {
		return scannerClass.getSimpleName();
	}

	private void expandSupertypes(Multimap<String, String> mmap, String key, Class< ? > type) {
		for (Class< ? > supertype : ReflectionUtils.getSuperTypes(type)) {
			if (mmap.put(supertype.getName(), key)) {
				expandSupertypes(mmap, supertype.getName(), supertype);
			}
		}
	}

	private static Class< ? > forName(String typeName, ClassLoader... classLoaders) {
		if (getPrimitiveNames().contains(typeName)) {
			return getPrimitiveTypes().get(getPrimitiveNames().indexOf(typeName));
		}
		else {
			String type;
			if (typeName.contains("[")) {
				int i = typeName.indexOf("[");
				type = typeName.substring(0, i);
				String array = typeName.substring(i).replace("]", "");
				if (getPrimitiveNames().contains(type)) {
					type = getPrimitiveDescriptors().get(getPrimitiveNames().indexOf(type));
				}
				else {
					type = "L" + type + ";";
				}
				type = array + type;
			}
			else {
				type = typeName;
			}
			List<ReflectionsException> reflectionsExceptions = Lists.newArrayList();
			for (ClassLoader classLoader : ClasspathHelper.classLoaders(classLoaders)) {
				if (type.contains("[")) {
					try {
						return Class.forName(type, false, classLoader);
					}
					catch (Throwable e) {
						reflectionsExceptions.add(new ReflectionsException("could not get type for name " + typeName, e));
					}
				}
				try {
					return classLoader.loadClass(type);
				}
				catch (Throwable e) {
				}
			}
			for (ReflectionsException reflectionsException : reflectionsExceptions) {
				logger.warn("could not get type for name " + typeName + " from any class loader", reflectionsException);
			}
			return null;
		}
	}

	private static void initPrimitives() {
		if (primitiveNames == null) {
			primitiveNames = Lists.newArrayList("boolean", "char", "byte", "short", "int", "long", "float", "double", "void");
			primitiveTypes = Lists.<Class< ? >> newArrayList(boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class,
					double.class, void.class);
			primitiveDescriptors = Lists.newArrayList("Z", "C", "B", "S", "I", "J", "F", "D", "V");
		}
	}

	private static List<String> getPrimitiveNames() {
		initPrimitives();
		return primitiveNames;
	}

	private static List<Class< ? >> getPrimitiveTypes() {
		initPrimitives();
		return primitiveTypes;
	}

	private static List<String> getPrimitiveDescriptors() {
		initPrimitives();
		return primitiveDescriptors;
	}

	@Override
	protected void scan(URL url) {
		Vfs.Dir dir = Vfs.fromURL(url);
		try {
			for (Vfs.File file : dir.getFiles()) {
				// scan if inputs filter accepts file relative path or fqn
				Predicate<String> inputsFilter = configuration.getInputsFilter();
				String path = file.getRelativePath();
				String fqn = path.replace('/', '.');
				if (inputsFilter == null || inputsFilter.apply(path) || inputsFilter.apply(fqn)) {
					Object classObject = null;
					for (Scanner scanner : configuration.getScanners()) {
						try {
							if (scanner.acceptsInput(path) && scanner.acceptResult(fqn)) {
								classObject = scanner.scan(file, classObject);
							}
						}
						catch (Exception e) {
							if (logger != null && logger.isDebugEnabled()) {
								logger.debug("could not scan file " + file.getRelativePath() + " in url " + url.toExternalForm() + " with scanner "
										+ scanner.getClass().getSimpleName(), e);
							}
						}
					}
				}
			}
		}
		finally {
			dir.close();
		}
	}
}