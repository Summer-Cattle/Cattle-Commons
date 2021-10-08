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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class SignUtils {

	public static String signSHA256(String str) throws CommonException {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			return Hex.encodeHexString(messageDigest.digest());
		}
		catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}