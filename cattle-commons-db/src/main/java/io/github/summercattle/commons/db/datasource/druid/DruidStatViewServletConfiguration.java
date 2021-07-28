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
package io.github.summercattle.commons.db.datasource.druid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.alibaba.druid.support.http.StatViewServlet;

import io.github.summercattle.commons.db.constants.DataConstants;

@ConditionalOnWebApplication
@ConditionalOnProperty(name = DataConstants.PROPERTY_PREFIX_DRUID + ".stat-view-servlet.enabled", havingValue = "true")
public class DruidStatViewServletConfiguration {

	private static final String DEFAULT_ALLOW_IP = "127.0.0.1";

	@Bean
	public ServletRegistrationBean<StatViewServlet> statViewServletRegistrationBean(DruidStatProperties properties) {
		DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
		ServletRegistrationBean<StatViewServlet> registrationBean = new ServletRegistrationBean<StatViewServlet>();
		registrationBean.setServlet(new StatViewServlet());
		registrationBean.addUrlMappings(config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*");
		if (config.getAllow() != null) {
			registrationBean.addInitParameter("allow", config.getAllow());
		}
		else {
			registrationBean.addInitParameter("allow", DEFAULT_ALLOW_IP);
		}
		if (config.getDeny() != null) {
			registrationBean.addInitParameter("deny", config.getDeny());
		}
		if (config.getLoginUsername() != null) {
			registrationBean.addInitParameter("loginUsername", config.getLoginUsername());
		}
		if (config.getLoginPassword() != null) {
			registrationBean.addInitParameter("loginPassword", config.getLoginPassword());
		}
		if (config.getResetEnable() != null) {
			registrationBean.addInitParameter("resetEnable", config.getResetEnable());
		}
		return registrationBean;
	}
}