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
package com.gitlab.summercattle.commons.db.handle.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.gitlab.summercattle.commons.db.constants.IsolationLevel;
import com.gitlab.summercattle.commons.db.constants.TransactionLevel;
import com.gitlab.summercattle.commons.db.dialect.Dialect;
import com.gitlab.summercattle.commons.db.handle.Dal;
import com.gitlab.summercattle.commons.db.handle.DbTransaction;
import com.gitlab.summercattle.commons.db.handle.SimpleDal;
import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.cache.CacheManager;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;
import com.gitlab.summercattle.commons.utils.reflect.annotation.ClassLoadLevel;
import com.gitlab.summercattle.commons.utils.spring.SpringContext;
import com.google.inject.Inject;

@ClassLoadLevel(1)
public class DbTransactionImpl implements DbTransaction {

	private static final Logger logger = LoggerFactory.getLogger(DbTransactionImpl.class);

	private static final int TRANSACTION_TIMEOUT = 3600;

	@Inject
	private CacheManager cacheManager;

	@Override
	public <T> T doDal(Dal<T> dal) throws CommonException {
		return doDal(TransactionLevel.REQUIRED, dal);
	}

	@Override
	public <T> T doDal(TransactionLevel transactionLevel, Dal<T> dal) throws CommonException {
		return doDal(transactionLevel, IsolationLevel.DEFAULT, TRANSACTION_TIMEOUT, dal);
	}

	@Override
	public <T> T doDal(TransactionLevel transactionLevel, IsolationLevel isolationLevel, int timeout, Dal<T> dal) throws CommonException {
		DataSourceTransactionManager transactionManager = (DataSourceTransactionManager) SpringContext.getBean("transactionManager");
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setPropagationBehavior(getPropagationBehavior(transactionLevel));
		transactionDefinition.setIsolationLevel(getIsolationLevel(isolationLevel));
		transactionDefinition.setTimeout(timeout);
		TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		Dialect dialect = SpringContext.getBean(Dialect.class);
		DataSource dataSource = null;
		Connection conn = null;
		try {
			dataSource = transactionManager.getDataSource();
			conn = DataSourceUtils.getConnection(dataSource);
			T result = dal.execute(new DalContextImpl(dialect, conn, cacheManager));
			transactionManager.commit(transactionStatus);
			logger.debug("数据库事务已提交");
			return result;
		}
		catch (Throwable e) {
			logger.error(e.getMessage(), e);
			transactionManager.rollback(transactionStatus);
			logger.debug("数据库事务已回滚");
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}

	@Override
	public <T> T doSimpleDal(SimpleDal<T> dal) throws CommonException {
		return doSimpleDal(TransactionLevel.REQUIRED, dal);
	}

	@Override
	public <T> T doSimpleDal(TransactionLevel transactionLevel, SimpleDal<T> dal) throws CommonException {
		return doSimpleDal(transactionLevel, IsolationLevel.DEFAULT, 3600, dal);
	}

	@Override
	public <T> T doSimpleDal(TransactionLevel transactionLevel, IsolationLevel isolationLevel, int timeout, SimpleDal<T> dal) throws CommonException {
		DataSourceTransactionManager transactionManager = (DataSourceTransactionManager) SpringContext.getBean("transactionManager");
		Dialect dialect = SpringContext.getBean(Dialect.class);
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setPropagationBehavior(getPropagationBehavior(transactionLevel));
		transactionDefinition.setIsolationLevel(getIsolationLevel(isolationLevel));
		transactionDefinition.setTimeout(timeout);
		TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		DataSource dataSource = null;
		Connection conn = null;
		try {
			dataSource = transactionManager.getDataSource();
			conn = DataSourceUtils.getConnection(dataSource);
			T result = dal.execute(new SimpleDalContextImpl(dialect, conn));
			transactionManager.commit(transactionStatus);
			return result;
		}
		catch (Throwable e) {
			logger.error(e.getMessage(), e);
			transactionManager.rollback(transactionStatus);
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}

	private int getIsolationLevel(IsolationLevel isolationLevel) throws CommonException {
		if (null == isolationLevel) {
			return TransactionDefinition.ISOLATION_DEFAULT;
		}
		if (isolationLevel == IsolationLevel.DEFAULT) {
			return TransactionDefinition.ISOLATION_DEFAULT;
		}
		else if (isolationLevel == IsolationLevel.READ_COMMITTED) {
			return TransactionDefinition.ISOLATION_READ_COMMITTED;
		}
		else if (isolationLevel == IsolationLevel.READ_UNCOMMITTED) {
			return TransactionDefinition.ISOLATION_READ_UNCOMMITTED;
		}
		else if (isolationLevel == IsolationLevel.REPEATABLE_READ) {
			return TransactionDefinition.ISOLATION_REPEATABLE_READ;
		}
		else if (isolationLevel == IsolationLevel.SERIALIZABLE) {
			return TransactionDefinition.ISOLATION_SERIALIZABLE;
		}
		throw new CommonException("不支持的隔离级别'" + isolationLevel.toString() + "'");
	}

	private int getPropagationBehavior(TransactionLevel transactionLevel) throws CommonException {
		if (null == transactionLevel) {
			return TransactionDefinition.PROPAGATION_REQUIRED;
		}
		if (transactionLevel == TransactionLevel.MANDATORY) {
			return TransactionDefinition.PROPAGATION_MANDATORY;
		}
		else if (transactionLevel == TransactionLevel.NEVER) {
			return TransactionDefinition.PROPAGATION_NEVER;
		}
		else if (transactionLevel == TransactionLevel.NOT_SUPPORTED) {
			return TransactionDefinition.PROPAGATION_NOT_SUPPORTED;
		}
		else if (transactionLevel == TransactionLevel.REQUIRED) {
			return TransactionDefinition.PROPAGATION_REQUIRED;
		}
		else if (transactionLevel == TransactionLevel.REQUIRES_NEW) {
			return TransactionDefinition.PROPAGATION_REQUIRES_NEW;
		}
		else if (transactionLevel == TransactionLevel.SUPPORTS) {
			return TransactionDefinition.PROPAGATION_SUPPORTS;
		}
		throw new CommonException("不支持的事务级别'" + transactionLevel.toString() + "'");
	}
}