package com.gitlab.summercattle.commons.quartz;

public class QuartzJobInfo {

	private boolean cronExpression;

	private TriggerType type;

	private String name;

	private Object value;

	private boolean immediately = false;

	private String[] profile;

	private String enabledProperty;

	private boolean enabledMatchIfMissing = true;

	public boolean isCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(boolean cronExpression) {
		this.cronExpression = cronExpression;
	}

	public TriggerType getType() {
		return type;
	}

	public void setType(TriggerType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isImmediately() {
		return immediately;
	}

	public void setImmediately(boolean immediately) {
		this.immediately = immediately;
	}

	public String[] getProfile() {
		return profile;
	}

	public void setProfile(String[] profile) {
		this.profile = profile;
	}

	public String getEnabledProperty() {
		return enabledProperty;
	}

	public void setEnabledProperty(String enabledProperty) {
		this.enabledProperty = enabledProperty;
	}

	public boolean isEnabledMatchIfMissing() {
		return enabledMatchIfMissing;
	}

	public void setEnabledMatchIfMissing(boolean enabledMatchIfMissing) {
		this.enabledMatchIfMissing = enabledMatchIfMissing;
	}
}