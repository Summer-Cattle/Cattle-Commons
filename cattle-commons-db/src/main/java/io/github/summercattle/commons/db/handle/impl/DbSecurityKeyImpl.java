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
package io.github.summercattle.commons.db.handle.impl;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

import com.google.inject.Inject;

import io.github.summercattle.commons.db.handle.DbSecurityKey;
import io.github.summercattle.commons.db.handle.DbTool;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.security.CommonEncryptUtils;
import io.github.summercattle.commons.utils.security.RSAUtils;
import io.github.summercattle.commons.utils.security.constants.CommonEncryptType;
import io.github.summercattle.commons.utils.security.constants.PaddingType;

public class DbSecurityKeyImpl implements DbSecurityKey {

	private static final String RSA_PUBLIC_KEY = "RsaPublicKey";

	private static final String RSA_PRIVATE_KEY = "RsaPrivateKey";

	private static final String COMMON_ENCRYPT_KEY = "CommonEncryptKey_";

	private byte[] rsaPublicKey;

	private byte[] rsaPrivateKey;

	private Map<CommonEncryptType, byte[]> commonEncryptKey = new HashMap<CommonEncryptType, byte[]>();

	static byte[] KEY = new byte[] { -37, 103, -12, -60, -65, 58, -17, 91, -91, 54, 39, -11, -68, -35, 108, 22, -86, 78, 117, -98, 82, -76, -2, -29,
			-13, -117, 62, 60, 21, 91, -45, -59 };

	@Inject
	private DbTool dbTool;

	@Override
	public RSAPublicKey getRSAPublicKey() throws CommonException {
		if (null == rsaPublicKey) {
			checkRSAKey();
		}
		return RSAUtils.getPublicKey(rsaPublicKey);
	}

	@Override
	public RSAPrivateKey getRSAPrivateKey() throws CommonException {
		if (null == rsaPrivateKey) {
			checkRSAKey();
		}
		return RSAUtils.getPrivateKey(rsaPrivateKey);
	}

	private synchronized void checkRSAKey() throws CommonException {
		rsaPublicKey = (byte[]) dbTool.getConfig(RSA_PUBLIC_KEY);
		rsaPrivateKey = (byte[]) dbTool.getConfig(RSA_PRIVATE_KEY);
		if ((null == rsaPublicKey || rsaPublicKey.length == 0) && (null == rsaPrivateKey || rsaPrivateKey.length == 0)) {
			byte[][] encryptKey = RSAUtils.getEncryptKey(2048);
			rsaPublicKey = encryptKey[0];
			rsaPrivateKey = encryptKey[1];
			dbTool.saveConfig(RSA_PUBLIC_KEY, true, rsaPublicKey);
			dbTool.saveConfig(RSA_PRIVATE_KEY, true, rsaPrivateKey);
		}
		else {
			if ((null == rsaPublicKey || rsaPublicKey.length == 0) || (null == rsaPrivateKey || rsaPrivateKey.length == 0)) {
				throw new CommonException("RSA密钥异常");
			}
		}
	}

	@Override
	public byte[] getCommonEncryptKey(CommonEncryptType encryptType) throws CommonException {
		if (!commonEncryptKey.containsKey(encryptType)) {
			checkCommonEncryptKey(encryptType);
		}
		return commonEncryptKey.get(encryptType);
	}

	private synchronized void checkCommonEncryptKey(CommonEncryptType encryptType) throws CommonException {
		String key = COMMON_ENCRYPT_KEY + encryptType.toString();
		byte[] encryptKey;
		if (encryptType == CommonEncryptType.AES) {
			byte[] lEncryptKey = (byte[]) dbTool.getConfig(key);
			if (null == lEncryptKey || lEncryptKey.length == 0) {
				encryptKey = CommonEncryptUtils.getEncryptKey(encryptType, 256);
				commonEncryptKey.put(encryptType, encryptKey);
				lEncryptKey = StringUtils.getBytesUtf8(Hex.encodeHexString(
						Base64.encodeBase64(CommonEncryptUtils.encryptECB(CommonEncryptType.AES, encryptKey, KEY, PaddingType.PKCS7Padding)), false));
				dbTool.saveConfig(key, false, lEncryptKey);
			}
			else {
				try {
					encryptKey = CommonEncryptUtils.decyrptECB(CommonEncryptType.AES,
							Base64.decodeBase64(Hex.decodeHex(StringUtils.newStringUtf8(lEncryptKey))), KEY, PaddingType.PKCS7Padding);
					commonEncryptKey.put(encryptType, encryptKey);
				}
				catch (DecoderException e) {
					throw ExceptionWrapUtils.wrap(e);
				}
			}
		}
		else {
			throw new CommonException("通用加密算法'" + encryptType.toString() + "'暂不支持");
		}
	}
}