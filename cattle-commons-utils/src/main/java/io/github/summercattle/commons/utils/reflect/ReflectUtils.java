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
package io.github.summercattle.commons.utils.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class ReflectUtils {

	public static Field getField(Class< ? > clazz, String fieldName) throws CommonException {
		return getField(clazz, fieldName, true);
	}

	private static Field getField(Class< ? > clazz, String fieldName, boolean isFirst) throws CommonException {
		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		}
		catch (SecurityException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		catch (NoSuchFieldException e) {
			if (clazz.getSuperclass() != Object.class) {
				field = getField(clazz.getSuperclass(), fieldName, false);
			}
		}
		if (field == null && isFirst) {
			throw new CommonException("在类'" + clazz.getName() + "'中没有找到字段'" + fieldName + "'");
		}
		return field;
	}

	public static void setFieldValue(Object obj, Field field, Object value) throws CommonException {
		boolean isAccessible = field.isAccessible();
		try {
			field.setAccessible(true);
			Object convertValue = null;
			if (value != null) {
				ClassType type = getClassType(field.getType());
				convertValue = convertValue(type, type == ClassType.Array ? field.getType().getComponentType()
						: (type == ClassType.Enum || type == ClassType.ClassObject ? field.getType() : null), value);
			}
			field.set(obj, convertValue);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			field.setAccessible(isAccessible);
		}
	}

	public static void setFieldValue(Object obj, String fieldName, Object value) throws CommonException {
		Field field = getField(obj.getClass(), fieldName);
		setFieldValue(obj, field, value);
	}

	public static ClassType getClassType(Class< ? > typeCls) {
		ClassType type = null;
		if (typeCls.isArray()) {
			type = ClassType.Array;
		}
		else if (typeCls.isEnum()) {
			type = ClassType.Enum;
		}
		else if (typeCls == Long.class || typeCls == long.class) {
			type = ClassType.Long;
		}
		else if (typeCls == Integer.class || typeCls == int.class) {
			type = ClassType.Int;
		}
		else if (typeCls == Double.class || typeCls == double.class) {
			type = ClassType.Double;
		}
		else if (typeCls == String.class) {
			type = ClassType.String;
		}
		else if (typeCls == Boolean.class || typeCls == boolean.class) {
			type = ClassType.Boolean;
		}
		else if (typeCls == java.sql.Date.class) {
			type = ClassType.SqlDate;
		}
		else if (typeCls == java.sql.Time.class) {
			type = ClassType.Time;
		}
		else if (typeCls == java.sql.Timestamp.class) {
			type = ClassType.Timestamp;
		}
		else if (typeCls == java.util.Date.class) {
			type = ClassType.Date;
		}
		else if (List.class.isAssignableFrom(typeCls)) {
			type = ClassType.List;
		}
		else if (Map.class.isAssignableFrom(typeCls)) {
			type = ClassType.Map;
		}
		else if (typeCls == Byte.class || typeCls == byte.class) {
			type = ClassType.Byte;
		}
		else if (typeCls == Character.class || typeCls == char.class) {
			type = ClassType.Char;
		}
		else if (typeCls == Short.class || typeCls == short.class) {
			type = ClassType.Short;
		}
		else if (typeCls == BigDecimal.class) {
			type = ClassType.BigDecimal;
		}
		else if (typeCls == BigInteger.class) {
			type = ClassType.BigInteger;
		}
		else if (typeCls == Class.class) {
			type = ClassType.Class;
		}
		else if (typeCls == Object.class) {
			type = ClassType.Object;
		}
		else {
			type = ClassType.ClassObject;
		}
		return type;
	}

	public static Object convertValue(ClassType type, Object value) throws CommonException {
		return convertValue(type, null, value);
	}

	public static Object convertValue(ClassType type, Class< ? > typeClass, Object value) throws CommonException {
		try {
			Object obj = null;
			if (value != null) {
				ClassType valueType = getClassType(value.getClass());
				if (!(type != ClassType.String && valueType == ClassType.String && StringUtils.isBlank(value.toString()))) {
					if (type != valueType) {
						if (type == ClassType.Long) {
							if (valueType == ClassType.BigDecimal) {
								obj = ((BigDecimal) value).longValue();
							}
							else if (valueType == ClassType.String) {
								obj = new Long(value.toString());
							}
							else if (valueType == ClassType.Int) {
								obj = ((Integer) value).longValue();
							}
							else if (valueType == ClassType.BigInteger) {
								obj = ((BigInteger) value).longValue();
							}
							else if (valueType == ClassType.Short) {
								obj = ((Short) value).longValue();
							}
						}
						else if (type == ClassType.BigDecimal) {
							if (valueType == ClassType.Int) {
								obj = new BigDecimal((Integer) value);
							}
							else if (valueType == ClassType.String) {
								obj = new BigDecimal(value.toString());
							}
							else if (valueType == ClassType.Long) {
								obj = new BigDecimal((Long) value);
							}
							else if (valueType == ClassType.Double) {
								obj = new BigDecimal((Double) value);
							}
							else if (valueType == ClassType.Boolean) {
								obj = ((Boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
							}
						}
						else if (type == ClassType.Int) {
							if (valueType == ClassType.BigDecimal) {
								obj = ((BigDecimal) value).intValue();
							}
							else if (valueType == ClassType.String) {
								obj = new Integer(value.toString());
							}
							else if (valueType == ClassType.Boolean) {
								obj = ((Boolean) value) ? 1 : 0;
							}
							else if (valueType == ClassType.BigInteger) {
								obj = ((BigInteger) value).intValue();
							}
						}
						else if (type == ClassType.Double) {
							if (valueType == ClassType.BigDecimal) {
								obj = ((BigDecimal) value).doubleValue();
							}
							else if (valueType == ClassType.String) {
								obj = new Double(value.toString());
							}
						}
						else if (type == ClassType.String) {
							if (valueType == ClassType.Array) {
								ClassType itemType = getClassType(value.getClass().getComponentType());
								if (itemType == ClassType.Byte) {
									obj = Base64.encodeBase64String((byte[]) value);
								}
							}
							else {
								obj = value.toString();
							}
						}
						else if (type == ClassType.Array) {
							if (valueType == ClassType.String) {
								ClassType itemType = getClassType(typeClass);
								if (itemType == ClassType.Byte) {
									obj = Base64.decodeBase64((String) value);
								}
							}
						}
						else if (type == ClassType.Boolean) {
							if (valueType == ClassType.BigDecimal) {
								obj = ((BigDecimal) value).intValue() == 1 ? true : false;
							}
							else if (valueType == ClassType.String) {
								obj = Boolean.parseBoolean((String) value);
							}
						}
						else if (type == ClassType.Date) {
							if (valueType == ClassType.Time) {
								obj = new java.util.Date(((java.sql.Time) value).getTime());
							}
							else if (valueType == ClassType.Timestamp) {
								obj = new java.util.Date(((java.sql.Timestamp) value).getTime());
							}
							else if (valueType == ClassType.SqlDate) {
								obj = new java.util.Date(((java.sql.Date) value).getTime());
							}
							else if (valueType == ClassType.Long) {
								obj = new java.util.Date((Long) value);
							}
						}
						else if (type == ClassType.Timestamp) {
							if (valueType == ClassType.Date) {
								obj = new java.sql.Timestamp(((java.util.Date) value).getTime());
							}
							else if (valueType == ClassType.Time) {
								obj = new java.sql.Timestamp(((java.sql.Time) value).getTime());
							}
							else if (valueType == ClassType.SqlDate) {
								obj = new java.sql.Timestamp(((java.sql.Date) value).getTime());
							}
						}
						else if (type == ClassType.Time) {
							if (valueType == ClassType.Date) {
								obj = new java.sql.Time(((java.util.Date) value).getTime());
							}
							else if (valueType == ClassType.SqlDate) {
								obj = new java.sql.Time(((java.sql.Date) value).getTime());
							}
							else if (valueType == ClassType.Timestamp) {
								obj = new java.sql.Time(((java.sql.Timestamp) value).getTime());
							}
						}
						else if (type == ClassType.SqlDate) {
							if (valueType == ClassType.Date) {
								obj = new java.sql.Date(((java.util.Date) value).getTime());
							}
							else if (valueType == ClassType.Timestamp) {
								obj = new java.sql.Date(((java.sql.Timestamp) value).getTime());
							}
							else if (valueType == ClassType.Time) {
								obj = new java.sql.Date(((java.sql.Time) value).getTime());
							}
						}
						else if (type == ClassType.Object) {
							obj = value;
						}
						else if (type == ClassType.Enum) {
							if (valueType == ClassType.String && typeClass != null) {
								obj = setEnumValue(typeClass, (String) value);
							}
						}
						else if (type == ClassType.ClassObject) {
							if (valueType == ClassType.String && typeClass == URI.class) {
								obj = new URI((String) value);
							}
						}
						if (obj == null) {
							throw new CommonException("没有相应转换机制(数据类型:" + getClassType(value.getClass()).toString() + ",字段类型:" + type.toString()
									+ (type == ClassType.ClassObject ? "[类:" + typeClass.getCanonicalName() + "]" : "") + ")");
						}
					}
					else {
						if (type == ClassType.ClassObject && typeClass != value.getClass() && !typeClass.isAssignableFrom(value.getClass())) {
							throw new CommonException(
									"类没有相应转换机制(数据类:" + value.getClass().getCanonicalName() + ",字段类:" + typeClass.getCanonicalName() + ")");
						}
						else if (type == ClassType.Array && getClassType(typeClass) != getClassType(value.getClass().getComponentType())) {
							throw new CommonException(
									"数组没有相应转换机制(数据类:" + value.getClass().getCanonicalName() + ",字段类:" + typeClass.getCanonicalName() + "[])");
						}
						else if (type == ClassType.Enum && !typeClass.getCanonicalName().equals(value.getClass().getCanonicalName())) {
							throw new CommonException(
									"枚举没有相应转换机制(数据类:" + value.getClass().getCanonicalName() + ",字段类:" + typeClass.getCanonicalName() + ")");
						}
						obj = value;
					}
				}
			}
			return obj;
		}
		catch (Throwable e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static Object setEnumValue(Class< ? > clazz, String value) throws CommonException {
		if (!clazz.isEnum()) {
			throw new CommonException("类'" + clazz.getCanonicalName() + "'不是枚举");
		}
		Method valuesMethod = getMethod(clazz, "values");
		Object values = invokeMethod(valuesMethod);
		int valuesSize = Array.getLength(values);
		boolean isFound = false;
		for (int i = 0; i < valuesSize; i++) {
			String enumValue = Array.get(values, i).toString();
			if (enumValue.equals(value)) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			throw new CommonException("值'" + value + "'不属于枚举'" + clazz.getCanonicalName() + "'中的内容");
		}
		Method valueOfMethod = getMethod(clazz, "valueOf", String.class);
		return invokeMethod(valueOfMethod, value);
	}

	public static Method getMethod(Class< ? > clazz, String methodName, Class< ? >... parameterTypes) throws CommonException {
		return getMethod(clazz, methodName, true, parameterTypes);
	}

	private static Method getMethod(Class< ? > clazz, String methodName, boolean isFirst, Class< ? >... parameterTypes) throws CommonException {
		Method method = null;
		try {
			method = clazz.getDeclaredMethod(methodName, parameterTypes);
		}
		catch (SecurityException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		catch (NoSuchMethodException e) {
			if (clazz.getSuperclass() != Object.class) {
				method = getMethod(clazz.getSuperclass(), methodName, false, parameterTypes);
			}
		}
		if (null == method && isFirst) {
			throw new CommonException("在类'" + clazz.getCanonicalName() + "'中没有找到方法'" + methodName + "'");
		}
		return method;
	}

	public static Method findSetMethod(Class< ? > clazz, String name) {
		String methodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		Method result = null;
		try {
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals(methodName) && method.getReturnType() == void.class && method.getParameterCount() == 1) {
					result = method;
					break;
				}
			}
		}
		catch (SecurityException e) {
		}
		if (null == result && clazz.getSuperclass() != Object.class) {
			result = findSetMethod(clazz.getSuperclass(), name);
		}
		return result;
	}

	public static Object invokeMethod(Method method, Object... args) throws CommonException {
		return invokeObjectMethod(method, null, args);
	}

	public static Object invokeObjectMethod(Method method, Object target, Object... args) throws CommonException {
		boolean accessible = method.isAccessible();
		try {
			method.setAccessible(true);
			return method.invoke(target, args);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				throw ExceptionWrapUtils.wrap(e.getCause());
			}
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			method.setAccessible(accessible);
		}
	}

	public static Object getFieldValue(Object obj, String fieldName) throws CommonException {
		Field field = getField(obj.getClass(), fieldName);
		return getFieldValue(obj, field);
	}

	public static Object getFieldValue(Object obj, Field field) throws CommonException {
		boolean isAccessible = field.isAccessible();
		try {
			field.setAccessible(true);
			return field.get(obj);
		}
		catch (IllegalArgumentException | IllegalAccessException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			field.setAccessible(isAccessible);
		}
	}

	public static List<Field> getFields(Class< ? > clazz) {
		List<Field> fields = new Vector<Field>();
		Field[] clsFields = clazz.getDeclaredFields();
		for (Field field : clsFields) {
			int mod = field.getModifiers();
			if ((Modifier.isPrivate(mod) || Modifier.isPublic(mod))
					&& (!Modifier.isFinal(mod) || (Modifier.isFinal(mod) && !Modifier.isStatic(mod)))) {
				fields.add(field);
			}
		}
		Class< ? > superclass = clazz.getSuperclass();
		if (superclass != null && superclass != Object.class) {
			fields.addAll(getFields(clazz.getSuperclass()));
		}
		return fields;
	}

	public static <A extends Annotation> A getAnnotation(Class< ? > clazz, Class<A> annotationClass) throws CommonException {
		A annotation = clazz.getAnnotation(annotationClass);
		if (null == annotation) {
			if (Object.class != clazz.getSuperclass()) {
				annotation = getAnnotation(clazz.getSuperclass(), annotationClass);
			}
		}
		return annotation;
	}
}