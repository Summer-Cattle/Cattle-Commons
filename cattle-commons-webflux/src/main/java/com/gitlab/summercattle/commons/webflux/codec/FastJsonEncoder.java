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
package com.gitlab.summercattle.commons.webflux.codec;

import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FastJsonEncoder extends FastJsonCodecSupport implements Encoder<Object> {

	@Override
	public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
		Class< ? > clazz = elementType.toClass();
		return supportsMimeType(mimeType) && (Object.class == clazz || (!String.class.isAssignableFrom(elementType.resolve(clazz))
				&& (JSONObject.class == elementType.resolve(clazz) || JSONArray.class == elementType.resolve(clazz))));
	}

	@Override
	public Flux<DataBuffer> encode(Publisher< ? extends Object> inputStream, DataBufferFactory bufferFactory, ResolvableType elementType,
			@Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {
		if (inputStream instanceof Mono) {
			return Mono.from(inputStream).map(value -> encodeValue(value, bufferFactory)).flux();
		}
		return null;
	}

	@Override
	public DataBuffer encodeValue(Object value, DataBufferFactory bufferFactory, ResolvableType valueType, @Nullable MimeType mimeType,
			@Nullable Map<String, Object> hints) {
		return encodeValue(value, bufferFactory);
	}

	private DataBuffer encodeValue(Object value, DataBufferFactory bufferFactory) {
		DataBuffer buffer = bufferFactory.allocateBuffer();
		boolean release = true;
		try {
			byte[] bytes = JSON.toJSONBytes(value, SerializerFeature.WriteMapNullValue, SerializerFeature.DisableCircularReferenceDetect);
			buffer.write(bytes);
			release = false;
			return buffer;
		}
		finally {
			if (release) {
				DataBufferUtils.release(buffer);
			}
		}
	}

	@Override
	public List<MimeType> getEncodableMimeTypes() {
		return getMimeTypes();
	}
}