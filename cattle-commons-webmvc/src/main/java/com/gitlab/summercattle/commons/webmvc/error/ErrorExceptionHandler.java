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
package com.gitlab.summercattle.commons.webmvc.error;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.exception.CommonRuntimeException;
import com.gitlab.summercattle.commons.resp.Response;

@RestControllerAdvice
public class ErrorExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ErrorExceptionHandler.class);

	@Value("${cattle.error.throwStackTrace:false}")
	private boolean throwStackTrace;

	@ExceptionHandler(Throwable.class)
	public Response<Void> handleException(HttpServletRequest request, Throwable e) {
		logger.error(e.getMessage(), e);
		if (e instanceof CommonException) {
			int errorCode = ((CommonException) e).getCode();
			return new Response<Void>(errorCode, e.getMessage(), throwStackTrace ? ExceptionUtils.getStackTrace(e) : null);
		}
		else if (e instanceof CommonRuntimeException) {
			int errorCode = ((CommonRuntimeException) e).getCode();
			return new Response<Void>(errorCode, e.getMessage(), throwStackTrace ? ExceptionUtils.getStackTrace(e) : null);
		}
		else {
			return new Response<Void>(e.getMessage(), throwStackTrace ? ExceptionUtils.getStackTrace(e) : null);
		}
	}
}