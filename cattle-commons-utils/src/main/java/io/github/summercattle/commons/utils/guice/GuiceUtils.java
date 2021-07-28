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
package io.github.summercattle.commons.utils.guice;

import java.util.List;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Stage;

import io.github.summercattle.commons.exception.CommonException;

public class GuiceUtils {

	private static Injector injector;

	public static <T> T getInstance(Class<T> type) throws CommonException {
		if (null != injector) {
			Binding<T> binding = injector.getExistingBinding(Key.get(type));
			if (null != binding) {
				Provider<T> provider = binding.getProvider();
				return provider.get();
			}
		}
		return null;
	}

	public static Injector createInjector(List<Module> modules) {
		injector = Guice.createInjector(Stage.PRODUCTION, modules);
		return injector;
	}
}