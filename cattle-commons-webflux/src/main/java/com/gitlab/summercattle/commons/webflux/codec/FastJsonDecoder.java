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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;

import com.alibaba.fastjson.JSON;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FastJsonDecoder extends FastJsonCodecSupport implements Decoder<Object> {

	@Override
	public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
		return (!CharSequence.class.isAssignableFrom(elementType.toClass()) && supportsMimeType(mimeType));
	}

	@Override
	public Flux<Object> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
		return Flux.from(inputStream).map(buffer -> decode(buffer, elementType));
	}

	@Override
	public Mono<Object> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
		return DataBufferUtils.join(inputStream).map(dataBuffer -> decode(dataBuffer, elementType));
	}

	private Object decode(DataBuffer buffer, ResolvableType elementType) {
		try {
			InputStream is = buffer.asInputStream();
			Class< ? > clazz = elementType.toClass();
			return JSON.parseObject(is, (Type) clazz);
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
		finally {
			DataBufferUtils.release(buffer);
		}
	}

	@Override
	public List<MimeType> getDecodableMimeTypes() {
		return getMimeTypes();
	}
}