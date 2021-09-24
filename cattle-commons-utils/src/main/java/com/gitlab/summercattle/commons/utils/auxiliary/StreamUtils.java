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
package com.gitlab.summercattle.commons.utils.auxiliary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class StreamUtils {

	public static String inputStream2String(InputStream is) throws CommonException {
		return inputStream2String(is, "UTF-8");
	}

	public static String inputStream2String(InputStream is, String charset) throws CommonException {
		byte[] bytes = inputStream2Bytes(is);
		try {
			return new String(bytes, charset);
		}
		catch (UnsupportedEncodingException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] inputStream2Bytes(InputStream is) throws CommonException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int c;
			while ((c = is.read(buffer, 0, 1024)) > 0) {
				baos.write(buffer, 0, c);
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}