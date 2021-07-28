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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class CompressUtils {

	public static byte[] compress(String name, byte[] datas) throws CommonException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (CompressorOutputStream cos = CompressorStreamFactory.getSingleton().createCompressorOutputStream(name, baos)) {
				cos.write(datas);
			}
			return baos.toByteArray();
		}
		catch (IOException | CompressorException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] decompress(String name, byte[] datas) throws CommonException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(datas)) {
			return decompress(name, bais);
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static byte[] decompress(String name, InputStream is) throws CommonException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (CompressorInputStream cis = CompressorStreamFactory.getSingleton().createCompressorInputStream(name, is)) {
				byte[] bytes = new byte[1024];
				int count = 0;
				while ((count = cis.read(bytes)) >= 0) {
					baos.write(bytes, 0, count);
				}
			}
			return baos.toByteArray();
		}
		catch (IOException | CompressorException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}