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
package com.gitlab.summercattle.commons.utils.auxiliary;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class FileUtils {

	public static URL getFileURL(String fileLocation) {
		URL result = null;
		File file = new File(fileLocation);
		if (file.exists()) {
			result = getFileURL(file);
		}
		return result;
	}

	public static URL getFileURL(File file) {
		URL result = null;
		try {
			String path = file.getCanonicalPath();
			result = new URL("file:" + (path.startsWith("/") ? path : "/" + path));
		}
		catch (IOException e) {
		}
		return result;
	}
}