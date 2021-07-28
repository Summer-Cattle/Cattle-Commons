package io.github.summercattle.commons.quartz.configure;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.quartz.QuartzSchedulerFactoryBeanCustomizer;
import io.github.summercattle.commons.quartz.QuartzService;
import io.github.summercattle.commons.quartz.annotation.ConfigureQuartzJob;
import io.github.summercattle.commons.quartz.annotation.CronQuartzJob;
import io.github.summercattle.commons.quartz.annotation.QuartzJob;
import io.github.summercattle.commons.quartz.job.Job;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassUtils;
import io.github.summercattle.commons.utils.spring.SpringContext;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(QuartzProperties.class)
@ConditionalOnClass({ Scheduler.class, SchedulerFactoryBean.class, PlatformTransactionManager.class })
@ConditionalOnProperty(prefix = "quartz", name = "enabled", matchIfMissing = true)
@Import(QuartzSchedulerFactoryBeanCustomizer.class)
@ComponentScan(basePackageClasses = QuartzService.class)
public class QuartzAutoConfiguration {

	public QuartzAutoConfiguration() {
		QuartzService quartzService = SpringContext.getBean(QuartzService.class);
		Class<Job>[] jobClasses = null;
		jobClasses = ClassUtils.getSubTypesOf(Job.class);
		if (null != jobClasses && jobClasses.length > 0) {
			for (Class<Job> jobClass : jobClasses) {
				try {
					QuartzJob quartzJob = jobClass.getAnnotation(QuartzJob.class);
					if (null != quartzJob) {
						quartzService.add(quartzJob, jobClass);
						continue;
					}
					CronQuartzJob cronQuartzJob = jobClass.getAnnotation(CronQuartzJob.class);
					if (null != cronQuartzJob) {
						quartzService.add(cronQuartzJob, jobClass);
						continue;
					}
					ConfigureQuartzJob configureQuartzJob = jobClass.getAnnotation(ConfigureQuartzJob.class);
					if (null != configureQuartzJob) {
						quartzService.add(configureQuartzJob, jobClass);
						continue;
					}
				}
				catch (CommonException e) {
					throw ExceptionWrapUtils.wrapRuntime(e);
				}
			}
		}
	}
}