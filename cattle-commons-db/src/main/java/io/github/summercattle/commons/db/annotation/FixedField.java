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
package io.github.summercattle.commons.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.summercattle.commons.db.constants.DataType;

/**
 * 固定字段的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface FixedField {

	/**
	 * 字段名
	 * @return 字段名
	 */
	String name();

	/**
	 * 备注
	 * @return 备注
	 */
	String comment() default "";

	/**
	 * 数据类型
	 * @return 数据类型
	 */
	DataType type();

	/**
	 * 长度
	 * @return 长度
	 */
	int length() default 0;

	/**
	 * 精度	
	 * @return 精度
	 */
	int scale() default 0;

	/**
	 * 允许为空
	 * @return 允许为空
	 */
	boolean allowNull() default true;

	/**
	 * 缺省值
	 * @return 缺省值
	 */
	String defaultValue() default "";
}