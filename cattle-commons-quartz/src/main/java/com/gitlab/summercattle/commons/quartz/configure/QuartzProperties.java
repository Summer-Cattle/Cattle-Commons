package com.gitlab.summercattle.commons.quartz.configure;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.gitlab.summercattle.commons.quartz.QuartzJobInfo;

@ConfigurationProperties(prefix = QuartzProperties.PREFIX)
public class QuartzProperties {

	public static final String PREFIX = "quartz";

	private List<QuartzJobInfo> jobs;

	public List<QuartzJobInfo> getJobs() {
		return jobs;
	}

	public void setJobs(List<QuartzJobInfo> jobs) {
		this.jobs = jobs;
	}
}