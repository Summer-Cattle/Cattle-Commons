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
package io.github.summercattle.commons.db.meta.parser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.db.annotation.Table;
import io.github.summercattle.commons.db.configure.DbProperties;
import io.github.summercattle.commons.db.meta.TableMeta;
import io.github.summercattle.commons.db.meta.TableMetaParser;
import io.github.summercattle.commons.db.meta.annotation.AnnotatedTableMeta;
import io.github.summercattle.commons.db.meta.parser.annotation.AnnotatedTableMetaImpl;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.reflect.ClassUtils;
import io.github.summercattle.commons.utils.spring.SpringContext;

public class AnnotatedTableMetaParser implements TableMetaParser {

	private static final Logger logger = LoggerFactory.getLogger(AnnotatedTableMetaParser.class);

	@Override
	public void parser(List<TableMeta> tables) throws CommonException {
		DbProperties dbProperties = SpringContext.getBean(DbProperties.class);
		Class< ? >[] tableClasses = ClassUtils.getTypesAnnotatedWith(Table.class);
		for (Class< ? > tableClazz : tableClasses) {
			logger.debug("处理数据表注解类:{}", tableClazz.getName());
			Table table = tableClazz.getAnnotation(Table.class);
			if (TableMetaUtils.allowTable(table.propertyNames(), table.havingValue(), table.matchIfMissing())) {
				AnnotatedTableMeta annotatedTable = new AnnotatedTableMetaImpl();
				annotatedTable.from(dbProperties, table, tableClazz);
				TableMetaUtils.checkTable(tables, "数据表注解类'" + tableClazz.getName() + "'", annotatedTable);
				logger.debug("加载数据表名:'{}'", annotatedTable.getName());
				tables.add(annotatedTable);
			}
		}
	}
}