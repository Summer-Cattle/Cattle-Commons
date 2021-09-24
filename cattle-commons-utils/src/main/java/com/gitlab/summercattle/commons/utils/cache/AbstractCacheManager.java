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
package com.gitlab.summercattle.commons.utils.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;

public abstract class AbstractCacheManager implements CacheManager {

	private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

	protected abstract Cache createCache(String name, Map<String, String> props) throws CommonException;

	@Override
	public Cache getCache(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("缓存分类键值为空");
		}
		if (!caches.containsKey(name)) {
			CacheProperties cacheProperties = SpringContext.getBean(CacheProperties.class);
			Map<String, Map<String, String>> cachesProps = cacheProperties.getCache();
			if (null == cachesProps) {
				throw new CommonException("缓存属性为空");
			}
			Map<String, String> props = cachesProps.get(name);
			putCache(name, props);
		}
		return caches.get(name);
	}

	@Override
	public Cache getCache(String name, Map<String, String> props) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("缓存分类键值为空");
		}
		if (!caches.containsKey(name)) {
			putCache(name, props);
		}
		return caches.get(name);
	}

	private synchronized void putCache(String name, Map<String, String> props) throws CommonException {
		if (null == props) {
			throw new CommonException("缓存属性为空");
		}
		Cache cache = createCache(name, props);
		if (null == cache) {
			throw new CommonException("缓存分类键值'" + name + "'创建失败");
		}
		caches.put(name, cache);
	}

	@Override
	public void removeCache(String name) throws CommonException {
		if (StringUtils.isBlank(name)) {
			throw new CommonException("缓存分类键值为空");
		}
		if (caches.containsKey(name)) {
			Cache cache = caches.get(name);
			cache.clear();
		}
	}
}