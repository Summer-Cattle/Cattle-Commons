package com.gitlab.summercattle.commons.quartz;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class QuartzSchedulerFactoryBeanCustomizer implements SchedulerFactoryBeanCustomizer {

	@Override
	public void customize(SchedulerFactoryBean schedulerFactoryBean) {
		schedulerFactoryBean.setAutoStartup(false);
	}
}