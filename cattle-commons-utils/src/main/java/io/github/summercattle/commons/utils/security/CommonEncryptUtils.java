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
package io.github.summercattle.commons.utils.security;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.security.constants.CommonEncryptType;
import io.github.summercattle.commons.utils.security.constants.PaddingType;

public class CommonEncryptUtils {

	public static byte[] getEncryptKey(CommonEncryptType encryptType, int keySize) throws CommonException {
		if (null == encryptType) {
			throw new CommonException("加密类型为空");
		}
		if (encryptType == CommonEncryptType.AES) {
			if (keySize != 128 && keySize != 192 && keySize != 256) {
				throw new CommonException("密钥长度只能为128位或192位或256位");
			}
		}
		else if (encryptType == CommonEncryptType.DES) {
			if (keySize != 56) {
				throw new CommonException("密钥长度只能为56位");
			}
		}
		else if (encryptType == CommonEncryptType.DESede) {
			if (keySize != 112 && keySize != 168) {
				throw new CommonException("密钥长度只能为112位或168位");
			}
		}
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptType.toString());
			keyGenerator.init(keySize, new SecureRandom());
			SecretKey secretKey = keyGenerator.generateKey();
			return secretKey.getEncoded();
		}
		catch (NoSuchAlgorithmException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] encryptCBC(CommonEncryptType encryptType, byte[] data, byte[] key, byte[] iv, PaddingType paddingType)
			throws CommonException {
		if (null == encryptType) {
			throw new CommonException("加密类型为空");
		}
		if (null == data) {
			throw new CommonException("数据为空");
		}
		if (null == key) {
			throw new CommonException("密钥为空");
		}
		if (null == iv) {
			throw new CommonException("IV初始向量为空");
		}
		if (null == paddingType) {
			throw new CommonException("填充类型为空");
		}
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, encryptType.toString());
			Cipher cipher = Cipher.getInstance(encryptType.toString() + "/CBC/" + paddingType.toString());
			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(encryptType.toString());
			algorithmParameters.init(new IvParameterSpec(iv));
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, algorithmParameters);
			return cipher.doFinal(data);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException
				| InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] decyrptCBC(CommonEncryptType encryptType, byte[] encryptData, byte[] key, byte[] iv, PaddingType paddingType)
			throws CommonException {
		if (null == encryptType) {
			throw new CommonException("加密类型为空");
		}
		if (null == encryptData) {
			throw new CommonException("加密后的数据为空");
		}
		if (null == key) {
			throw new CommonException("密钥为空");
		}
		if (null == iv) {
			throw new CommonException("IV初始向量为空");
		}
		if (null == paddingType) {
			throw new CommonException("填充类型为空");
		}
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, encryptType.toString());
			Cipher cipher = Cipher.getInstance(encryptType.toString() + "/CBC/" + paddingType.toString());
			AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(encryptType.toString());
			algorithmParameters.init(new IvParameterSpec(iv));
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, algorithmParameters);
			return cipher.doFinal(encryptData);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] encryptECB(CommonEncryptType encryptType, byte[] data, byte[] key, PaddingType paddingType) throws CommonException {
		if (null == encryptType) {
			throw new CommonException("加密类型为空");
		}
		if (null == data) {
			throw new CommonException("数据为空");
		}
		if (null == key) {
			throw new CommonException("密钥为空");
		}
		if (null == paddingType) {
			throw new CommonException("填充类型为空");
		}
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, encryptType.toString());
			String transformation = encryptType.toString() + "/ECB/" + paddingType.toString();
			Cipher cipher;
			if (encryptType == CommonEncryptType.AES && paddingType == PaddingType.PKCS7Padding) {
				cipher = Cipher.getInstance(transformation, getProvider());
			}
			else {
				cipher = Cipher.getInstance(transformation);
			}
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			return cipher.doFinal(data);
		}
		catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] decyrptECB(CommonEncryptType encryptType, byte[] encryptData, byte[] key, PaddingType paddingType) throws CommonException {
		if (null == encryptType) {
			throw new CommonException("加密类型为空");
		}
		if (null == encryptData) {
			throw new CommonException("加密后的数据为空");
		}
		if (null == key) {
			throw new CommonException("密钥为空");
		}
		if (null == paddingType) {
			throw new CommonException("填充类型为空");
		}
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, encryptType.toString());
			String transformation = encryptType.toString() + "/ECB/" + paddingType.toString();
			Cipher cipher;
			if (encryptType == CommonEncryptType.AES && paddingType == PaddingType.PKCS7Padding) {
				cipher = Cipher.getInstance(transformation, getProvider());
			}
			else {
				cipher = Cipher.getInstance(transformation);
			}
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			return cipher.doFinal(encryptData);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private static Provider getProvider() throws CommonException {
		return new BouncyCastleProvider();
	}
}