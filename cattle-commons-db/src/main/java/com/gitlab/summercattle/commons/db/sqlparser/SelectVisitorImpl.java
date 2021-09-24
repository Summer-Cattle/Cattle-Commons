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

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class SelectVisitorImpl implements SelectVisitor {

	@Override
	public void visit(PlainSelect plainSelect) {
		// 访问from
		FromItem fromItem = plainSelect.getFromItem();
		FromItemVisitorImpl fromItemVisitorImpl = new FromItemVisitorImpl();
		fromItem.accept(fromItemVisitorImpl);
		// 访问join
		if (plainSelect.getJoins() != null) {
			for (Join join : plainSelect.getJoins()) {
				join.getRightItem().accept(new FromItemVisitorImpl());
			}
		}
		// 访问 select
		if (plainSelect.getSelectItems() != null) {
			for (SelectItem item : plainSelect.getSelectItems()) {
				item.accept(new SelectItemVisitorImpl());
			}
		}
		// 访问where
		if (plainSelect.getWhere() != null) {
			plainSelect.getWhere().accept(new ExpressionVisitorImpl());
		}
		// 访问 order by
		if (plainSelect.getOrderByElements() != null) {
			for (OrderByElement orderByElement : plainSelect.getOrderByElements()) {
				orderByElement.getExpression().accept(new ExpressionVisitorImpl());
			}
		}
		// 访问having
		if (plainSelect.getHaving() != null) {
			plainSelect.getHaving().accept(new ExpressionVisitorImpl());
		}
		//group by
		if (null != plainSelect.getGroupBy()) {
			List<Expression> expressions = plainSelect.getGroupBy().getGroupByExpressionList().getExpressions();
			for (Expression expression : expressions) {
				expression.accept(new ExpressionVisitorImpl());
			}
		}
	}

	@Override
	public void visit(SetOperationList setOpList) {
		for (SelectBody plainSelect : setOpList.getSelects()) {
			plainSelect.accept(new SelectVisitorImpl());
		}
	}

	@Override
	public void visit(WithItem withItem) {
		withItem.getSubSelect().getSelectBody().accept(new SelectVisitorImpl());
	}

	@Override
	public void visit(ValuesStatement aThis) {
	}
}