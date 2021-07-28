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
package io.github.summercattle.commons.utils.reflect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.annotation.ClassLoadLevel;
import io.github.summercattle.commons.utils.spring.SpringResourceLoader;

@ClassLoadLevel(1)
public class SpringClassResourceLoader implements ClassResourceLoader {

	private ResourceLoader resourceLoader = SpringResourceLoader.getResourceLoader();

	@Override
	public boolean existResource(String location) throws CommonException {
		try {
			boolean result = false;
			org.springframework.core.io.Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
					.getResources(getLocation(location));
			for (org.springframework.core.io.Resource resource : resources) {
				if (resource.exists()) {
					result = true;
					break;
				}
			}
			return result;
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Resource getResource(String location) throws CommonException {
		try {
			org.springframework.core.io.Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
					.getResources(getLocation(location));
			Resource result = null;
			for (org.springframework.core.io.Resource resource : resources) {
				if (resource.exists()) {
					result = new SpringResourceImpl(resource);
					break;
				}
			}
			return result;
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Set<Resource> getResources(String location) throws CommonException {
		Set<Resource> result = null;
		try {
			org.springframework.core.io.Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
					.getResources(getLocation(location));
			result = new LinkedHashSet<Resource>();
			for (org.springframework.core.io.Resource resource : resources) {
				if (resource.exists()) {
					result.add(new SpringResourceImpl(resource));
				}
			}
			return result;
		}
		catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				return result;
			}
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private String getLocation(String location) {
		String result;
		if (location.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX) || location.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)) {
			result = location;
		}
		else {
			result = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + (location.startsWith("/") ? "" : "/") + location;
		}
		return result;
	}
}