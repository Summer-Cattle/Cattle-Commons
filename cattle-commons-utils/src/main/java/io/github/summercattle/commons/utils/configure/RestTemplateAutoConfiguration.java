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
package io.github.summercattle.commons.utils.configure;

import java.nio.charset.Charset;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;
import io.github.summercattle.commons.utils.auxiliary.SSLUtils;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

@EnableConfigurationProperties(RestTemplateProperties.class)
public class RestTemplateAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(RestTemplateAutoConfiguration.class);

	@Autowired
	private RestTemplateProperties restTemplateProperties;

	@Bean("restTemplate")
	@ConditionalOnMissingBean
	public RestTemplate restTemplate() {
		logger.debug("RestTemplate的连接缺省超时设置为:" + restTemplateProperties.getTimeout() + "秒,最大连接数:" + restTemplateProperties.getMaxTotal() + ",最大并发数:"
				+ restTemplateProperties.getDefaultMaxPerRoute());
		PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
		poolingConnectionManager.setMaxTotal(restTemplateProperties.getMaxTotal());
		poolingConnectionManager.setDefaultMaxPerRoute(restTemplateProperties.getDefaultMaxPerRoute());
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setConnectionManager(poolingConnectionManager);
		try {
			KeyStore keyStore = null;
			KeyStore trustStore = null;
			if (StringUtils.isNotBlank(restTemplateProperties.getKeystoreURL())) {
				keyStore = SSLUtils.createKeyStore(restTemplateProperties.getKeystoreURL(), restTemplateProperties.getKeystorePassword());
			}
			if (StringUtils.isNotBlank(restTemplateProperties.getTruststoreURL())) {
				trustStore = SSLUtils.createKeyStore(restTemplateProperties.getTruststoreURL(), restTemplateProperties.getTruststorePassword());
			}
			SSLContext sslContext = SSLUtils.createSSLContext(keyStore,
					StringUtils.isNotBlank(restTemplateProperties.getKeyPassword()) ? restTemplateProperties.getKeyPassword()
							: restTemplateProperties.getKeystorePassword(),
					trustStore);
			httpClientBuilder.setSSLContext(sslContext);
		}
		catch (CommonException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		int timeout = restTemplateProperties.getTimeout() * 1000;
		Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setConnectionRequestTimeout(timeout);
		requestConfigBuilder.setConnectTimeout(timeout);
		requestConfigBuilder.setSocketTimeout(timeout);
		if (restTemplateProperties.isUseProxy()) {
			if (StringUtils.isBlank(restTemplateProperties.getProxyHost())) {
				throw new CommonRuntimeException("代理地址为空");
			}
			if (restTemplateProperties.getProxyPort() <= 0) {
				throw new CommonRuntimeException("代理地址端口必须大于0");
			}
			requestConfigBuilder.setProxy(new HttpHost(restTemplateProperties.getProxyHost(), restTemplateProperties.getProxyPort()));
		}
		httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClientBuilder.build());
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		return restTemplate;
	}
}