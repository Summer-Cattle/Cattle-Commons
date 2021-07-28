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
package io.github.summercattle.commons.webmvc.error;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;
import io.github.summercattle.commons.utils.exception.CommonResponseException;
import io.github.summercattle.commons.utils.exception.CommonResponseRuntimeException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassUtils;
import io.github.summercattle.commons.webmvc.configure.ErrorFrontendPageProperties;

@Controller
@RequestMapping(path = "/error")
public class ErrorExceptionController extends AbstractErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorExceptionController.class);

	private static final String DEFAULT_SUFFIX = ".html";

	private ErrorAttributes errorAttributes;

	@Autowired
	private ErrorFrontendPageProperties errorFrontendPageProperties;

	public ErrorExceptionController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
		this.errorAttributes = errorAttributes;
	}

	private Throwable getThrowable(HttpServletRequest request) {
		WebRequest webRequest = new ServletWebRequest(request);
		return errorAttributes.getError(webRequest);
	}

	private String getExceptionResponse(Throwable e) {
		if (e instanceof CommonResponseRuntimeException) {
			return ((CommonResponseRuntimeException) e).getResponse();
		}
		else if (e instanceof CommonResponseRuntimeException) {
			return ((CommonResponseException) e).getResponse();
		}
		else {
			if (null != e.getCause()) {
				return getExceptionResponse(e.getCause());
			}
		}
		return null;
	}

	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
	public String errorHtml(Model model, HttpServletRequest request, HttpServletResponse response) {
		int status = super.getStatus(request).value();
		Throwable throwable = getThrowable(request);
		String message;
		if (null != throwable) {
			message = throwable.getMessage();
			String jsonStr = getExceptionResponse(throwable);
			if (StringUtils.isNotBlank(jsonStr)) {
				JSONObject jsonObj = JSON.parseObject(jsonStr);
				status = jsonObj.getIntValue("status");
				message = jsonObj.getString("message");
				if (StringUtils.isBlank(message)) {
					message = jsonObj.getString("error");
				}
			}
		}
		else {
			Map<String, Object> errorAttributes = super.getErrorAttributes(request, ErrorAttributeOptions.of(Include.MESSAGE));
			message = (String) errorAttributes.get("message");
			if (StringUtils.isBlank(message)) {
				message = (String) errorAttributes.get("error");
			}
		}
		model.addAttribute("errorStatus", status);
		model.addAttribute("errorMessage", message);
		response.setStatus(status);
		String page;
		if (HttpServletResponse.SC_NOT_FOUND == status) {
			page = errorFrontendPageProperties.getNotFoundPage();
		}
		else if (HttpServletResponse.SC_FORBIDDEN == status) {
			page = errorFrontendPageProperties.getForbiddenPage();
		}
		else {
			page = errorFrontendPageProperties.getErrorPage();
		}
		if (StringUtils.isBlank(page)) {
			logger.error("状态码:" + status + ",异常信息:" + message);
			throw new CommonRuntimeException("没有设置异常对应的页面");
		}
		try {
			if (!ClassUtils.getClassResourceLoader().existResource(page)) {
				logger.error("状态码:" + status + ",异常信息:" + message);
				throw new CommonRuntimeException("异常对应的页面'" + page + "'没有找到");
			}
			if (page.endsWith(DEFAULT_SUFFIX)) {
				page = page.substring(0, page.length() - DEFAULT_SUFFIX.length());
			}
			return page;
		}
		catch (CommonException e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@RequestMapping
	@ResponseBody
	public Map<String, Object> handleError(HttpServletRequest request, HttpServletResponse response) {
		int status = super.getStatus(request).value();
		Throwable throwable = getThrowable(request);
		String message;
		if (null != throwable) {
			message = throwable.getMessage();
			String jsonStr = getExceptionResponse(throwable);
			if (StringUtils.isNotBlank(jsonStr)) {
				JSONObject jsonObj = JSON.parseObject(jsonStr);
				status = jsonObj.getIntValue("status");
				message = jsonObj.getString("message");
				if (StringUtils.isBlank(message)) {
					message = jsonObj.getString("error");
				}
			}
		}
		else {
			Map<String, Object> errorAttributes = super.getErrorAttributes(request, ErrorAttributeOptions.of(Include.MESSAGE));
			message = (String) errorAttributes.get("message");
			if (StringUtils.isBlank(message)) {
				message = (String) errorAttributes.get("error");
			}
		}
		Map<String, Object> errorResponse = new HashMap<String, Object>();
		errorResponse.put("success", false);
		errorResponse.put("message", message);
		errorResponse.put("errorCode", status);
		response.setStatus(status);
		return errorResponse;
	}
}