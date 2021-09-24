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
package com.gitlab.summercattle.commons.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.guice.GuiceUtils;
import com.gitlab.summercattle.commons.utils.reflect.annotation.ClassLoadLevel;
import com.gitlab.summercattle.commons.utils.reflect.annotation.ConditionalOnClass;

public class ClassUtils {

	private static Objenesis objenesis = new ObjenesisStd(true);

	private static final ConcurrentMap<String, ServiceLoader< ? >> serviceLoaderCache = new ConcurrentHashMap<String, ServiceLoader< ? >>();

	@SuppressWarnings("unchecked")
	public static <T> Class<T>[] getSubTypesOf(Class<T> type) {
		Set<Class< ? extends T>> classes = ReflectionsUtils.getSubTypesOf(type);
		List<Class<T>> rClasses = new Vector<Class<T>>();
		for (Class< ? extends T> clazz : classes) {
			if (isClass(clazz)) {
				clazz = (Class< ? extends T>) checkConditionalOnClass(clazz);
				if (null != clazz) {
					rClasses.add((Class<T>) clazz);
				}
			}
		}
		return rClasses.toArray(new Class[0]);
	}

	public static <T> Class<T> getSubTypesOfByLoadLevel(Class<T> type) {
		Class<T>[] classes = getSubTypesOf(type);
		Class<T> result = null;
		int currentLevel = -1;
		for (Class<T> lClazz : classes) {
			ClassLoadLevel loadLevel = lClazz.getAnnotation(ClassLoadLevel.class);
			int level = loadLevel != null ? loadLevel.value() : 0;
			if (level > currentLevel) {
				result = lClazz;
				currentLevel = level;
			}
		}
		return result;
	}

	public static Class< ? >[] getTypesAnnotatedWith(Class< ? extends Annotation> annotation) {
		Set<Class< ? >> classes = ReflectionsUtils.getTypesAnnotatedWith(annotation);
		List<Class< ? >> rClasses = new Vector<Class< ? >>();
		for (Class< ? > clazz : classes) {
			if (isClass(clazz)) {
				clazz = checkConditionalOnClass(clazz);
				if (null != clazz) {
					rClasses.add(clazz);
				}
			}
		}
		return rClasses.toArray(new Class[0]);
	}

	private static Class< ? > checkConditionalOnClass(Class< ? > clazz) {
		ConditionalOnClass conditionalOnClass = clazz.getAnnotation(ConditionalOnClass.class);
		if (null != conditionalOnClass) {
			String[] classNames = conditionalOnClass.value();
			if (null != classNames) {
				for (String className : classNames) {
					if (StringUtils.isNotBlank(className)) {
						try {
							Class.forName(className);
						}
						catch (ClassNotFoundException e) {
							clazz = null;
							break;
						}
					}
				}
			}
		}
		return clazz;
	}

	public static boolean isInterfaceClass(Class< ? > clazz) {
		int modifier = clazz.getModifiers();
		return Modifier.isInterface(modifier);
	}

	public static boolean isClass(Class< ? > clazz) {
		int modifier = clazz.getModifiers();
		return !Modifier.isInterface(modifier) && !Modifier.isAbstract(modifier);
	}

	public static boolean isPublicClass(Class< ? > clazz) {
		int modifier = clazz.getModifiers();
		return Modifier.isPublic(modifier);
	}

	public static boolean isFinalClass(Class< ? > clazz) {
		int modifier = clazz.getModifiers();
		return Modifier.isFinal(modifier);
	}

	public static boolean isStaticClass(Class< ? > clazz) {
		int modifier = clazz.getModifiers();
		return Modifier.isStatic(modifier);
	}

	public static boolean isAbstractClass(Class< ? > clazz) {
		int modifier = clazz.getModifiers();
		return Modifier.isAbstract(modifier);
	}

	public static <T> List<T> getService(Class<T> service, boolean cache) throws CommonException {
		ServiceLoader<T> serviceLoader = getServiceLoader(service, cache);
		List<T> result = new Vector<T>();
		for (T lService : serviceLoader) {
			result.add(lService);
		}
		return result;
	}

	public static <T> List<T> getService(Class<T> service) throws CommonException {
		return getService(service, false);
	}

	public static <T> T getServiceByLoadLevel(Class<T> service) throws CommonException {
		return getServiceByLoadLevel(service, false);
	}

	public static <T> T getServiceByLoadLevel(Class<T> service, boolean cache) throws CommonException {
		ServiceLoader<T> serviceLoader = getServiceLoader(service, cache);
		T result = null;
		int currentLevel = -1;
		for (T lService : serviceLoader) {
			ClassLoadLevel loadLevel = lService.getClass().getAnnotation(ClassLoadLevel.class);
			int level = loadLevel != null ? loadLevel.value() : 0;
			if (level > currentLevel) {
				result = lService;
				currentLevel = level;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> ServiceLoader<T> getServiceLoader(Class<T> service, boolean cache) throws CommonException {
		if (!isPublicClass(service) && !isInterfaceClass(service)) {
			throw new CommonException("'" + service.getName() + "'不是公共接口类");
		}
		ServiceLoader<T> serviceLoader = null;
		String serviceName = service.getName();
		if (cache) {
			if (serviceLoaderCache.containsKey(serviceName)) {
				serviceLoader = (ServiceLoader<T>) serviceLoaderCache.get(serviceName);
			}
		}
		if (null == serviceLoader) {
			serviceLoader = ServiceLoader.load(service);
			if (cache) {
				synchronized (serviceLoaderCache) {
					serviceLoaderCache.put(serviceName, serviceLoader);
				}
			}
		}
		return serviceLoader;
	}

	public static <T> T instance(Class<T> type) throws CommonException {
		try {
			return type.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static <T> T instanceEmpty(Class<T> type) {
		return objenesis.newInstance(type);
	}

	public static boolean implementsInterface(Class< ? > clazz, Class< ? > intf) {
		if (!isAbstractClass(intf) && !isInterfaceClass(intf)) {
			return false;
		}
		return intf.isAssignableFrom(clazz);
	}

	public static ClassResourceLoader getClassResourceLoader() throws CommonException {
		return GuiceUtils.getInstance(ClassResourceLoader.class);
	}
}