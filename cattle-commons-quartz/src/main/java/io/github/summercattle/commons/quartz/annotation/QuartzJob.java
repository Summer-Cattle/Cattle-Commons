package io.github.summercattle.commons.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.summercattle.commons.quartz.TriggerType;

/**
 * 定时器作业
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface QuartzJob {

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

	/**
	 * 触发器类型
	 * @return 触发器类型
	 */
	TriggerType type();

	/**
	 * 间隔值
	 * @return 间隔值
	 */
	int interval() default 0;

	/**
	 * 是否立即执行
	 * @return 是否立即执行
	 */
	boolean immediately() default false;

	/**
	 * Profile值
	 * @return Profile值
	 */
	String[] profile() default {};

	/**
	 * 是否有效的属性名
	 * @return 是否有效的属性名
	 */
	String enabledProperty() default "";

	/**
	 * 是否有效的属性匹配缺省值
	 * @return 是否有效的属性匹配缺省值
	 */
	boolean enabledMatchIfMissing() default true;

	/**
	 * 匹配的条件值
	 * @return 匹配的条件值
	 */
	boolean matchConditionalValue() default true;
}