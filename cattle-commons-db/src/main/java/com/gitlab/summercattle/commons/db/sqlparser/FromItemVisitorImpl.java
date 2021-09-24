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
package com.gitlab.summercattle.commons.db.sqlparser;

import org.apache.commons.lang3.StringUtils;

import com.gitlab.summercattle.commons.db.DbUtils;
import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.meta.TableMeta;
import com.gitlab.summercattle.commons.exception.CommonRuntimeException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

public class FromItemVisitorImpl implements FromItemVisitor {

	@Override
	public void visit(Table tableName) {
		Dialect dialect = SpringContext.getBean(Dialect.class);
		String schema = tableName.getSchemaName();
		if (StringUtils.isNotBlank(schema)) {
//			if (!schema.equals(dialect.getSchema())) {
//				return;
//			}
		}
		String name = tableName.getName();
		try {
			if (DbUtils.getDbMetaModel().existTable(name)) {
				TableMeta tableMeta = DbUtils.getDbMetaModel().getTable(name);
				name = tableMeta.getName();
				if (!DbUtils.getDbStruct().existTable(name)) {
					throw new CommonRuntimeException("表'" + name + "'不存在");
				}
				tableName.setName(name);
			}
			else {
				if (!DbUtils.getDbStruct().existTable(name)) {
					if (!DbUtils.getDbStruct().existView(name)) {
						throw new CommonRuntimeException("表或视图'" + name + "'不存在");
					}
				}
			}
		}
		catch (Throwable e) {
			throw ExceptionWrapUtils.wrapRuntime(e);
		}
	}

	@Override
	public void visit(SubSelect subSelect) {
		// 如果是子查询的话返回到select接口实现类
		subSelect.getSelectBody().accept(new SelectVisitorImpl());
	}

	@Override
	public void visit(SubJoin subjoin) {
		subjoin.getLeft().accept(new FromItemVisitorImpl());
		for (Join join : subjoin.getJoinList()) {
			join.getRightItem().accept(new FromItemVisitorImpl());
		}
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		lateralSubSelect.getSubSelect().getSelectBody().accept(new SelectVisitorImpl());
	}

	@Override
	public void visit(ValuesList valuesList) {
	}

	@Override
	public void visit(TableFunction tableFunction) {
	}

	@Override
	public void visit(ParenthesisFromItem aThis) {
	}
}