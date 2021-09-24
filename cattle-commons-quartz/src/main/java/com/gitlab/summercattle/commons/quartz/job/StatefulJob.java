package com.gitlab.summercattle.commons.quartz.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public interface StatefulJob extends Job {
}