package io.github.summercattle.commons.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定时器作业(根据配置)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface ConfigureQuartzJob {

	/**
	 * 定时器名称
	 * @return 定时器名称
	 */
	String name() default "";

	/**
	 * 定时器描述
	 * @return 定时器描述
	 */
	String description() default "";
}