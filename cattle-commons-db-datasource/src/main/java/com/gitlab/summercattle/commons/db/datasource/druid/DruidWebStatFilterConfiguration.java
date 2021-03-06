/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitlab.summercattle.commons.db.datasource.druid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.alibaba.druid.support.http.WebStatFilter;
import com.gitlab.summercattle.commons.db.datasource.constants.DataSourceConstants;

@ConditionalOnWebApplication
@ConditionalOnProperty(name = DataSourceConstants.PROPERTY_PREFIX_DRUID + ".web-stat-filter.enabled", havingValue = "true")
public class DruidWebStatFilterConfiguration {

	@Bean
	public FilterRegistrationBean<WebStatFilter> webStatFilterRegistrationBean(DruidStatProperties properties) {
		DruidStatProperties.WebStatFilter config = properties.getWebStatFilter();
		FilterRegistrationBean<WebStatFilter> registrationBean = new FilterRegistrationBean<WebStatFilter>();
		WebStatFilter filter = new WebStatFilter();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns(config.getUrlPattern() != null ? config.getUrlPattern() : "/*");
		registrationBean.addInitParameter("exclusions",
				config.getExclusions() != null ? config.getExclusions() : "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		if (config.getSessionStatEnable() != null) {
			registrationBean.addInitParameter("sessionStatEnable", config.getSessionStatEnable());
		}
		if (config.getSessionStatMaxCount() != null) {
			registrationBean.addInitParameter("sessionStatMaxCount", config.getSessionStatMaxCount());
		}
		if (config.getPrincipalSessionName() != null) {
			registrationBean.addInitParameter("principalSessionName", config.getPrincipalSessionName());
		}
		if (config.getPrincipalCookieName() != null) {
			registrationBean.addInitParameter("principalCookieName", config.getPrincipalCookieName());
		}
		if (config.getProfileEnable() != null) {
			registrationBean.addInitParameter("profileEnable", config.getProfileEnable());
		}
		return registrationBean;
	}
}