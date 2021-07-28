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
package io.github.summercattle.commons.exception;

/**
 * 运行时异常
 */
public class CommonRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private Integer code;

	public CommonRuntimeException(String message) {
		this(message, null);
	}

	public CommonRuntimeException(Integer code, String message) {
		this(code, message, null);
	}

	public CommonRuntimeException(String message, Throwable e) {
		this(ExceptionConstants.GENERAL, message, e);
	}

	public CommonRuntimeException(Integer code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}

	public int getCode() {
		return null != code ? code.intValue() : ExceptionConstants.GENERAL;
	}
}