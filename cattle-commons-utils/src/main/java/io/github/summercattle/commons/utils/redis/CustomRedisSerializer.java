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
package io.github.summercattle.commons.utils.redis;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.CompressUtils;
import io.github.summercattle.commons.utils.auxiliary.ObjectUtils;

public class CustomRedisSerializer implements RedisSerializer<Object> {

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		byte[] bytes;
		try {
			bytes = ObjectUtils.serialize(object);
		}
		catch (CommonException e) {
			throw new SerializationException(e.getMessage(), e);
		}
		try {
			bytes = CompressUtils.compress(CompressorStreamFactory.GZIP, bytes);
		}
		catch (CommonException e) {
			throw new SerializationException("不能够被压缩:" + e.getMessage(), e);
		}
		return StringUtils.getBytesUtf8(Hex.encodeHexString(bytes));
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (null == bytes || bytes.length == 0) {
			return null;
		}
		try {
			bytes = Hex.decodeHex(StringUtils.newStringUtf8(bytes));
		}
		catch (DecoderException e) {
			throw new SerializationException("不能够被解码:" + e.getMessage(), e);
		}
		try {
			bytes = CompressUtils.decompress(CompressorStreamFactory.GZIP, bytes);
		}
		catch (CommonException e) {
			throw new SerializationException("不能够被解压:" + e.getMessage(), e);
		}
		try {
			return ObjectUtils.deserialize(bytes);
		}
		catch (CommonException e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}
}