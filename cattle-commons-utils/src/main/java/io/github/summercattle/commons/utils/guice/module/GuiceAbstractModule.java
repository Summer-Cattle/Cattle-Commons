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
package io.github.summercattle.commons.utils.guice.module;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.github.summercattle.commons.utils.reflect.ClassUtils;

public abstract class GuiceAbstractModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(GuiceAbstractModule.class);

	ConcurrentMap<String, Object> objects = new ConcurrentHashMap<String, Object>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void bindClass(Class clazz) {
		Class bindClass = null;
		bindClass = ClassUtils.getSubTypesOfByLoadLevel(clazz);
		if (null != bindClass) {
			String className = bindClass.getName();
			Object obj = objects.get(className);
			if (null == obj) {
				try {
					obj = bindClass.newInstance();
					binder().bind(clazz).toInstance(obj);
					logger.debug("注入Guice,接口类:'" + clazz.getName() + "',实现类:'" + bindClass.getName() + "'");
				}
				catch (InstantiationException | IllegalAccessException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void bindMultiClass(Class clazz) {
		Multibinder binder = Multibinder.newSetBinder(binder(), clazz);
		Class[] classes = ClassUtils.getSubTypesOf(clazz);
		for (Class tClass : classes) {
			binder.addBinding().to(tClass);
		}
	}
}