package io.github.summercattle.commons.quartz.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定时器作业(Cron表达式)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface CronQuartzJob {

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
	 * Cron表达式
	 * @return Cron表达式
	 */
	String cronExpression() default "";

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
}