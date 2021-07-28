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
package io.github.summercattle.commons.webmvc.configure;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson.support.spring.JSONPResponseBodyAdvice;

import io.github.summercattle.commons.webmvc.error.ErrorExceptionController;
import io.github.summercattle.commons.webmvc.error.ErrorExceptionHandler;

@Configuration(proxyBeanMethods = false)
@EnableWebMvc
@ComponentScan(basePackageClasses = { ErrorExceptionController.class, ErrorExceptionHandler.class })
@PropertySource("classpath:/io/github/summercattle/commons/webmvc/configure/webmvc.properties")
@EnableConfigurationProperties(ErrorFrontendPageProperties.class)
public class WebMvcAutoConfiguration implements WebMvcConfigurer {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter< ? >> converters) {
		FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
		List<MediaType> mediaTypes = new Vector<MediaType>();
		mediaTypes.add(new MediaType("application", "json"));
		mediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
		converter.setSupportedMediaTypes(mediaTypes);
		converters.add(0, converter);
	}

	@Bean
	public JSONPResponseBodyAdvice jsonpResponseBodyAdvice() {
		return new JSONPResponseBodyAdvice();
	}
}