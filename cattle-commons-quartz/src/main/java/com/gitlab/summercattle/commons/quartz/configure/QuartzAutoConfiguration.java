package com.gitlab.summercattle.commons.quartz.configure;

import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.quartz.QuartzSchedulerFactoryBeanCustomizer;
import com.gitlab.summercattle.commons.quartz.QuartzService;
import com.gitlab.summercattle.commons.quartz.annotation.ConfigureQuartzJob;
import com.gitlab.summercattle.commons.quartz.annotation.CronQuartzJob;
import com.gitlab.summercattle.commons.quartz.annotation.QuartzJob;
import com.gitlab.summercattle.commons.quartz.job.Job;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.reflect.ClassUtils;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;

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