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

/**
 * 表的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Table {

	/**
	 * 表名
	 * @return 表名
	 */
	String name();

	/**
	 * 别名
	 * @return 别名
	 */
	String alias() default "";

	/**
	 * 是否缓存
	 * @return 是否缓存
	 */
	boolean useCache() default false;

	/**
	 * 备注
	 * @return 备注
	 */
	String comment() default "";

	/**
	 * 主键约束名称
	 * @return 主键约束名称
	 */
	String primaryKeyName() default "";

	/**
	 * 主键是否为数值
	 * @return 主键是否为数值
	 */
	boolean primaryKeyUseNumber() default false;

	/**
	 * 索引
	 * @return 索引
	 */
	Index[] indexes() default {};

	/**
	 * 判断有效的属性名
	 * @return 判断有效的属性名
	 */
	String[] propertyNames() default {};

	/**
	 * 判断值
	 * @return 判断值
	 */
	String havingValue() default "";

	/**
	 * 有效的属性名缺失时匹配值
	 * @return 有效的属性名缺失时匹配值
	 */
	boolean matchIfMissing() default false;
}