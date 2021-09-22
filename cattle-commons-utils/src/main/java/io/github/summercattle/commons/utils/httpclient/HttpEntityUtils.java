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
package io.github.summercattle.commons.utils.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.HttpUtils;
import io.github.summercattle.commons.utils.auxiliary.SSLUtils;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class HttpEntityUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpEntityUtils.class);

	private static final int HTTP_CONNECT_RETRY = 5;

	public static HttpClientResult post(String url, Map<String, String> headers, Object entity, ContentType contentType, CharsetType charsetType,
			int timeout, String keystoreURL, String keystorePassword, String keyPassword, String truststoreURL, String truststorePassword)
			throws CommonException {
		return httpRequest(new HttpPost(url), url, headers, entity, contentType, charsetType, timeout, keystoreURL, keystorePassword, keyPassword,
				truststoreURL, truststorePassword);
	}

	public static HttpClientResult put(String url, Map<String, String> headers, Object entity, ContentType contentType, CharsetType charsetType,
			int timeout, String keystoreURL, String keystorePassword, String keyPassword, String truststoreURL, String truststorePassword)
			throws CommonException {
		return httpRequest(new HttpPut(url), url, headers, entity, contentType, charsetType, timeout, keystoreURL, keystorePassword, keyPassword,
				truststoreURL, truststorePassword);
	}

	public static HttpClientResult get(String url, Map<String, String> headers, int timeout, String keystoreURL, String keystorePassword,
			String keyPassword, String truststoreURL, String truststorePassword) throws CommonException {
		return httpRequest(new HttpGet(url), url, headers, null, null, null, timeout, keystoreURL, keystorePassword, keyPassword, truststoreURL,
				truststorePassword);
	}

	public static HttpClientResult delete(String url, Map<String, String> headers, int timeout, String keystoreURL, String keystorePassword,
			String keyPassword, String truststoreURL, String truststorePassword) throws CommonException {
		return httpRequest(new HttpDelete(url), url, headers, null, null, null, timeout, keystoreURL, keystorePassword, keyPassword, truststoreURL,
				truststorePassword);
	}

	@SuppressWarnings("unchecked")
	private static HttpClientResult httpRequest(HttpRequestBase httpRequest, String url, Map<String, String> headers, Object entity,
			ContentType contentType, CharsetType charsetType, int timeout, String keystoreURL, String keystorePassword, String keyPassword,
			String truststoreURL, String truststorePassword) throws CommonException {
		SSLContext sslContext = null;
		if (HttpUtils.isSSL(url)) {
			KeyStore keyStore = null;
			if (StringUtils.isNotBlank(keystoreURL)) {
				keyStore = SSLUtils.createKeyStore(keystoreURL, keystorePassword);
			}
			KeyStore trustStore = null;
			if (StringUtils.isNotBlank(truststoreURL)) {
				trustStore = SSLUtils.createKeyStore(truststoreURL, truststorePassword);
			}
			sslContext = SSLUtils.createSSLContext(keyStore, StringUtils.isNotBlank(keyPassword) ? keyPassword : keystorePassword, trustStore);
		}
		CloseableHttpClient httpClient = sslContext != null ? HttpClients.custom().setSSLContext(sslContext).build() : HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			if (headers != null) {
				Iterator<String> iterator = headers.keySet().iterator();
				while (iterator.hasNext()) {
					String name = iterator.next();
					String value = headers.get(name);
					httpRequest.addHeader(name, value);
				}
			}
			if (entity != null && httpRequest instanceof HttpEntityEnclosingRequestBase) {
				if (entity instanceof Map) {
					Map<String, String> lEntity = (Map<String, String>) entity;
					List<NameValuePair> nvps = new Vector<NameValuePair>();
					Iterator<String> iterator = lEntity.keySet().iterator();
					while (iterator.hasNext()) {
						String name = iterator.next();
						String value = lEntity.get(name);
						nvps.add(new BasicNameValuePair(name, value));
					}
					if (charsetType == CharsetType.NONE) {
						((HttpEntityEnclosingRequestBase) httpRequest).setEntity(new UrlEncodedFormEntity(nvps));
					}
					else {
						((HttpEntityEnclosingRequestBase) httpRequest).setEntity(new UrlEncodedFormEntity(nvps, toHttpClientCharset(charsetType)));
					}
				}
				else if (entity instanceof String) {
					String lEntity = (String) entity;
					if (StringUtils.isNotBlank(lEntity)) {
						if (contentType != null) {
							((HttpEntityEnclosingRequestBase) httpRequest)
									.setEntity(new StringEntity(lEntity, toHttpClientContentType(contentType, charsetType)));
						}
						else if (charsetType == CharsetType.NONE) {
							((HttpEntityEnclosingRequestBase) httpRequest).setEntity(new StringEntity(lEntity));
						}
						else {
							((HttpEntityEnclosingRequestBase) httpRequest).setEntity(new StringEntity(lEntity, toHttpClientCharset(charsetType)));
						}
					}
				}
				else {
					throw new CommonException("无效的实体类型:" + ReflectUtils.getClassType(entity.getClass()).toString());
				}
			}
			httpRequest.setConfig(getRequestConfig(timeout));
			response = (CloseableHttpResponse) doTryHttpConnect(url, new HttpConnectProcess() {

				public Object process() throws Throwable {
					return httpClient.execute(httpRequest);
				}
			});
			StatusLine status = response.getStatusLine();
			boolean success = status.getStatusCode() == HttpURLConnection.HTTP_OK || status.getStatusCode() == HttpURLConnection.HTTP_CREATED
					|| status.getStatusCode() == HttpURLConnection.HTTP_ACCEPTED;
			Header[] responseHeaders = response.getAllHeaders();
			Map<String, String> mHeaders = new HashMap<String, String>();
			for (Header responseHeader : responseHeaders) {
				mHeaders.put(responseHeader.getName(), responseHeader.getValue());
			}
			HttpEntity responseEntity = response.getEntity();
			String responseContentType = responseEntity.getContentType().getValue();
			InputStream inputStream = new ByteArrayInputStream(EntityUtils.toByteArray(responseEntity));
			if (responseEntity.isChunked()) {
				EntityUtils.consume(responseEntity);
			}
			return new HttpClientResult(success, status.getStatusCode(), mHeaders, status.getReasonPhrase(), responseContentType, inputStream);
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			if (httpRequest != null) {
				httpRequest.releaseConnection();
			}
			if (response != null) {
				try {
					response.close();
				}
				catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (httpClient != null) {
				try {
					httpClient.close();
				}
				catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private static Charset toHttpClientCharset(CharsetType charsetType) throws CommonException {
		Charset charset = null;
		switch (charsetType) {
			case ASCII:
				charset = Consts.ASCII;
				break;
			case ISO_8859_1:
				charset = Consts.ISO_8859_1;
				break;
			case UTF_8:
				charset = Consts.UTF_8;
				break;
			default:
				throw new CommonException("HttpClient不支持的字符集类型:" + charsetType.toString());
		}
		return charset;
	}

	private static org.apache.http.entity.ContentType toHttpClientContentType(ContentType contentType, CharsetType charsetType)
			throws CommonException {
		Charset charset = null;
		if (charsetType != null && charsetType != CharsetType.NONE) {
			charset = toHttpClientCharset(charsetType);
		}
		String mimeType;
		switch (contentType) {
			case APPLICATION_ATOM_XML:
				mimeType = "application/atom+xml";
				break;
			case APPLICATION_FORM_URLENCODED:
				mimeType = "application/x-www-form-urlencoded";
				break;
			case APPLICATION_JSON:
				mimeType = "application/json";
				break;
			case APPLICATION_OCTET_STREAM:
				mimeType = "application/octet-stream";
				break;
			case APPLICATION_SVG_XML:
				mimeType = "application/svg+xml";
				break;
			case APPLICATION_XHTML_XML:
				mimeType = "application/xhtml+xml";
				break;
			case APPLICATION_XML:
				mimeType = "application/xml";
				break;
			case MULTIPART_FORM_DATA:
				mimeType = "multipart/form-data";
				break;
			case TEXT_HTML:
				mimeType = "text/html";
				break;
			case TEXT_PLAIN:
				mimeType = "text/plain";
				break;
			case TEXT_XML:
				mimeType = "text/xml";
				break;
			case WILDCARD:
				mimeType = "*/*";
				break;
			default:
				throw new CommonException("HttpClient不支持的内容类型:" + contentType.toString());
		}
		return org.apache.http.entity.ContentType.create(mimeType, charset);
	}

	private static RequestConfig getRequestConfig(int timeout) {
		RequestConfig.Builder builder = RequestConfig.custom();
		if (timeout > 0) {
			int lTimeout = timeout * 1000;
			builder.setSocketTimeout(lTimeout);
			builder.setConnectTimeout(lTimeout);
		}
		return builder.build();
	}

	private static Object doTryHttpConnect(String url, HttpConnectProcess httpConnectProcess) throws CommonException {
		Object result = null;
		CommonException exception = null;
		for (int i = 0; i <= HTTP_CONNECT_RETRY; i++) {
			logger.debug("第" + String.valueOf(i + 1) + "次重试连接地址" + url);
			try {
				result = httpConnectProcess.process();
				exception = null;
				break;
			}
			catch (Throwable e) {
				boolean canTry = false;
				if (e instanceof ConnectException) {
					canTry = true;
				}
				else {
					Throwable cause = e.getCause();
					while (cause != null) {
						if (cause instanceof ConnectException) {
							canTry = true;
							break;
						}
						cause = cause.getCause();
					}
				}
				if (canTry) {
					exception = new CommonException("连接Http地址'" + url + "'失败", e);
				}
				else {
					exception = ExceptionWrapUtils.wrap(e);
				}
				if (i < HTTP_CONNECT_RETRY && !canTry) {
					break;
				}
			}
		}
		if (exception != null) {
			throw exception;
		}
		return result;
	}
}