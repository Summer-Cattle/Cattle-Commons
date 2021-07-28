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
package io.github.summercattle.commons.utils;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.cache.CacheManager;
import io.github.summercattle.commons.utils.guice.GuiceUtils;

public class Utils {

	public static CacheManager getCacheManager() throws CommonException {
		CacheManager cacheManager = GuiceUtils.getInstance(CacheManager.class);
		if (null == cacheManager) {
			throw new CommonException("缓存管理器实现类为空");
		}
		return cacheManager;
	}
}