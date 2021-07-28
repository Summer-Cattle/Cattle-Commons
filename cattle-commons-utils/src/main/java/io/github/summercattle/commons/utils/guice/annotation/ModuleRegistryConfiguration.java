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
package io.github.summercattle.commons.utils.guice.annotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.google.inject.spi.ElementSource;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.guice.GuiceUtils;
import io.github.summercattle.commons.utils.guice.module.SpringModule;
import io.github.summercattle.commons.utils.reflect.ClassUtils;
import io.github.summercattle.commons.utils.spring.SpringContext;

class ModuleRegistryConfiguration implements BeanDefinitionRegistryPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ModuleRegistryConfiguration.class);

	private AtomicBoolean initCreated = new AtomicBoolean(false);

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		beanFactory.registerSingleton("guiceInjectorInitializer", new GuiceInjectorInitializingBeanPostProcessor() {

			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
				return bean;
			}

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
				if (initCreated.compareAndSet(false, true)) {
					SpringContext.initBeanFactory(beanFactory);
					createGuiceModules(beanFactory);
				}
				return bean;
			}
		});
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
	}

	private void createGuiceModules(ConfigurableListableBeanFactory beanFactory) {
		List<Module> modules = new Vector<Module>();
		Class< ? >[] moduleClasses = ClassUtils.getTypesAnnotatedWith(GuiceModule.class);
		for (Class< ? > moduleClass : moduleClasses) {
			if (ClassUtils.implementsInterface(moduleClass, Module.class) && ClassUtils.isClass(moduleClass)) {
				try {
					Object obj = ClassUtils.instance(moduleClass);
					if (null != obj) {
						logger.debug("找到Guice模块,类:'{}'", moduleClass.getName());
						modules.add((Module) obj);
					}
				}
				catch (CommonException e) {
				}
			}
		}
		Collection<Module> sModules = beanFactory.getBeansOfType(Module.class).values();
		for (Module module : sModules) {
			if (null == module.getClass().getAnnotation(GuiceModule.class)) {
				logger.debug("找到Guice模块,类:" + module.getClass().getName());
				modules.add(module);
			}
		}
		modules.add(new SpringModule(beanFactory));
		Injector injector = GuiceUtils.createInjector(modules);
		Map<Key< ? >, Binding< ? >> bindings = injector.getAllBindings();
		mapBindings(bindings, (BeanDefinitionRegistry) beanFactory);
		beanFactory.registerResolvableDependency(Injector.class, injector);
		beanFactory.registerSingleton("injector", injector);
	}

	private void mapBindings(Map<Key< ? >, Binding< ? >> bindings, BeanDefinitionRegistry registry) {
		for (Entry<Key< ? >, Binding< ? >> entry : bindings.entrySet()) {
			Key< ? > key = entry.getKey();
			Class< ? > keyType = key.getTypeLiteral().getRawType();
			if (Stage.class.equals(keyType) || java.util.logging.Logger.class.equals(keyType) || null != keyType.getAnnotation(GuiceBindBean.class)
					|| java.util.Set.class.equals(keyType) || java.util.Collection.class.equals(keyType) || Injector.class.equals(keyType)) {
				continue;
			}
			if (SpringModule.CATTLE_GUICE_SOURCE.equals(Optional.ofNullable(entry.getValue().getSource()).map(Object::toString).orElse(""))) {
				continue;
			}
			if (null != key.getAnnotationType() && key.getAnnotationType().getName().equals("com.google.inject.internal.Element")) {
				continue;
			}
			Binding< ? > binding = entry.getValue();
			Object source = binding.getSource();
			RootBeanDefinition bean = new RootBeanDefinition(GuiceFactoryBean.class);
			ConstructorArgumentValues args = new ConstructorArgumentValues();
			args.addIndexedArgumentValue(0, keyType);
			args.addIndexedArgumentValue(1, key);
			args.addIndexedArgumentValue(2, Scopes.isSingleton(binding));
			bean.setConstructorArgumentValues(args);
			bean.setTargetType(ResolvableType.forType(key.getTypeLiteral().getType()));
			if (!Scopes.isSingleton(binding)) {
				bean.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
			}
			if (source instanceof ElementSource) {
				bean.setResourceDescription(((ElementSource) source).getDeclaringSource().toString());
			}
			else {
				bean.setResourceDescription(SpringModule.CATTLE_GUICE_SOURCE);
			}
			bean.setAttribute(SpringModule.CATTLE_GUICE_SOURCE, true);
			if (key.getAnnotationType() != null) {
				bean.addQualifier(new AutowireCandidateQualifier(Qualifier.class, getValueAttributeForNamed(key)));
				bean.addQualifier(new AutowireCandidateQualifier(key.getAnnotationType(), getValueAttributeForNamed(key)));
			}
			registry.registerBeanDefinition(extractName(key), bean);
		}
	}

	private String extractName(Key< ? > key) {
		final String className = key.getTypeLiteral().getType().getTypeName();
		String valueAttribute = getValueAttributeForNamed(key);
		if (valueAttribute != null) {
			return valueAttribute + "_" + className;
		}
		else {
			return className;
		}
	}

	private String getValueAttributeForNamed(Key< ? > key) {
		if (key.getAnnotation() instanceof Named) {
			return ((Named) key.getAnnotation()).value();
		}
		else if (key.getAnnotation() instanceof javax.inject.Named) {
			return ((javax.inject.Named) key.getAnnotation()).value();
		}
		else if (key.getAnnotationType() != null) {
			return key.getAnnotationType().getName();
		}
		else {
			return null;
		}
	}
}