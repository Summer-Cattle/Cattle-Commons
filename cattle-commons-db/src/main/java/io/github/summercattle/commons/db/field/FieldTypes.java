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
package io.github.summercattle.commons.db.field;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

import io.github.summercattle.commons.exception.CommonException;

public class FieldTypes {

	private static final AbstractField BIG_INTEGER = new BigIntegerField();

	private static final AbstractField BINARY = new BinaryField();

	private static final AbstractField BOOLEAN = new BooleanField();

	private static final AbstractField DATE = new DateField();

	private static final AbstractField DOUBLE = new DoubleField();

	private static final AbstractField INTEGER = new IntegerField();

	private static final AbstractField SHORT = new ShortField();

	private static final AbstractField BYTE = new ByteField();

	private static final AbstractField TIME = new TimeField();

	private static final AbstractField TIMESTAMP = new TimestampField();

	private static final AbstractField STRING = new StringField();

	private static final AbstractField NSTRING = new NStringField();

	private static final AbstractField BIG_DECIMAL = new BigDecimalField();

	private static final AbstractField BLOB = new BlobField();

	private static final AbstractField CLOB = new ClobField();

	private static final AbstractField NCLOB = new NClobField();

	public static AbstractField getType(String table, String field, int code) throws CommonException {
		AbstractField fieldType = null;
		if (code == Types.BIGINT) {
			fieldType = FieldTypes.BIG_INTEGER;
		}
		else if (code == Types.BINARY || code == Types.VARBINARY || code == Types.LONGVARBINARY) {
			fieldType = FieldTypes.BINARY;
		}
		else if (code == Types.BIT) {
			fieldType = FieldTypes.BOOLEAN;
		}
		else if (code == Types.CHAR || code == Types.NCHAR) {
			fieldType = FieldTypes.STRING;
		}
		else if (code == Types.DATE) {
			fieldType = FieldTypes.DATE;
		}
		else if (code == Types.DOUBLE) {
			fieldType = FieldTypes.DOUBLE;
		}
		else if (code == Types.INTEGER) {
			fieldType = FieldTypes.INTEGER;
		}
		else if (code == Types.SMALLINT) {
			fieldType = FieldTypes.SHORT;
		}
		else if (code == Types.TINYINT) {
			fieldType = FieldTypes.BYTE;
		}
		else if (code == Types.TIME) {
			fieldType = FieldTypes.TIME;
		}
		else if (code == Types.TIMESTAMP) {
			fieldType = FieldTypes.TIMESTAMP;
		}
		else if (code == Types.VARCHAR || code == Types.LONGVARCHAR) {
			fieldType = FieldTypes.STRING;
		}
		else if (code == Types.NVARCHAR || code == Types.LONGNVARCHAR) {
			fieldType = FieldTypes.NSTRING;
		}
		else if (code == Types.NUMERIC || code == Types.DECIMAL) {
			fieldType = FieldTypes.BIG_DECIMAL;
		}
		else if (code == Types.BLOB) {
			fieldType = FieldTypes.BLOB;
		}
		else if (code == Types.CLOB) {
			fieldType = FieldTypes.CLOB;
		}
		else if (code == Types.NCLOB) {
			fieldType = FieldTypes.NCLOB;
		}
		else {
			throw new CommonException((StringUtils.isNotBlank(table) ? "表" + table + "的" : "") + "字段" + field + "为无效的类型" + code);
		}
		return fieldType;
	}
}