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
package com.gitlab.summercattle.commons.utils.auxiliary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gitlab.summercattle.commons.exception.CommonException;
import com.gitlab.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class Dom4jUtils {

	private static SAXReader getSAXReader() {
		SAXReader reader = new SAXReader();
		try {
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
			reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		}
		catch (SAXException e) {
		}
		return reader;
	}

	public static Document getDocument(InputStream is) throws CommonException {
		try {
			SAXReader reader = getSAXReader();
			return reader.read(is);
		}
		catch (DocumentException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static Document getDocument(URL url) throws CommonException {
		SAXReader reader = getSAXReader();
		try (InputStream is = url.openStream()) {
			return reader.read(is);
		}
		catch (IOException | DocumentException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static Document getDocument(String xml) throws CommonException {
		SAXReader reader = getSAXReader();
		String encoding = getEncoding(xml);
		InputSource source = new InputSource(new StringReader(xml));
		source.setEncoding(encoding);
		try {
			Document result = reader.read(source);
			if (result.getXMLEncoding() == null) {
				result.setXMLEncoding(encoding);
			}
			return result;
		}
		catch (DocumentException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private static String getEncoding(String text) {
		String result = null;
		String xml = text.trim();
		if (xml.startsWith("<?xml")) {
			int end = xml.indexOf("?>");
			String sub = xml.substring(0, end);
			StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if ("encoding".equals(token)) {
					if (tokens.hasMoreTokens()) {
						result = tokens.nextToken();
					}
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 根据Document对象生成不带回车的Xml字符串
	 * @param document document对象
	 * @param enc 字符集
	 * @return 不带回车的Xml字符串
	 * @throws CommonException 异常
	 */
	public static String asXmlWithoutPretty(Document document, String enc) throws CommonException {
		return asXml(document, enc, false);
	}

	/**
	 * 根据Document对象生成带回车的Xml字符串
	 * @param document document对象
	 * @param enc 字符集
	 * @return 带回车的Xml字符串
	 * @throws CommonException 异常
	 */
	public static String asXmlWithPretty(Document document, String enc) throws CommonException {
		return asXml(document, enc, true);
	}

	/**
	 * 根据Document对象生成Xml字符串
	 * @param document document对象
	 * @param enc 字符集
	 * @param isPretty 是否带有漂亮的格式
	 * @return Xml字符串
	 * @throws CommonException 异常
	 */
	private static String asXml(Document document, String enc, boolean isPretty) throws CommonException {
		XMLWriter writer = null;
		ByteArrayOutputStream bout = null;
		try {
			String xml = "";
			// 美化格式
			OutputFormat format = null;
			if (isPretty) {
				format = OutputFormat.createPrettyPrint();
			}
			else {
				format = OutputFormat.createCompactFormat();
			}
			format.setEncoding(enc);
			format.setTrimText(false);
			format.setExpandEmptyElements(false);
			format.setNewLineAfterDeclaration(false);
			bout = new ByteArrayOutputStream();
			writer = new XMLWriter(bout, format);
			if (document != null) {
				writer.write(document);
				xml = bout.toString(enc);
			}
			return xml;
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (bout != null) {
					bout.close();
				}
			}
			catch (IOException e) {
			}
		}
	}

	/**
	 * 根据Element对象生成不带回车的Xml字符串
	 * @param element element对象
	 * @param enc 字符集
	 * @return 不带回车的Xml字符串
	 * @throws CommonException 异常
	 */
	public static String asXmlWithoutPretty(Element element, String enc) throws CommonException {
		return asXml(element, enc, false);
	}

	/**
	 * 根据Element对象生成带回车的Xml字符串
	 * @param element element对象
	 * @param enc 字符集
	 * @return 带回车的Xml字符串
	 * @throws CommonException 异常
	 */
	public static String asXmlWithPretty(Element element, String enc) throws CommonException {
		return asXml(element, enc, true);
	}

	/**
	 * 根据Element对象生成Xml字符串
	 * @param element element对象
	 * @param enc 字符集
	 * @param isPretty 是否带有漂亮的格式
	 * @return Xml字符串
	 * @throws CommonException 异常
	 */
	private static String asXml(Element element, String enc, boolean isPretty) throws CommonException {
		XMLWriter writer = null;
		ByteArrayOutputStream bout = null;
		try {
			String xml = "";
			// 美化格式
			OutputFormat format = null;
			if (isPretty) {
				format = OutputFormat.createPrettyPrint();
			}
			else {
				format = OutputFormat.createCompactFormat();
			}
			format.setEncoding(enc);
			format.setTrimText(false);
			format.setExpandEmptyElements(false);
			format.setNewLineAfterDeclaration(false);
			bout = new ByteArrayOutputStream();
			writer = new XMLWriter(bout, format);
			if (element != null) {
				writer.write(element);
				xml = bout.toString(enc);
			}
			return xml;
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (bout != null) {
					bout.close();
				}
			}
			catch (IOException e) {
			}
		}
	}
}