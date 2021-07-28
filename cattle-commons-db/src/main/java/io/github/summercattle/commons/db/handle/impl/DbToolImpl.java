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

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.github.summercattle.commons.db.constants.DataConstants;
import io.github.summercattle.commons.db.constants.DatabaseType;
import io.github.summercattle.commons.db.constants.TransactionLevel;
import io.github.summercattle.commons.db.dialect.Dialect;
import io.github.summercattle.commons.db.handle.DbSecurityKey;
import io.github.summercattle.commons.db.handle.DbTool;
import io.github.summercattle.commons.db.handle.DbTransaction;
import io.github.summercattle.commons.db.object.DataTable;
import io.github.summercattle.commons.db.utils.JdbcUtils;
import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.auxiliary.ArrayUtils;
import io.github.summercattle.commons.utils.auxiliary.CompressUtils;
import io.github.summercattle.commons.utils.auxiliary.ObjectUtils;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;
import io.github.summercattle.commons.utils.reflect.ReflectUtils;
import io.github.summercattle.commons.utils.security.CommonEncryptUtils;
import io.github.summercattle.commons.utils.security.constants.CommonEncryptType;
import io.github.summercattle.commons.utils.security.constants.PaddingType;

public class DbToolImpl implements DbTool {

	@Inject
	private DbTransaction dbTransaction;

	@Inject
	private DbSecurityKey dbSecurityKey;

	@Override
	public Date getCurrentDate() throws CommonException {
		return dbTransaction.doSimpleDal(context -> {
			if (context.getDialect().supportsCurrentTimestampSelection()) {
				return getCurrentTimestampSelect(context.getConnection(), context.getDialect().getCurrentTimestampSelectString());
			}
			if (context.getDialect().isCurrentTimestampSelectStringCallable()) {
				return getCurrentTimestampSelectCallable(context.getConnection(), context.getDialect().getCurrentTimestampCallString());
			}
			return new Date();
		});
	}

	private Date getCurrentTimestampSelectCallable(Connection conn, String strSQL) throws CommonException {
		CallableStatement cs = null;
		try {
			cs = conn.prepareCall(strSQL);
			cs.registerOutParameter(1, Types.TIMESTAMP);
			JdbcUtils.execute(cs, "执行SQL语句:" + strSQL);
			return new Date(cs.getTimestamp(1).getTime());
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeStatement(cs);
		}
	}

