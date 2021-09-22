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
package io.github.summercattle.commons.db.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public abstract class AbstractLimitHandler implements LimitHandler {

	public boolean useMaxForLimit() {
		return false;
	}

	@Override
	public int bindLimitParametersAtStartOfQuery(int startRow, int perPageRows, PreparedStatement statement, int index) throws CommonException {
		return bindLimitParametersFirst() ? bindLimitParameters(startRow, perPageRows, statement, index) : 0;
	}

	@Override
	public int bindLimitParametersAtEndOfQuery(int startRow, int perPageRows, PreparedStatement statement, int index) throws CommonException {
		return !bindLimitParametersFirst() ? bindLimitParameters(startRow, perPageRows, statement, index) : 0;
	}

	public int bindLimitParameters(int startRow, int perPageRows, PreparedStatement statement, int index) throws CommonException {
		try {
			int lastRow = getMaxOrLimit(startRow, perPageRows);
			boolean hasFirstRow = supportsLimitOffset() && startRow > 0;
			final boolean reverse = bindLimitParametersInReverseOrder();
			if (hasFirstRow) {
				statement.setInt(index + (reverse ? 1 : 0), startRow);
			}
			statement.setInt(index + (reverse || !hasFirstRow ? 0 : 1), lastRow);
			return hasFirstRow ? 2 : 1;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	@Override
	public Object[] getOutputParameters(Object[] params, int startRow, int perPageRows) {
		int lastRow = getMaxOrLimit(startRow, perPageRows);
		boolean hasFirstRow = supportsLimitOffset() && startRow > 0;
		final boolean reverse = bindLimitParametersInReverseOrder();
		Object[] outParams = new Object[params.length + (hasFirstRow ? 2 : 1)];
		int index = 0;
		if (bindLimitParametersFirst()) {
			if (hasFirstRow) {
				outParams[index + (reverse ? 1 : 0)] = startRow;
			}
			outParams[index + (reverse || !hasFirstRow ? 0 : 1)] = lastRow;
			index += hasFirstRow ? 2 : 1;
		}
		for (int i = 0; i < params.length; i++) {
			outParams[index] = params[i];
			index++;
		}
		if (!bindLimitParametersFirst()) {
			if (hasFirstRow) {
				outParams[index + (reverse ? 1 : 0)] = startRow;
			}
			outParams[index + (reverse || !hasFirstRow ? 0 : 1)] = lastRow;
			index += hasFirstRow ? 2 : 1;
		}
		return outParams;
	}

	private int getMaxOrLimit(int startRow, int perPageRows) {
		int maxRows = useMaxForLimit() ? perPageRows + startRow : perPageRows;
		// Use Integer.MAX_VALUE on overflow
		if (maxRows < 0) {
			return Integer.MAX_VALUE;
		}
		else {
			return maxRows;
		}
	}

	public boolean bindLimitParametersInReverseOrder() {
		return false;
	}

	public boolean bindLimitParametersFirst() {
		return false;
	}
}