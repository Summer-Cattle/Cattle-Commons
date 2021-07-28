package io.github.summercattle.commons.quartz;

import java.util.Date;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.quartz.annotation.ConfigureQuartzJob;
import io.github.summercattle.commons.quartz.annotation.CronQuartzJob;
import io.github.summercattle.commons.quartz.annotation.QuartzJob;
import io.github.summercattle.commons.quartz.configure.QuartzProperties;
import io.github.summercattle.commons.quartz.job.Job;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;
import io.github.summercattle.commons.utils.spring.SpringContext;

@Component
@ConditionalOnClass({ Scheduler.class, SchedulerFactoryBean.class })
public class QuartzService {

	private static final Logger logger = LoggerFactory.getLogger(QuartzService.class);

	private static final String QUARTZ_GROUP = "scm_eapp";

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private QuartzProperties quartzProperties;

	public void add(QuartzJob quartzJob, Class<Job> jobClass) throws CommonException {
		boolean oper = true;
		if (null != quartzJob.profile() && quartzJob.profile().length > 0) {
			oper = SpringContext.getEnvironment().acceptsProfiles(Profiles.of(quartzJob.profile()));
		}
		if (oper && StringUtils.isNotBlank(quartzJob.enabledProperty())) {
			boolean match;
			String strEnabledProperty = SpringContext.getProperty(quartzJob.enabledProperty());
			if (StringUtils.isNotBlank(strEnabledProperty)) {
				match = BooleanUtils.toBoolean(strEnabledProperty);
			}
			else {
				match = quartzJob.enabledMatchIfMissing();
			}
			oper = match == quartzJob.matchConditionalValue();
		}
		if (oper) {
			String name = getJobName(quartzJob.name(), jobClass);
			if (StringUtils.isNotBlank(name)) {
				if (null == quartzJob.type()) {
					throw new CommonException("定时作业'" + name + "'的触发器类型为空");
				}
				if (quartzJob.interval() <= 0) {
					throw new CommonException("定时作业'" + name + "'的间隔必须大于零");
				}
				SimpleTrigger simpleTrigger = getSimpleTrigger(name, quartzJob.description(), quartzJob.type(), quartzJob.interval(),
						quartzJob.immediately());
				JobDetail jobDetail = getJobDetail(name, quartzJob.description(), jobClass);
				scheduleJob(name, jobDetail, simpleTrigger);
			}
		}
	}

	public void add(CronQuartzJob cronQuartzJob, Class<Job> jobClass) throws CommonException {
		boolean oper = true;
		if (null != cronQuartzJob.profile() && cronQuartzJob.profile().length > 0) {
			oper = SpringContext.getEnvironment().acceptsProfiles(Profiles.of(cronQuartzJob.profile()));
		}
		if (oper && StringUtils.isNotBlank(cronQuartzJob.enabledProperty())) {
			String strEnabledProperty = SpringContext.getProperty(cronQuartzJob.enabledProperty());
			if (StringUtils.isNotBlank(strEnabledProperty)) {
				oper = BooleanUtils.toBoolean(strEnabledProperty);
			}
			else {
				oper = cronQuartzJob.enabledMatchIfMissing();
			}
		}
		if (oper) {
			String name = getJobName(cronQuartzJob.name(), jobClass);
			if (StringUtils.isNotBlank(name)) {
				if (StringUtils.isBlank(cronQuartzJob.cronExpression())) {
					throw new CommonException("定时作业'" + name + "'没有设置Cron表达式");
				}
				CronTrigger cronTrigger = getCronTrigger(name, cronQuartzJob.description(), cronQuartzJob.cronExpression());
				JobDetail jobDetail = getJobDetail(name, cronQuartzJob.description(), jobClass);
				scheduleJob(name, jobDetail, cronTrigger);
			}
		}
	}

	public void add(ConfigureQuartzJob configureQuartzJob, Class<Job> jobClass) throws CommonException {
		String name = getJobName(configureQuartzJob.name(), jobClass);
		if (StringUtils.isNotBlank(name)) {
			QuartzJobInfo jobInfo = getQuartzJobInfo(name);
			if (null != jobInfo) {
				boolean oper = true;
				if (null != jobInfo.getProfile() && jobInfo.getProfile().length > 0) {
					oper = SpringContext.getEnvironment().acceptsProfiles(Profiles.of(jobInfo.getProfile()));
				}
				if (oper && StringUtils.isNotBlank(jobInfo.getEnabledProperty())) {
					String strEnabledProperty = SpringContext.getProperty(jobInfo.getEnabledProperty());
					if (StringUtils.isNotBlank(strEnabledProperty)) {
						oper = BooleanUtils.toBoolean(strEnabledProperty);
					}
					else {
						oper = jobInfo.isEnabledMatchIfMissing();
					}
				}
				if (oper) {
					if (jobInfo.isCronExpression()) {
						String cronExpression = (String) ReflectUtils.convertValue(ClassType.String, jobInfo.getValue());
						if (StringUtils.isBlank(cronExpression)) {
							throw new CommonException("定时作业'" + name + "'没有设置Cron表达式");
						}
						CronTrigger cronTrigger = getCronTrigger(name, configureQuartzJob.description(), cronExpression);
						JobDetail jobDetail = getJobDetail(name, configureQuartzJob.description(), jobClass);
						scheduleJob(name, jobDetail, cronTrigger);
					}
					else {
						if (null == jobInfo.getType()) {
							throw new CommonException("定时作业'" + name + "'的触发器类型为空");
						}
						Integer interval = (Integer) ReflectUtils.convertValue(ClassType.Int, jobInfo.getValue());
						if (null == interval || interval.intValue() <= 0) {
							throw new CommonException("定时作业'" + name + "'的间隔必须大于零");
						}
						SimpleTrigger simpleTrigger = getSimpleTrigger(name, configureQuartzJob.description(), jobInfo.getType(), interval.intValue(),
								jobInfo.isImmediately());
						JobDetail jobDetail = getJobDetail(name, configureQuartzJob.description(), jobClass);
						scheduleJob(name, jobDetail, simpleTrigger);
					}
				}
			}
			else {
				logger.warn("定时作业'" + name + "'没有相应的配置信息");
			}
		}
	}

