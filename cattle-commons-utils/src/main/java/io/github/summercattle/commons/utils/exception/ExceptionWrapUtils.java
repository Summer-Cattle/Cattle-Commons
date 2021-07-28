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
package io.github.summercattle.commons.utils.exception;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;

public class ExceptionWrapUtils {

	public static CommonException wrap(Throwable e) {
		if (e instanceof CommonException) {
			return (CommonException) e;
		}
		else if (e instanceof CommonResponseRuntimeException) {
			CommonResponseRuntimeException exception = (CommonResponseRuntimeException) e;
			return new CommonResponseException(exception.getCode(), exception.getMessage(), exception.getResponse());
		}
		else if (e instanceof CommonRuntimeException) {
			CommonRuntimeException exception = (CommonRuntimeException) e;
			return new CommonException(exception.getCode(), exception.getMessage());
		}
		else {
			return new CommonException(e.getMessage(), e);
		}
	}

	public static CommonRuntimeException wrapRuntime(Throwable e) {
		if (e instanceof CommonRuntimeException) {
			return (CommonRuntimeException) e;
		}
		else if (e instanceof CommonResponseException) {
			CommonResponseException exception = (CommonResponseException) e;
			return new CommonResponseRuntimeException(exception.getCode(), exception.getMessage(), exception.getResponse());
		}
		else if (e instanceof CommonException) {
			CommonException exception = (CommonException) e;
			return new CommonRuntimeException(exception.getCode(), exception.getMessage());
		}
		else {
			return new CommonRuntimeException(e.getMessage(), e);
		}
	}
}