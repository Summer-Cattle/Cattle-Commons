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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import io.github.summercattle.commons.exception.CommonException;
import io.github.summercattle.commons.utils.exception.ExceptionWrapUtils;

public class QRUtils {

	public static void createQR(OutputStream outputStream, String content, int width, int height, int margin, Charset charset,
			ErrorCorrectionLevel errorCorrectionLevel, Color front, Color background, String format) throws CommonException {
		MatrixToImageConfig config = new MatrixToImageConfig(null == front ? Color.BLACK.getRGB() : front.getRGB(),
				null == background ? Color.WHITE.getRGB() : background.getRGB());
		BitMatrix matrix = createQR(content, width, height, margin, charset, errorCorrectionLevel);
		try {
			MatrixToImageWriter.writeToStream(matrix, StringUtils.isBlank(format) ? "png" : format, outputStream, config);
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	public static void createLogoQR(OutputStream outputStream, String content, InputStream logoInputStream, int width, int height, int margin,
			Charset charset, ErrorCorrectionLevel errorCorrectionLevel, Color front, Color background, String format) throws CommonException {
		MatrixToImageConfig config = new MatrixToImageConfig(null == front ? Color.BLACK.getRGB() : front.getRGB(),
				null == background ? Color.WHITE.getRGB() : background.getRGB());
		BitMatrix matrix = createQR(content, width, height, margin, charset, errorCorrectionLevel);
		int qrWidth = matrix.getWidth();
		int qrHeight = matrix.getHeight();
		BufferedImage qrImage = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < qrWidth; x++) {
			for (int y = 0; y < qrHeight; y++) {
				qrImage.setRGB(x, y, (matrix.get(x, y) ? config.getPixelOnColor() : config.getPixelOffColor()));
			}
		}
		try {
			BufferedImage logoImage = ImageIO.read(logoInputStream);
			Graphics2D g2 = qrImage.createGraphics();
			int matrixWidth = qrImage.getWidth();
			int matrixHeigh = qrImage.getHeight();
			//绘制
			g2.drawImage(logoImage, matrixWidth / 5 * 2, matrixHeigh / 5 * 2, matrixWidth / 5, matrixHeigh / 5, null);
			BasicStroke stroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			// 设置笔画对象
			g2.setStroke(stroke);
			//指定弧度的圆角矩形
			RoundRectangle2D.Float round = new RoundRectangle2D.Float(matrixWidth / 5 * 2, matrixHeigh / 5 * 2, matrixWidth / 5, matrixHeigh / 5, 20,
					20);
			g2.setColor(Color.white);
			// 绘制圆弧矩形
			g2.draw(round);
			//设置logo 有一道灰色边框
			BasicStroke stroke2 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			// 设置笔画对象
			g2.setStroke(stroke2);
			RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(matrixWidth / 5 * 2 + 2, matrixHeigh / 5 * 2 + 2, matrixWidth / 5 - 4,
					matrixHeigh / 5 - 4, 20, 20);
			g2.setColor(new Color(128, 128, 128));
			// 绘制圆弧矩形
			g2.draw(round2);
			g2.dispose();
			qrImage.flush();
			if (!ImageIO.write(qrImage, StringUtils.isBlank(format) ? "png" : format, outputStream)) {
				throw new IOException("Could not write an image of format " + format);
			}
		}
		catch (IOException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}

	private static BitMatrix createQR(String content, int width, int height, int margin, Charset charset, ErrorCorrectionLevel errorCorrectionLevel)
			throws CommonException {
		try {
			Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
			hints.put(EncodeHintType.CHARACTER_SET, null == charset ? CharacterSetECI.UTF8 : charset);
			hints.put(EncodeHintType.ERROR_CORRECTION, null == errorCorrectionLevel ? ErrorCorrectionLevel.H : errorCorrectionLevel);
			hints.put(EncodeHintType.MARGIN, margin);
			return new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		}
		catch (WriterException e) {
			throw ExceptionWrapUtils.wrap(e);
		}
	}
}