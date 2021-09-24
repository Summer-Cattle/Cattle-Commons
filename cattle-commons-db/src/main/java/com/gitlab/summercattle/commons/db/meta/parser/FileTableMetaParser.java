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
package com.gitlab.summercattle.commons.db.meta.parser;

import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.summercattle.commons.db.configure.DbProperties;
import com.gitlab.summercattle.commons.db.meta.TableMeta;
import com.gitlab.summercattle.commons.db.meta.TableMetaParser;
import com.gitlab.summercattle.commons.db.meta.file.FileTableMeta;
import com.gitlab.summercattle.commons.db.meta.parser.file.FileTableMetaImpl;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.auxiliary.Dom4jUtils;
import com.gitlab.summercattle.commons.utils.reflect.ClassResourceLoader;
import com.gitlab.summercattle.commons.utils.reflect.Resource;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;
import com.google.inject.Inject;

public class FileTableMetaParser implements TableMetaParser {

	private static final Logger logger = LoggerFactory.getLogger(FileTableMetaParser.class);

	@Inject
	private ClassResourceLoader classResourceLoader;

	@Override
	public void parser(List<TableMeta> tables) throws CommonException {
		Set<Resource> resources = classResourceLoader.getResources(MetaParserConstants.DATA_META_PATH);
		if (null != resources) {
			DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
			for (Resource resource : resources) {
				String resourceDescription = resource.getDescription();
				logger.debug("处理数据表定义文件:'{}'", resourceDescription);
				Document document = Dom4jUtils.getDocument(resource.getInputStream());
				Element rootElement = document.getRootElement();
				if (rootElement.getName().equals("MetaData")) {
					if (TableMetaUtils.allowTable(rootElement)) {
						FileTableMeta fileTable = new FileTableMetaImpl();
						fileTable.from(dbProperties, rootElement);
						TableMetaUtils.checkTable(tables, "数据表定义文件'" + resourceDescription + "'", fileTable);
						logger.debug("加载数据表名:'{}'", fileTable.getName());
						tables.add(fileTable);
					}
				}
			}
		}
	}
}