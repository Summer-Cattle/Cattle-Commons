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
package com.gitlab.summercattle.commons.utils.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class RSAUtils {

	private static final int ENCRYPT_SEG = 64;

	private static final int DECRYPT_SEG = 128;

	public static byte[][] getEncryptKey(int keySize) throws CommonException {
		if (keySize < 512) {
			throw new CommonException("密钥长度必须至少有512位长");
		}
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(keySize, new SecureRandom());
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			return new byte[][] { keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded() };
		}
		catch (NoSuchAlgorithmException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static RSAPublicKey getPublicKey(byte[] publicKey) throws CommonException {
		if (publicKey == null) {
			throw new CommonException("公有密钥为空");
		}
		try {
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static RSAPrivateKey getPrivateKey(byte[] privateKey) throws CommonException {
		if (privateKey == null) {
			throw new CommonException("私有密钥为空");
		}
		try {
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		}
		catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] sign(byte[] data, RSAPrivateKey privateKey) throws CommonException {
		if (data == null) {
			throw new CommonException("需签名的数据为空");
		}
		if (privateKey == null) {
			throw new CommonException("私有密钥为空");
		}
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		}
		catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static boolean verify(byte[] data, RSAPublicKey publicKey, byte[] sign) throws CommonException {
		if (data == null) {
			throw new CommonException("数据为空");
		}
		if (publicKey == null) {
			throw new CommonException("公有密钥为空");
		}
		if (sign == null) {
			throw new CommonException("签名为空");
		}
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sign);
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) throws CommonException {
		byte[] result = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int length = data.length;
			int pos = 0;
			if (length == 0) {
				byte[] tmpResult = doEncryptByPublicKey(data, publicKey);
				baos.write(tmpResult);
			}
			else {
				while (pos < length) {
					byte[] tmp = null;
					if (pos + ENCRYPT_SEG < length) {
						tmp = Arrays.copyOfRange(data, pos, pos + ENCRYPT_SEG);
					}
					else {
						tmp = Arrays.copyOfRange(data, pos, length);
					}
					byte[] tmpResult = doEncryptByPublicKey(tmp, publicKey);
					baos.write(tmpResult);
					pos += ENCRYPT_SEG;
				}
			}
			result = baos.toByteArray();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		return result;
	}

	public static byte[] doEncryptByPublicKey(byte[] data, RSAPublicKey publicKey) throws CommonException {
		if (data == null) {
			throw new CommonException("数据为空");
		}
		if (publicKey == null) {
			throw new CommonException("公有密钥为空");
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(data);
		}
		catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] encryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) throws CommonException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int length = data.length;
			int pos = 0;
			if (length == 0) {
				byte[] tmpResult = doEncryptByPrivateKey(data, privateKey);
				baos.write(tmpResult);
			}
			else {
				while (pos < length) {
					byte[] tmp = null;
					if (pos + ENCRYPT_SEG < length) {
						tmp = Arrays.copyOfRange(data, pos, pos + ENCRYPT_SEG);
					}
					else {
						tmp = Arrays.copyOfRange(data, pos, length);
					}
					byte[] tmpResult = doEncryptByPrivateKey(tmp, privateKey);
					baos.write(tmpResult);
					pos += ENCRYPT_SEG;
				}
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] doEncryptByPrivateKey(byte[] data, RSAPrivateKey privateKey) throws CommonException {
		if (data == null) {
			throw new CommonException("数据为空");
		}
		if (privateKey == null) {
			throw new CommonException("私有密钥为空");
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			return cipher.doFinal(data);
		}
		catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] decryptByPublicKey(byte[] encryptData, RSAPublicKey publicKey) throws CommonException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int length = encryptData.length;
			int pos = 0;
			while (pos < length) {
				byte[] tmp = null;
				if (pos + DECRYPT_SEG < length) {
					tmp = Arrays.copyOfRange(encryptData, pos, pos + DECRYPT_SEG);
				}
				else {
					tmp = Arrays.copyOfRange(encryptData, pos, length);
				}
				byte[] tmpResult = doDecryptByPublicKey(tmp, publicKey);
				baos.write(tmpResult);
				pos += DECRYPT_SEG;
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] doDecryptByPublicKey(byte[] encryptData, RSAPublicKey publicKey) throws CommonException {
		if (encryptData == null) {
			throw new CommonException("加密后的数据为空");
		}
		if (publicKey == null) {
			throw new CommonException("公有密钥为空");
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return cipher.doFinal(encryptData);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] decryptByPrivateKey(byte[] encryptData, RSAPrivateKey privateKey) throws CommonException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int length = encryptData.length;
			int pos = 0;
			while (pos < length) {
				byte[] tmp = null;
				if (pos + DECRYPT_SEG < length) {
					tmp = Arrays.copyOfRange(encryptData, pos, pos + DECRYPT_SEG);
				}
				else {
					tmp = Arrays.copyOfRange(encryptData, pos, length);
				}
				byte[] tmpResult = doDecryptByPrivateKey(tmp, privateKey);
				baos.write(tmpResult);
				pos += DECRYPT_SEG;
			}
			baos.flush();
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] doDecryptByPrivateKey(byte[] encryptData, RSAPrivateKey privateKey) throws CommonException {
		if (encryptData == null) {
			throw new CommonException("加密后的数据为空");
		}
		if (privateKey == null) {
			throw new CommonException("私有密钥为空");
		}
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(encryptData);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}