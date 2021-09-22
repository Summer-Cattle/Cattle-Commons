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
package io.github.summercattle.commons.db.handle.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.handle.AbstractDalContext;
import io.github.summercattle.commons.db.sqlparser.StatementVisitorImpl;
import io.github.summercattle.commons.db.utils.JdbcUtils;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.ArrayUtils;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class AbstractDalContextImpl implements AbstractDalContext {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDalContextImpl.class);

	protected Dialect dialect;

	protected Connection conn;

	public AbstractDalContextImpl(Dialect dialect, Connection conn) {
		this.dialect = dialect;
		this.conn = conn;
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public void execute(String sql, Object... params) throws CommonException {
		checkExecuteSQL(sql);
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			String info = "执行SQL语句:" + sql + ",参数值:" + (params != null && params.length > 0 ? ArrayUtils.toString(params) : "无");
			if (params != null && params.length > 0) {
				setParams(ps, 1, params, info);
			}
			JdbcUtils.executeUpdate(ps, info);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeStatement(ps);
		}
	}

	protected void checkSQL(String strSQL) throws CommonException {
		if (org.apache.commons.lang3.StringUtils.isBlank(strSQL)) {
			throw new CommonException("SQL语句为空");
		}
	}

	private void checkExecuteSQL(String strSQL) throws CommonException {
		checkSQL(strSQL);
		String sql = strSQL.trim().toLowerCase();
		if (sql.startsWith("select")) {
			throw new CommonException("只允许使用CREATE、ALTER、DROP、INSERT、UPDATE及DELETE语句");
		}
	}

	protected int setParams(PreparedStatement ps, int index, Object[] params, String info) throws CommonException {
		try {
			int lIndex = index;
			for (int i = 0; i < params.length; i++) {
				JdbcUtils.setDbObject(ps, lIndex, params[i]);
				lIndex++;
			}
			return lIndex;
		}
		catch (CommonException e) {
			logger.warn(info + ",出现异常:" + e.getMessage());
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private Statement parser(String sql) throws CommonException {
		try {
			long startTime = (new Date()).getTime();
			Statement statement = CCJSqlParserUtil.parse(sql);
			statement.accept(new StatementVisitorImpl());
			long endTime = (new Date()).getTime();
			logger.debug("解析SQL:" + sql + ",执行时间:" + (endTime - startTime) + "毫秒");
			return statement;
		}
		catch (JSQLParserException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	protected String parserSQL(String sql) throws CommonException {
		Statement statement = parser(sql);
		return statement.toString();
	}
}