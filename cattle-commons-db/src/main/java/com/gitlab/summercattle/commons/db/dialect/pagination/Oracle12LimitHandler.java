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
package com.gitlab.summercattle.commons.db.dialect.pagination;

public class Oracle12LimitHandler extends AbstractLimitHandler {

	private boolean bindLimitParametersInReverseOrder;

	private boolean useMaxForLimit;

	public static final LimitHandler INSTANCE = new Oracle12LimitHandler();

	private static final String ROW_NUMBER_ALIAS = "rownum_";

	Oracle12LimitHandler() {
	}

	@Override
	public String processSql(String sql, int startRow) {
		boolean hasOffset = startRow > 0;
		return processSqlOffsetFetch(sql, hasOffset);
	}

	private String processSqlOffsetFetch(String sql, boolean hasOffset) {
		int forUpdateLastIndex = getForUpdateIndex(sql);
		if (forUpdateLastIndex > -1) {
			return processSql(sql, forUpdateLastIndex, hasOffset);
		}
		bindLimitParametersInReverseOrder = false;
		useMaxForLimit = false;
		final int offsetFetchLength;
		final String offsetFetchString;
		if (hasOffset) {
			offsetFetchString = " offset ? rows fetch next ? rows only";
		}
		else {
			offsetFetchString = " fetch first ? rows only";
		}
		offsetFetchLength = sql.length() + offsetFetchString.length();
		return new StringBuilder(offsetFetchLength).append(sql).append(offsetFetchString).toString();
	}

	private String processSql(String sql, int forUpdateIndex, boolean hasOffset) {
		bindLimitParametersInReverseOrder = true;
		useMaxForLimit = true;
		String forUpdateClause = null;
		boolean isForUpdate = false;
		if (forUpdateIndex > -1) {
			// save 'for update ...' and then remove it
			forUpdateClause = sql.substring(forUpdateIndex);
			sql = sql.substring(0, forUpdateIndex - 1);
			isForUpdate = true;
		}
		StringBuilder pagingSelect;
		int forUpdateClauseLength;
		if (forUpdateClause == null) {
			forUpdateClauseLength = 0;
		}
		else {
			forUpdateClauseLength = forUpdateClause.length() + 1;
		}
		if (hasOffset) {
			pagingSelect = new StringBuilder(sql.length() + forUpdateClauseLength + 98);
			pagingSelect.append("select * from ( select row_.*, rownum " + ROW_NUMBER_ALIAS + " from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) row_ where rownum <= ?) where " + ROW_NUMBER_ALIAS + " > ?");
		}
		else {
			pagingSelect = new StringBuilder(sql.length() + forUpdateClauseLength + 37);
			pagingSelect.append("select * from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) where rownum <= ?");
		}
		if (isForUpdate) {
			pagingSelect.append(" ");
			pagingSelect.append(forUpdateClause);
		}
		return pagingSelect.toString();
	}

	private int getForUpdateIndex(String sql) {
		int forUpdateLastIndex = sql.toLowerCase().lastIndexOf("for update");
		int lastIndexOfQuote = sql.lastIndexOf("'");
		if (forUpdateLastIndex > -1) {
			if (lastIndexOfQuote == -1) {
				return forUpdateLastIndex;
			}
			if (lastIndexOfQuote > forUpdateLastIndex) {
				return -1;
			}
			return forUpdateLastIndex;
		}
		return forUpdateLastIndex;
	}

	@Override
	public boolean useMaxForLimit() {
		return useMaxForLimit;
	}

	@Override
	public boolean bindLimitParametersInReverseOrder() {
		return bindLimitParametersInReverseOrder;
	}

	@Override
	public boolean isFilterPageFields() {
		return true;
	}

	@Override
	public String[] getFilterPageFields() {
		return new String[] { ROW_NUMBER_ALIAS };
	}
}