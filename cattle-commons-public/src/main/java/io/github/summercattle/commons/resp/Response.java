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
package io.github.summercattle.commons.resp;

import java.io.Serializable;

/**
 * 信息反馈
 */
public class Response<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 消息
	 */
	private String message;

	/**
	 * 返回值
	 */
	private T data;

	/**
	 * 错误代码
	 */
	private Integer errorCode;

	/**
	 * 错误异常
	 */
	private String stackTrace;

	public Response() {
		this((T) null);
	}

	public Response(T data) {
		this.success = true;
		this.data = data;
		this.message = "成功调用";
	}

	public Response(String message, String stackTrace) {
		this(null, message, stackTrace);
	}

	public Response(Integer errorCode, String message, String stackTrace) {
		this.success = false;
		this.errorCode = errorCode;
		if (null != message && !message.isEmpty()) {
			this.message = message;
		}
		else {
			this.message = "空值异常";
		}
		this.stackTrace = stackTrace;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Integer getErrorCode() {
		if (null == errorCode || errorCode.intValue() == 0) {
			return null;
		}
		return errorCode;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public T getData() {
		return data;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setData(T data) {
		this.data = data;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}