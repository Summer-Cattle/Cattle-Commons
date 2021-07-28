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
package io.github.summercattle.commons.db.object.internal.impl;

import io.github.summercattle.commons.db.field.FieldTypes;
import io.github.summercattle.commons.db.object.internal.RowLineSet;
import io.github.summercattle.commons.db.object.internal.RowStatus;
import io.github.summercattle.commons.exception.CommonException;

public class RowLineSetImpl extends RowLineImpl implements RowLineSet {

	private Object[] initValues;

	private RowStatus initStatus;

	private int[] fieldsType;

	private boolean isOperate = false;

	private String[] fieldNames;

	private String name;

	public RowLineSetImpl(RowStatus rowStatus, String name, String[] fieldNames, int[] fieldsType, Object[] values) throws CommonException {
		super(values);
		initStatus = rowStatus;
		initValues = values.clone();
		this.name = name;
		this.fieldNames = fieldNames;
		this.fieldsType = fieldsType;
	}

	@Override
	public void set(int index, Object value) throws CommonException {
		if (!isOperate) {
			if (!FieldTypes.getType(name, fieldNames[index], fieldsType[index]).nullSafeIsEqual(values[index], value)) {
				isOperate = true;
			}
		}
		values[index] = value;
	}

	@Override
	public RowStatus getStatus() throws CommonException {
		if (initStatus == RowStatus.Add) {
			return RowStatus.Add;
		}
		if (isOperate && initStatus == RowStatus.Init) {
			boolean isModify = false;
			for (int i = 0; i < initValues.length; i++) {
				if (!FieldTypes.getType(name, fieldNames[i], fieldsType[i]).nullSafeIsEqual(initValues[i], values[i])) {
					isModify = true;
					break;
				}
			}
			if (isModify) {
				return RowStatus.Modify;
			}
		}
		return RowStatus.Init;
	}

	@Override
	public Object[] getValues() {
		return values;
	}
}