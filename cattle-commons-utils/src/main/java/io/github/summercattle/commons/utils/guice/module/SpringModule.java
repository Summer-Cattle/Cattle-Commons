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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;

import io.github.summercattle.commons.utils.guice.annotation.GuiceBindBean;

public class SpringModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(SpringModule.class);

	public static final String CATTLE_GUICE_SOURCE = "cattle-guice";

	private ConfigurableListableBeanFactory beanFactory;

	public SpringModule(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	protected void configure() {
		if (beanFactory.getBeanNamesForType(ProvisionListener.class).length > 0) {
			binder().bindListener(Matchers.any(), beanFactory.getBeansOfType(ProvisionListener.class).values().toArray(new ProvisionListener[0]));
		}
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAutowireCandidateResolver(new GuiceAutowireCandidateResolver(binder().getProvider(Injector.class)));
		}
		bind(beanFactory);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void bind(ConfigurableListableBeanFactory beanFactory) {
		for (String name : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getBeanDefinition(name);
			if (definition.hasAttribute(CATTLE_GUICE_SOURCE)) {
				continue;
			}
			if (definition.isAutowireCandidate() && definition.getRole() == AbstractBeanDefinition.ROLE_APPLICATION) {
				Class< ? > clazz = beanFactory.getType(name);
				if (clazz == null) {
					continue;
				}
				GuiceBindBean guiceBindBean = clazz.getAnnotation(GuiceBindBean.class);
				if (null != guiceBindBean) {
					BeanProvider beanFactoryProvider = new BeanProvider(beanFactory, name, clazz);
					binder().bind(clazz).toProvider(beanFactoryProvider);
					logger.debug("注入Guice,实现类:'" + clazz.getName() + "'");
				}
			}
		}
	}
}