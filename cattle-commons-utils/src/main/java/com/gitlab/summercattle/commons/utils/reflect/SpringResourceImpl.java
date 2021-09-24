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
package com.gitlab.summercattle.commons.utils.reflect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class SpringResourceImpl implements Resource {

	private static final String FILE_START = "file:/";

	private org.springframework.core.io.Resource resource;

	private String description;

	public SpringResourceImpl(org.springframework.core.io.Resource resource) {
		this.resource = resource;
		if (null != resource) {
			try {
				String file = resource.getURL().getFile();
				if (file.startsWith(FILE_START)) {
					file = file.substring(FILE_START.length());
				}
				description = file;
			}
			catch (IOException e) {
			}
		}
	}

	@Override
	public URL getURL() throws CommonException {
		try {
			return resource.getURL();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public InputStream getInputStream() throws CommonException {
		try {
			return resource.getInputStream();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public URI getURI() throws CommonException {
		try {
			return resource.getURI();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public File getFile() throws CommonException {
		try {
			return resource.getFile();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public long lastModified() throws CommonException {
		try {
			return resource.lastModified();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public long contentLength() throws CommonException {
		try {
			return resource.contentLength();
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public boolean isFile() {
		return StringUtils.isNotBlank(getFileName());
	}

	@Override
	public String getFileName() {
		return resource.getFilename();
	}

	@Override
	public String getPath() throws CommonException {
		String path = getURL().getPath();
		int index = path.lastIndexOf("!/");
		if (index >= 0) {
			return path.substring(index + 2);
		}
		else {
			return getFile().getPath();
		}
	}
}