	private JobDetail getJobDetail(String name, String description, Class<Job> jobClass) {
		JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(new JobKey(name, QUARTZ_GROUP));
		if (StringUtils.isNotBlank(description)) {
			jobBuilder = jobBuilder.withDescription(description);
		}
		return jobBuilder.build();
	}

	private CronTrigger getCronTrigger(String name, String description, String cronExpression) {
		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
		TriggerBuilder<CronTrigger> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(name, QUARTZ_GROUP))
				.withSchedule(cronScheduleBuilder);
		if (StringUtils.isNotBlank(description)) {
			triggerBuilder = triggerBuilder.withDescription(description);
		}
		return triggerBuilder.build();
	}

	private SimpleTrigger getSimpleTrigger(String name, String description, TriggerType type, int interval, boolean immediately)
			throws CommonException {
		SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().repeatForever();
		if (type == TriggerType.Day) {
			simpleScheduleBuilder = simpleScheduleBuilder.withIntervalInMilliseconds(DateBuilder.MILLISECONDS_IN_DAY * interval);
		}
		else if (type == TriggerType.Hour) {
			simpleScheduleBuilder = simpleScheduleBuilder.withIntervalInHours(interval);
		}
		else if (type == TriggerType.Minute) {
			simpleScheduleBuilder = simpleScheduleBuilder.withIntervalInMinutes(interval);
		}
		else if (type == TriggerType.Second) {
			simpleScheduleBuilder = simpleScheduleBuilder.withIntervalInSeconds(interval);
		}
		else {
			throw new CommonException("未知的触发器类型'" + type.toString() + "'");
		}
		TriggerBuilder<SimpleTrigger> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(name, QUARTZ_GROUP))
				.withSchedule(simpleScheduleBuilder);
		if (StringUtils.isNotBlank(description)) {
			triggerBuilder = triggerBuilder.withDescription(description);
		}
		if (immediately) {
			triggerBuilder = triggerBuilder.startNow();
		}
		else {
			long nextInterval = 0;
			if (type == TriggerType.Day) {
				nextInterval = DateBuilder.MILLISECONDS_IN_DAY * interval;
			}
			else if (type == TriggerType.Hour) {
				nextInterval = DateBuilder.MILLISECONDS_IN_HOUR * interval;
			}
			else if (type == TriggerType.Minute) {
				nextInterval = DateBuilder.MILLISECONDS_IN_MINUTE * interval;
			}
			else if (type == TriggerType.Second) {
				nextInterval = interval * 1000L;
			}
			else {
				throw new CommonException("未知的触发器类型'" + type.toString() + "'");
			}
			Date startTime = new Date((new Date()).getTime() + nextInterval);
			triggerBuilder = triggerBuilder.startAt(startTime);
		}
		return triggerBuilder.build();
	}

	private void scheduleJob(String name, JobDetail jobDetail, Trigger trigger) throws CommonException {
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			logger.debug("定时作业'" + name + "'加入定时器");
			scheduler.scheduleJob(jobDetail, trigger);
		}
		catch (SchedulerException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private String getJobName(String name, Class<Job> jobClass) throws CommonException {
		String result = name;
		if (StringUtils.isBlank(result)) {
			result = jobClass.getSimpleName();
		}
		try {
			if (scheduler.checkExists(new JobKey(name, QUARTZ_GROUP))) {
				logger.warn("定时作业'" + name + "'已经存在");
				return null;
			}
			return result;
		}
		catch (SchedulerException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private QuartzJobInfo getQuartzJobInfo(String name) {
		QuartzJobInfo result = null;
		if (null != quartzProperties.getJobs()) {
			for (QuartzJobInfo jobInfo : quartzProperties.getJobs()) {
				if (name.equals(jobInfo.getName())) {
					result = jobInfo;
					break;
				}
			}
		}
		return result;
	}
}