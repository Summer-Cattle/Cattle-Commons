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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassUtils;
import io.github.summercattle.commons.utils.reflect.Resource;

public class SSLUtils {

	private static final Logger logger = LoggerFactory.getLogger(SSLUtils.class);

	private static SSLKeyStoreType getKeyStoreType(String keystoreURL) throws CommonException {
		String url = keystoreURL.toLowerCase();
		if (url.endsWith(".p12")) {
			return SSLKeyStoreType.PKCS12;
		}
		throw new CommonException("密鈅存储文件'" + keystoreURL + "'的类型未知");
	}

	public static KeyStore createKeyStore(String keystoreURL, String keystorePassword) throws CommonException {
		if (StringUtils.isBlank(keystoreURL)) {
			throw new CommonException("密钥库地址不能为空");
		}
		SSLKeyStoreType keyStoreType = getKeyStoreType(keystoreURL);
		Resource resource = ClassUtils.getClassResourceLoader().getResource(keystoreURL);
		URL tKeystoreURL = null;
		if (null != resource) {
			tKeystoreURL = resource.getURL();
		}
		if (tKeystoreURL == null) {
			tKeystoreURL = FileUtils.getFileURL(keystoreURL);
		}
		if (tKeystoreURL == null) {
			throw new CommonException("密钥库地址'" + keystoreURL + "'不存在");
		}
		logger.debug("初始化密钥库");
		KeyStore keystore;
		try {
			keystore = KeyStore.getInstance(keyStoreType.toString());
		}
		catch (KeyStoreException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		try (InputStream is = tKeystoreURL.openStream()) {
			keystore.load(is, StringUtils.isNotBlank(keystorePassword) ? keystorePassword.toCharArray() : null);
		}
		catch (IOException | NoSuchAlgorithmException | CertificateException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		return keystore;
	}

	public static SSLContext createSSLContext(KeyStore keyStore, String keyPassword, KeyStore trustStore) throws CommonException {
		try {
			KeyManager[] keymanagers = null;
			TrustManager[] trustmanagers = null;
			if (keyStore != null) {
				if (logger.isDebugEnabled()) {
					Enumeration<String> aliases = keyStore.aliases();
					while (aliases.hasMoreElements()) {
						String alias = aliases.nextElement();
						Certificate[] certs = keyStore.getCertificateChain(alias);
						if (certs != null) {
							logger.debug("Certificate chain '" + alias + "':");
							for (int c = 0; c < certs.length; c++) {
								if (certs[c] instanceof X509Certificate) {
									X509Certificate cert = (X509Certificate) certs[c];
									logger.debug(" Certificate " + (c + 1) + ":");
									logger.debug("  Subject DN: " + cert.getSubjectDN());
									logger.debug("  Signature Algorithm: " + cert.getSigAlgName());
									logger.debug("  Valid from: " + cert.getNotBefore());
									logger.debug("  Valid until: " + cert.getNotAfter());
									logger.debug("  Issuer: " + cert.getIssuerDN());
								}
							}
						}
					}
				}
				keymanagers = createKeyManagers(keyStore, keyPassword);
			}
			if (trustStore != null) {
				if (logger.isDebugEnabled()) {
					Enumeration<String> aliases = trustStore.aliases();
					while (aliases.hasMoreElements()) {
						String alias = aliases.nextElement();
						logger.debug("Trusted certificate '" + alias + "':");
						Certificate trustedcert = trustStore.getCertificate(alias);
						if (trustedcert != null && trustedcert instanceof X509Certificate) {
							X509Certificate cert = (X509Certificate) trustedcert;
							logger.debug("  Subject DN: " + cert.getSubjectDN());
							logger.debug("  Signature Algorithm: " + cert.getSigAlgName());
							logger.debug("  Valid from: " + cert.getNotBefore());
							logger.debug("  Valid until: " + cert.getNotAfter());
							logger.debug("  Issuer: " + cert.getIssuerDN());
						}
					}
				}
				trustmanagers = createTrustManagers(trustStore);
			}
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(keymanagers, trustmanagers, null);
			return sslcontext;
		}
		catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private static KeyManager[] createKeyManagers(KeyStore keystore, String keyPassword) throws CommonException {
		if (keystore == null) {
			throw new CommonException("密钥库不能为空");
		}
		logger.debug("初始化密钥管理器");
		try {
			KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, StringUtils.isNotBlank(keyPassword) ? keyPassword.toCharArray() : null);
			return kmfactory.getKeyManagers();
		}
		catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private static TrustManager[] createTrustManagers(KeyStore keystore) throws CommonException {
		if (keystore == null) {
			throw new CommonException("密钥库不能为空");
		}
		logger.debug("初始化信任管理器");
		try {
			TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmfactory.init(keystore);
			TrustManager[] trustmanagers = tmfactory.getTrustManagers();
			for (int i = 0; i < trustmanagers.length; i++) {
				if (trustmanagers[i] instanceof X509TrustManager) {
					trustmanagers[i] = new SSLX509TrustManager((X509TrustManager) trustmanagers[i]);
				}
			}
			return trustmanagers;
		}
		catch (KeyStoreException | NoSuchAlgorithmException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}