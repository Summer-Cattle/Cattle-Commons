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
package io.github.summercattle.commons.utils.auxiliary;

import java.math.BigInteger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.RandomStringGenerator;

public class StringUtils {

	public static String replaceOnce(String template, String placeholder, String replacement) {
		if (template == null) {
			return null;
		}
		int loc = template.indexOf(placeholder);
		if (loc < 0) {
			return template;
		}
		return template.substring(0, loc) + replacement + template.substring(loc + placeholder.length());
	}

	public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
		if ((null == cs1 && null != cs2 && cs2.length() == 0) || (null != cs1 && cs1.length() == 0 && null == cs2)) {
			return true;
		}
		else {
			return org.apache.commons.lang3.StringUtils.equals(cs1, cs2);
		}
	}

	public static String replace(String template, String placeholder, String replacement) {
		return replace(template, placeholder, replacement, false);
	}

	public static String replace(String template, String placeholder, String replacement, boolean wholeWords) {
		int loc = template.indexOf(placeholder);
		if (loc < 0) {
			return template;
		}
		boolean actuallyReplace = !wholeWords || ((loc + placeholder.length()) == template.length())
				|| !Character.isJavaIdentifierPart(template.charAt(loc + placeholder.length()));
		String actualReplacement = actuallyReplace ? replacement : placeholder;
		return new StringBuffer(template.substring(0, loc)).append(actualReplacement)
				.append(replace(template.substring(loc + placeholder.length()), placeholder, replacement, wholeWords)).toString();
	}

	public static String getRandomString(int length) {
		return getRandomString(false, length);
	}

	public static String getRandomString(boolean onlyNumber, int length) {
		char[][] pairs = { { 'a', 'z' }, { 'A', 'Z' }, { '0', '9' } };
		RandomStringGenerator generator = onlyNumber ? new RandomStringGenerator.Builder().withinRange('0', '9').build()
				: new RandomStringGenerator.Builder().withinRange(pairs).build();
		return generator.generate(length);
	}

	/**
	 * 字符串是否匹配(通配符匹配，?代表任意一位 *代表0-N位)
	 * @param s 被匹配字符串
	 * @param p 匹配字符串
	 * @return 是否匹配
	 */
	public static boolean isMatch(String s, String p) {
		int idxs = 0;
		int idxp = 0;
		int idxstar = -1;
		int idxmatch = 0;
		while (idxs < s.length()) {
			// 当两个指针指向完全相同的字符时，或者p中遇到的是?时
			if (idxp < p.length() && (s.charAt(idxs) == p.charAt(idxp) || p.charAt(idxp) == '?')) {
				idxp++;
				idxs++;
				// 如果字符不同也没有?，但在p中遇到是*时，我们记录下*的位置，但不改变s的指针
			}
			else if (idxp < p.length() && p.charAt(idxp) == '*') {
				idxstar = idxp;
				idxp++;
				//遇到*后，我们用idxmatch来记录*匹配到的s字符串的位置，和不用*匹配到的s字符串位置相区分
				idxmatch = idxs;
				// 如果字符不同也没有?，p指向的也不是*，但之前已经遇到*的话，我们可以从idxmatch继续匹配任意字符
			}
			else if (idxstar != -1) {
				// 用上一个*来匹配，那我们p的指针也应该退回至上一个*的后面
				idxp = idxstar + 1;
				// 用*匹配到的位置递增
				idxmatch++;
				// s的指针退回至用*匹配到位置
				idxs = idxmatch;
			}
			else {
				return false;
			}
		}
		// 因为1个*能匹配无限序列，如果p末尾有多个*，我们都要跳过
		while (idxp < p.length() && p.charAt(idxp) == '*') {
			idxp++;
		}
		// 如果p匹配完了，说明匹配成功
		return idxp == p.length();
	}

	public static String getHashName(String str) {
		byte[] bytes = DigestUtils.md5(str);
		BigInteger bigInteger = new BigInteger(1, bytes);
		return bigInteger.toString(35).toUpperCase();
	}
}