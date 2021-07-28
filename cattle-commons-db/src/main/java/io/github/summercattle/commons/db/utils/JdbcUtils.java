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
package io.github.summercattle.commons.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.exception.CommonRuntimeException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ClassType;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;

public class JdbcUtils {

	private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

	private static final int BATCH_RECORDS = 1000;

	public static ResultSet executeQuery(PreparedStatement ps, String info) throws CommonException {
		try {
			long startTime = (new Date()).getTime();
			ResultSet rs = ps.executeQuery();
			long endTime = (new Date()).getTime();
			logger.debug(info + ",执行时间:" + (endTime - startTime) + "毫秒");
			return rs;
		}
		catch (SQLException e) {
			logger.error(info + ",出现异常:" + e.getMessage());
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static void closeResultSet(ResultSet rs) {
		if (null != rs) {
			try {
				rs.close();
			}
			catch (SQLException e) {
			}
		}
	}

	public static void closeStatement(Statement st) {
		if (null != st) {
			try {
				st.close();
			}
			catch (SQLException e) {
			}
		}
	}

	public static void setDbObject(PreparedStatement ps, int index, Object value) throws CommonException {
		try {
			if (null != value) {
				ClassType valueType = ReflectUtils.getClassType(value.getClass());
				if (valueType == ClassType.Time || valueType == ClassType.Date) {
					ps.setObject(index, ReflectUtils.convertValue(ClassType.Timestamp, value));
				}
				else if (valueType == ClassType.Boolean) {
					ps.setObject(index, ReflectUtils.convertValue(ClassType.Int, value));
				}
				else {
					ps.setObject(index, value);
				}
			}
			else {
				ps.setObject(index, null);
			}
		}
		catch (SQLException e) {
			if (value == null) {
				logger.error("设置对象异常(索引:" + index + ",值:空值)");
			}
			else {
				logger.error("设置对象异常(索引:" + index + ",值:" + value.toString() + ",类型:" + ReflectUtils.getClassType(value.getClass()).toString() + ")");
			}
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static int executeUpdate(PreparedStatement ps, String info) throws CommonException {
		try {
			long startTime = (new Date()).getTime();
			int records = ps.executeUpdate();
			long endTime = (new Date()).getTime();
			logger.debug(info + ",成功处理:" + records + "条数据,执行时间:" + (endTime - startTime) + "毫秒");
			return records;
		}
		catch (SQLException e) {
			logger.error(info + ",出现异常:" + e.getMessage());
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static void execute(CallableStatement cs, String info) throws CommonException {
		try {
			long startTime = (new Date()).getTime();
			cs.execute();
			long endTime = (new Date()).getTime();
			logger.debug(info + ",执行时间:" + (endTime - startTime) + "毫秒");
		}
		catch (SQLException e) {
			logger.error(info + ",出现异常:" + e.getMessage());
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static int addBatch(PreparedStatement ps, String info, int currentFrequency) throws CommonException {
		try {
			ps.addBatch();
			currentFrequency++;
			if (currentFrequency == BATCH_RECORDS) {
				completeBatch(ps, info, currentFrequency);
				currentFrequency = 0;
			}
			return currentFrequency;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static void completeBatch(PreparedStatement ps, String info, int currentFrequency) throws CommonException {
		if (currentFrequency > 0) {
			try {
				long startTime = (new Date()).getTime();
				int[] results = ps.executeBatch();
				long endTime = (new Date()).getTime();
				int success = 0;
				int fail = 0;
				for (int result : results) {
					if (result == 1 || result == PreparedStatement.SUCCESS_NO_INFO) {
						success++;
					}
					else if (result == 0 || result == PreparedStatement.EXECUTE_FAILED) {
						fail++;
					}
				}
				logger.debug(info + ",成功处理:" + success + "条数据,失败:" + fail + "条数据,执行时间:" + (endTime - startTime) + "毫秒");
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			finally {
				try {
					ps.clearBatch();
				}
				catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public static void executeSQL(Connection conn, String strSQL) throws CommonException {
		if (StringUtils.isNotBlank(strSQL)) {
			String[] sqls = strSQL.split(";");
			for (String sql : sqls) {
				if (StringUtils.isNotBlank(sql)) {
					PreparedStatement ps = null;
					try {
						ps = conn.prepareStatement(sql);
						long startTime = (new Date()).getTime();
						ps.execute();
						long endTime = (new Date()).getTime();
						logger.debug("执行SQL语句:" + sql + ",执行时间:" + (endTime - startTime) + "毫秒");
					}
					catch (SQLException e) {
						logger.error("执行SQL语句:" + sql + ",出现异常:" + e.getMessage());
						throw ExceptionWrapUtils.wrap(e);
					}
					finally {
						closeStatement(ps);
					}
				}
			}
		}
	}

	public static DataType getDataType(int jdbcDataType) {
		if (jdbcDataType == Types.VARCHAR) {
			return DataType.String;
		}
		else if (jdbcDataType == Types.NVARCHAR) {
			return DataType.NString;
		}
		else if (jdbcDataType == Types.TIMESTAMP) {
			return DataType.Timestamp;
		}
		else if (jdbcDataType == Types.DATE) {
			return DataType.Date;
		}
		else if (jdbcDataType == Types.TIME) {
			return DataType.Time;
		}
		else if (jdbcDataType == Types.LONGVARCHAR || jdbcDataType == Types.CLOB) {
			return DataType.Clob;
		}
		else if (jdbcDataType == Types.LONGNVARCHAR || jdbcDataType == Types.NCLOB) {
			return DataType.NClob;
		}
		else if (jdbcDataType == Types.LONGVARBINARY || jdbcDataType == Types.BLOB) {
			return DataType.Blob;
		}
		else if (jdbcDataType == Types.DECIMAL || jdbcDataType == Types.NUMERIC || jdbcDataType == Types.INTEGER) {
			return DataType.Number;
		}
		throw new CommonRuntimeException("未知的JDBC数据类型'" + jdbcDataType + "'");
	}
}