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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.ExceptionConstants;
import io.github.summercattle.commons.utils.exception.CommonResponseException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

@Component
public class RestTemplateUtils {

	private static final Logger logger = LoggerFactory.getLogger(RestTemplateUtils.class);

	public <T, R> T post(String url, MediaType type, Map<String, String> headers, R request, Class<T> responseType) throws CommonException {
		HttpHeaders httpHeaders = getHeaders(type, headers);
		RestTemplate restTemplate = getRestTemplate();
		HttpEntity<R> requestEntity = new HttpEntity<R>(request, httpHeaders);
		try {
			ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
			return responseEntity.getBody();
		}
		catch (Throwable e) {
			if (e instanceof RestClientResponseException) {
				RestClientResponseException hseExcep = (RestClientResponseException) e;
				int exceptionCode = ExceptionConstants.GENERAL;
				if (HttpServletResponse.SC_SERVICE_UNAVAILABLE == hseExcep.getRawStatusCode()) {
					exceptionCode = ExceptionConstants.SERVICE_UNAVAILABLE;
				}
				else {
					logger.error(hseExcep.getMessage(), hseExcep);
				}
				throw new CommonResponseException(exceptionCode, hseExcep.getMessage(), hseExcep.getResponseBodyAsString());
			}
			else {
				logger.error(e.getMessage(), e);
				throw ExceptionWrapUtils.wrap(e);
			}
		}
	}

	private HttpHeaders getHeaders(MediaType type, Map<String, String> headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if (null != type) {
			httpHeaders.setContentType(type);
		}
		if (null != headers) {
			Iterator<String> iterator = headers.keySet().iterator();
			while (iterator.hasNext()) {
				String header = iterator.next();
				httpHeaders.add(header, headers.get(header));
			}
		}
		return httpHeaders;
	}

	public <T> T get(String url, MediaType type, Map<String, String> headers, Class<T> responseType) throws CommonException {
		return get(url, type, headers, responseType, null);
	}

	public <T> T get(String url, MediaType type, Map<String, String> headers, Class<T> responseType, Map<String, ? > uriVariables)
			throws CommonException {
		HttpHeaders httpHeaders = getHeaders(type, headers);
		RestTemplate restTemplate = getRestTemplate();
		HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
		try {
			ResponseEntity<T> responseEntity;
			if (null != uriVariables) {
				responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType, uriVariables);
			}
			else {
				responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
			}
			return responseEntity.getBody();
		}
		catch (Throwable e) {
			if (e instanceof RestClientResponseException) {
				RestClientResponseException hseExcep = (RestClientResponseException) e;
				int exceptionCode = ExceptionConstants.GENERAL;
				if (HttpServletResponse.SC_SERVICE_UNAVAILABLE == hseExcep.getRawStatusCode()) {
					exceptionCode = ExceptionConstants.SERVICE_UNAVAILABLE;
				}
				else {
					logger.error(hseExcep.getMessage(), hseExcep);
				}
				throw new CommonResponseException(exceptionCode, hseExcep.getMessage(), hseExcep.getResponseBodyAsString());
			}
			else {
				logger.error(e.getMessage(), e);
				throw ExceptionWrapUtils.wrap(e);
			}
		}
	}

	private RestTemplate getRestTemplate() {
		return SpringContext.getBean(RestTemplate.class);
	}
}