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
package io.github.summercattle.commons.utils.spring;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.github.summercattle.commons.exception.CommonRuntimeException;

@Component
public class SpringContext implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(SpringContext.class);

	private static ApplicationContext applicationContext;

	private static ConfigurableListableBeanFactory beanFactory;

	@Value("${spring.restTemplate.timeout:30}")
	private int httpTimeout;

	@Value("${spring.restTemplate.maxTotal:500}")
	private int maxTotal;

	@Value("${spring.restTemplate.defaultMaxPerRoute:250}")
	private int defaultMaxPerRoute;

	@Bean
	@ConditionalOnMissingBean
	public RestTemplate restTemplate() {
		logger.debug("RestTemplate的连接缺省超时设置为:" + httpTimeout + "秒,最大连接数:" + maxTotal + ",最大并发数:" + defaultMaxPerRoute);
		PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
		poolingConnectionManager.setMaxTotal(maxTotal);
		poolingConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
		HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(poolingConnectionManager).build();
		int timeout = httpTimeout * 1000;
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectionRequestTimeout(timeout);
		httpRequestFactory.setConnectTimeout(timeout);
		httpRequestFactory.setReadTimeout(timeout);
		httpRequestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		return restTemplate;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContext.applicationContext = applicationContext;
	}

	public static String getApplicationName() {
		return getProperty("spring.application.name");
	}

	public static String getContextPath() {
		String contextPath = getProperty("server.servlet.context-path");
		if (StringUtils.isBlank(contextPath)) {
			contextPath = "/";
		}
		return contextPath;
	}

	public static <T> T getBean(Class<T> requiredType) {
		if (null == requiredType) {
			throw new CommonRuntimeException("Bean的类型为空");
		}
		try {
			if (null != applicationContext) {
				return applicationContext.getBean(requiredType);
			}
			if (null != beanFactory) {
				return beanFactory.getBean(requiredType);
			}
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new CommonRuntimeException("没有找到类型'" + requiredType.getName() + "'的Bean");
		}
		throw new CommonRuntimeException("应用程序上下文为空");
	}

	public static Object getBean(String name) {
		if (StringUtils.isBlank(name)) {
			throw new CommonRuntimeException("Bean的名称为空");
		}
		try {
			if (null != applicationContext) {
				return applicationContext.getBean(name);
			}
			if (null != beanFactory) {
				return beanFactory.getBean(name);
			}
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new CommonRuntimeException("没有找到名称'" + name + "'的Bean");
		}
		throw new CommonRuntimeException("应用程序上下文为空");
	}

	public static boolean containsProperty(String key) {
		checkApplicationContext();
		return applicationContext.getEnvironment().containsProperty(key);
	}

	public static String getProperty(String key) {
		checkApplicationContext();
		return applicationContext.getEnvironment().getProperty(key);
	}

	private static void checkApplicationContext() {
		if (null == applicationContext) {
			throw new CommonRuntimeException("应用程序上下文为空");
		}
	}

	public static ApplicationContext getApplicationContext() {
		checkApplicationContext();
		return applicationContext;
	}

	public static Environment getEnvironment() {
		checkApplicationContext();
		return applicationContext.getEnvironment();
	}

	public static void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		SpringContext.beanFactory = beanFactory;
	}
}