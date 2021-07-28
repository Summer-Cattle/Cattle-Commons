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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import io.github.summercattle.commons.exception.CommonException;

public interface Resource {

	String getFileName();

	URL getURL() throws CommonException;

	String getPath() throws CommonException;

	String getDescription();

	InputStream getInputStream() throws CommonException;

	URI getURI() throws CommonException;

	boolean isFile();

	File getFile() throws CommonException;

	long lastModified() throws CommonException;

	long contentLength() throws CommonException;
}