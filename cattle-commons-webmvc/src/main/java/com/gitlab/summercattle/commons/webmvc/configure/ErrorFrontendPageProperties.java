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
package com.gitlab.summercattle.commons.webmvc.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = ErrorFrontendPageProperties.PREFIX)
public class ErrorFrontendPageProperties {

	public static final String PREFIX = "cattle.error.page";

	private String notFoundPage;

	private String forbiddenPage;

	private String errorPage;

	public String getNotFoundPage() {
		return notFoundPage;
	}

	public void setNotFoundPage(String notFoundPage) {
		this.notFoundPage = notFoundPage;
	}

	public String getForbiddenPage() {
		return forbiddenPage;
	}

	public void setForbiddenPage(String forbiddenPage) {
		this.forbiddenPage = forbiddenPage;
	}

	public String getErrorPage() {
		return errorPage;
	}

	public void setErrorPage(String errorPage) {
		this.errorPage = errorPage;
	}
}