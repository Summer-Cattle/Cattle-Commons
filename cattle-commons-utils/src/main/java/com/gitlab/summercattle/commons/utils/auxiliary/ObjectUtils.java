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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.gitlab.summercattle.commons.exception.CommonException;

public class ObjectUtils {

	public static byte[] serialize(Object object) throws CommonException {
		if (null == object) {
			return new byte[0];
		}
		if (!(object instanceof Serializable)) {
			throw new CommonException("类'" + object.getClass().getName() + "'没有继承Serializable");
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(1024); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(object);
			oos.flush();
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new CommonException("不能够被序列化:" + e.getMessage(), e);
		}
	}

	public static Object deserialize(byte[] bytes) throws CommonException {
		if (null == bytes || bytes.length == 0) {
			return null;
		}
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
			return ois.readObject();
		}
		catch (IOException | ClassNotFoundException e) {
			throw new CommonException("不能够被反序列化:" + e.getMessage(), e);
		}
	}
}