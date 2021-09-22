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
package io.github.summercattle.commons.db.meta.parser.file;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Element;

import io.github.summercattle.commons.db.constants.DataType;
import io.github.summercattle.commons.db.meta.file.FileFixedFieldMeta;
import io.github.summercattle.commons.db.meta.impl.FixedFieldMetaImpl;
import io.github.summercattle.commons.exception.CommonException;

public class FileFixedFieldMetaImpl extends FixedFieldMetaImpl implements FileFixedFieldMeta {

	@Override
	public void from(String name, Element fieldElement) throws CommonException {
		String strType = fieldElement.attributeValue("type");
		if (StringUtils.isBlank(strType)) {
			throw new CommonException("字段数据类型为空");
		}
		if (!EnumUtils.isValidEnum(DataType.class, strType)) {
			throw new CommonException("无效的字段数据类型'" + strType + "'");
		}
		this.name = name;
		type = EnumUtils.getEnum(DataType.class, strType);
		comment = fieldElement.attributeValue("comment", "");
		length = NumberUtils.toInt(fieldElement.attributeValue("length"));
		scale = NumberUtils.toInt(fieldElement.attributeValue("scale"));
		defaultValue = fieldElement.attributeValue("default");
		String strAllowNull = fieldElement.attributeValue("allowNull");
		allowNull = StringUtils.isNotBlank(strAllowNull) ? BooleanUtils.toBoolean(strAllowNull) : true;
		if (type == DataType.Number || type == DataType.String || type == DataType.NString) {
			if (length <= 0) {
				throw new CommonException("数据表字段在文本类型或数值类型下,长度必须大于0");
			}
			if ((type == DataType.String || type == DataType.NString) && scale != 0) {
				throw new CommonException("数据表字段在文本类型下,精度必须为0");
			}
			if (type == DataType.Number && scale < 0) {
				throw new CommonException("数据表字段在数值类型下,精度必须不能小于0");
			}
		}
		else if (type == DataType.Boolean) {
			if (length != 0) {
				throw new CommonException("数据表字段在布尔值类型下，长度必须为0");
			}
			if (scale != 0) {
				throw new CommonException("数据表字段在布尔值类型下，精度必须为0");
			}
			if (StringUtils.isNotBlank(defaultValue)) {
				defaultValue = BooleanUtils.toBoolean(defaultValue) ? "1" : "0";
			}
		}
		else if (type == DataType.Binary || type == DataType.LongBinary) {
			if (length < 0) {
				throw new CommonException("数据表字段在二进制类型下,长度必须不能小于0");
			}
			if (scale != 0) {
				throw new CommonException("数据表字段在二进制类型下，精度必须为0");
			}
		}
		else {
			if (length > 0) {
				throw new CommonException("数据表字段长度不能大于0");
			}
			if (scale > 0) {
				throw new CommonException("数据表字段精度不能大于0");
			}
		}
	}
}