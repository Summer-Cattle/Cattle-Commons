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
package com.gitlab.summercattle.commons.db.configure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.gitlab.summercattle.commons.db.DbUtils;
import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.event.DbStartupEvent;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.reflect.ClassUtils;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;

@Component
public class DbBeanPostProcessor implements BeanPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DbBeanPostProcessor.class);

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Dialect) {
			try {
				executeStartup();
			}
			catch (CommonException e) {
				throw ExceptionWrapUtils.wrapRuntime(e);
			}
		}
		return bean;
	}

	private void executeStartup() throws CommonException {
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		if (dbProperties.isGenerate()) {
			DbUtils.getDbStruct().check(dbProperties);
		}
		Class<DbStartupEvent>[] classes = ClassUtils.getSubTypesOf(DbStartupEvent.class);
		for (Class<DbStartupEvent> clazz : classes) {
			DbStartupEvent startupEvent = ClassUtils.instance(clazz);
			DbUtils.getDbTransaction().doDal(ctx -> {
				logger.debug("执行启动事件,类'" + clazz.getName() + "'");
				startupEvent.execute(ctx);
				return null;
			});
		}
	}
}