	private Date getCurrentTimestampSelect(Connection conn, String strSQL) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(strSQL);
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + strSQL);
			rs.next();
			return new Date(rs.getTimestamp(1).getTime());
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
	}

	@Override
	public long getSequenceNextVal(String sequenceName) throws CommonException {
		if (StringUtils.isBlank(sequenceName)) {
			throw new CommonException("序列名为空");
		}
		return dbTransaction.doSimpleDal(TransactionLevel.REQUIRES_NEW, context -> {
			String lSequenceName = sequenceName.toUpperCase();
			if (context.getDialect().supportsSequences()) {
				if (!isSequence(context.getDialect(), context.getConnection(), lSequenceName)) {
					String strSQL = context.getDialect().getCreateSequenceString(lSequenceName);
					JdbcUtils.executeSQL(context.getConnection(), strSQL);
				}
				return getSequenceNextValue(context.getDialect(), context.getConnection(), lSequenceName);
			}
			else {
				if (!context.getDialect().existTable(context.getConnection(), DataConstants.SEQUENCE_TABLE_NAME)) {
					throw new CommonException("自定义序列表不存在");
				}
				return getCustomNextVal(context.getDialect(), context.getConnection(), lSequenceName);
			}
		});
	}

	private boolean isSequence(Dialect dialect, Connection conn, String sequenceName) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			boolean hasSequence = false;
			String strSQL = dialect.getQuerySequencesString();
			ps = conn.prepareStatement(strSQL);
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + strSQL);
			while (rs.next()) {
				if (rs.getString(1).equals("EAPP_" + sequenceName)) {
					hasSequence = true;
					break;
				}
			}
			return hasSequence;
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
	}

	private long getSequenceNextValue(Dialect dialect, Connection conn, String sequenceName) throws CommonException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String strSQL = dialect.getSequenceNextValString(sequenceName);
			ps = conn.prepareStatement(strSQL);
			rs = JdbcUtils.executeQuery(ps, "执行SQL语句:" + strSQL);
			rs.next();
			return rs.getLong(1);
		}
		catch (SQLException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
		}
	}

	private synchronized long getCustomNextVal(Dialect dialect, Connection conn, String sequenceName) throws CommonException {
		int rows = 0;
		long value = 0;
		do {
			PreparedStatement queryPs = null;
			ResultSet queryRs = null;
			try {
				String sql = "select sequence_value from " + dialect.appendLock(DataConstants.SEQUENCE_TABLE_NAME) + " where sequence_name=?"
						+ dialect.getForUpdateString();
				queryPs = conn.prepareStatement(sql);
				queryPs.setString(1, sequenceName);
				queryRs = JdbcUtils.executeQuery(queryPs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { sequenceName }));
				if (!queryRs.next()) {
					PreparedStatement insertPs = null;
					try {
						sql = "insert into " + DataConstants.SEQUENCE_TABLE_NAME + " (sequence_name,sequence_value) values (?,?)";
						insertPs = conn.prepareStatement(sql);
						insertPs.setString(1, sequenceName);
						insertPs.setLong(2, value);
						JdbcUtils.executeUpdate(insertPs, "执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { sequenceName, value }));
					}
					finally {
						JdbcUtils.closeStatement(insertPs);
					}
				}
				else {
					value = queryRs.getLong(1);
				}
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			finally {
				JdbcUtils.closeResultSet(queryRs);
				JdbcUtils.closeStatement(queryPs);
			}
			PreparedStatement updatePs = null;
			try {
				String sql = "update " + DataConstants.SEQUENCE_TABLE_NAME + " set sequence_value=? where sequence_name=? and sequence_value=?";
				updatePs = conn.prepareStatement(sql);
				updatePs.setLong(1, value + 1);
				updatePs.setString(2, sequenceName);
				updatePs.setLong(3, value);
				rows = JdbcUtils.executeUpdate(updatePs,
						"执行SQL语句:" + sql + ",参数值:" + ArrayUtils.toString(new Object[] { value + 1, sequenceName, value }));
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			finally {
				JdbcUtils.closeStatement(updatePs);
			}
		}
		while (rows == 0);
		return value + 1;
	}

	@Override
	public boolean validConnection() throws CommonException {
		return dbTransaction.doSimpleDal(context -> {
			if (context.getDialect().getType() == DatabaseType.MySQL) {
				Class< ? > clazz = null;
				try {
					clazz = Class.forName("com.mysql.jdbc.MySQLConnection");
				}
				catch (ClassNotFoundException e2) {
					try {
						clazz = Class.forName("com.mysql.cj.jdbc.ConnectionImpl");
					}
					catch (ClassNotFoundException e1) {
					}
				}
				if (null != clazz) {
					try {
						Method pingMethod = ReflectUtils.getMethod(clazz, "pingInternal", boolean.class, int.class);
						if (null != pingMethod) {
							if (clazz.isAssignableFrom(context.getConnection().getClass())) {
								ReflectUtils.invokeObjectMethod(pingMethod, context.getConnection(), new Object[] { true, 1000 * 1000 });
								return true;
							}
						}
					}
					catch (CommonException e3) {
					}
				}
			}
			String validateQuery = context.getDialect().getValidateQuery();
			if (StringUtils.isBlank(validateQuery)) {
				return true;
			}
			try {
				if (context.getConnection().isClosed()) {
					return false;
				}
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			Statement st = null;
			ResultSet rs = null;
			try {
				st = context.getConnection().createStatement();
				if (context.getDialect().getType() == DatabaseType.SQLServer) {
					st = context.getConnection().createStatement();
					st.execute(validateQuery);
				}
				else {
					st.setQueryTimeout(1);
					rs = st.executeQuery(validateQuery);
				}
				return true;
			}
			catch (SQLException e) {
				throw ExceptionWrapUtils.wrap(e);
			}
			finally {
				JdbcUtils.closeResultSet(rs);
				JdbcUtils.closeStatement(st);
			}
		});
	}

	@Override
	public void saveConfig(String name, boolean encrypt, Object value) throws CommonException {
		dbTransaction.doDal(ctx -> {
			DataTable dt = ctx.select("ConfigInfo", "CONFIG_NAME=?", new Object[] { name });
			if (dt.first()) {
				if (null == value) {
					dt.delete();
				}
			}
			else {
				if (null != value) {
					dt.insert();
					dt.setString("CONFIG_NAME", name);
				}
			}
			if (null != value) {
				byte[] bytes = ObjectUtils.serialize(value);
				bytes = CompressUtils.compress(CompressorStreamFactory.GZIP, bytes);
				if (encrypt) {
					byte[] encryptKey = dbSecurityKey.getCommonEncryptKey(CommonEncryptType.AES);
					bytes = CommonEncryptUtils.encryptECB(CommonEncryptType.AES, bytes, encryptKey, PaddingType.PKCS7Padding);
				}
				dt.setBoolean("CONFIG_ENCRYPT", encrypt);
				dt.setString("CONFIG_VALUE", Hex.encodeHexString(bytes));
			}
			ctx.save(dt);
			return null;
		});
	}

	@Override
	public Object getConfig(String name) throws CommonException {
		return dbTransaction.doDal(ctx -> {
			DataTable dt = ctx.select("ConfigInfo", "CONFIG_NAME=?", new Object[] { name });
			if (dt.first()) {
				String str = dt.getString("CONFIG_VALUE");
				if (StringUtils.isNotBlank(str)) {
					byte[] bytes;
					try {
						bytes = Hex.decodeHex(str);
					}
					catch (DecoderException e) {
						throw ExceptionWrapUtils.wrap(e);
					}
					if (dt.getBoolean("CONFIG_ENCRYPT")) {
						byte[] encryptKey = dbSecurityKey.getCommonEncryptKey(CommonEncryptType.AES);
						bytes = CommonEncryptUtils.decyrptECB(CommonEncryptType.AES, bytes, encryptKey, PaddingType.PKCS7Padding);
					}
					bytes = CompressUtils.decompress(CompressorStreamFactory.GZIP, bytes);
					return ObjectUtils.deserialize(bytes);
				}
			}
			return null;
		});
	}
}