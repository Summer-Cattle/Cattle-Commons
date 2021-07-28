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
package io.github.summercattle.commons.utils.auxiliary;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.exception.CommonRuntimeException;

class SslX509TrustManager implements X509TrustManager {

	/** Log object for this class. */
	private static Logger logger = LoggerFactory.getLogger(SslX509TrustManager.class);

	private X509TrustManager defaultTrustManager = null;

	/**
	 * Constructor for AuthSSLX509TrustManager.
	 */
	public SslX509TrustManager(X509TrustManager defaultTrustManager) {
		if (defaultTrustManager == null) {
			throw new CommonRuntimeException("Trust管理器不可以为空");
		}
		this.defaultTrustManager = defaultTrustManager;
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
	 */
	public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
		if (logger.isInfoEnabled() && certificates != null) {
			for (int c = 0; c < certificates.length; c++) {
				X509Certificate cert = certificates[c];
				logger.info(" Client certificate " + (c + 1) + ":");
				logger.info("  Subject DN: " + cert.getSubjectDN());
				logger.info("  Signature Algorithm: " + cert.getSigAlgName());
				logger.info("  Valid from: " + cert.getNotBefore());
				logger.info("  Valid until: " + cert.getNotAfter());
				logger.info("  Issuer: " + cert.getIssuerDN());
			}
		}
		defaultTrustManager.checkClientTrusted(certificates, authType);
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
	 */
	public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
		if (logger.isInfoEnabled() && certificates != null) {
			for (int c = 0; c < certificates.length; c++) {
				X509Certificate cert = certificates[c];
				logger.info(" Server certificate " + (c + 1) + ":");
				logger.info("  Subject DN: " + cert.getSubjectDN());
				logger.info("  Signature Algorithm: " + cert.getSigAlgName());
				logger.info("  Valid from: " + cert.getNotBefore());
				logger.info("  Valid until: " + cert.getNotAfter());
				logger.info("  Issuer: " + cert.getIssuerDN());
			}
		}
		defaultTrustManager.checkServerTrusted(certificates, authType);
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	public X509Certificate[] getAcceptedIssuers() {
		return this.defaultTrustManager.getAcceptedIssuers();
	}
}