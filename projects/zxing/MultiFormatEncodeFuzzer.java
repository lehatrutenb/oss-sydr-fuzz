// Copyright 2021 Google LLC
// Modifications copyright (C) 2025 ISP RAS
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.aztec.AztecReader;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.oned.CodaBarReader;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.oned.Code39Reader;
import com.google.zxing.oned.Code93Reader;
import com.google.zxing.oned.EAN13Reader;
import com.google.zxing.oned.EAN8Reader;
import com.google.zxing.oned.ITFReader;
import com.google.zxing.oned.UPCAReader;
import com.google.zxing.oned.UPCEReader;
import com.google.zxing.pdf417.PDF417Reader;
import com.google.zxing.qrcode.QRCodeReader;

public class MultiFormatEncodeFuzzer {
  private static final int HEADER = 3;

  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.length() <= HEADER) {
      return;
    }

    BarcodeFormat[] formats = BarcodeFormat.values();
    BarcodeFormat format = formats[data.charAt(0) % formats.length];
    int width = 100 + data.charAt(1) % 101;
    int height = 100 + data.charAt(2) % 101;
    String contents = data.substring(HEADER);

    Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    hints.put(EncodeHintType.MARGIN, Integer.valueOf(data.charAt(0) % 8));
    hints.put(EncodeHintType.CHARACTER_SET, (data.charAt(1) & 1) == 0 ? "UTF-8" : "ISO-8859-1");

    BitMatrix matrix;
    try {
      matrix = new MultiFormatWriter().encode(contents, format, width, height, hints);
    } catch (WriterException | IllegalArgumentException | ArithmeticException e) {
      return;
    }
    matrix.toString();

    try {
      Result result = getReader(format).decode(new BinaryBitmap(new TrivialBinarizer(matrix)));
      result.getText();
      result.getResultMetadata();
    } catch (NotFoundException | ChecksumException | FormatException
             | IllegalArgumentException e) {
      return;
    }
  }

  private static Reader getReader(BarcodeFormat format) {
    switch (format) {
      case EAN_8:
        return new EAN8Reader();
      case UPC_E:
        return new UPCEReader();
      case EAN_13:
        return new EAN13Reader();
      case UPC_A:
        return new UPCAReader();
      case QR_CODE:
        return new QRCodeReader();
      case CODE_39:
        return new Code39Reader();
      case CODE_93:
        return new Code93Reader();
      case CODE_128:
        return new Code128Reader();
      case ITF:
        return new ITFReader();
      case PDF_417:
        return new PDF417Reader();
      case CODABAR:
        return new CodaBarReader();
      case DATA_MATRIX:
        return new DataMatrixReader();
      case AZTEC:
        return new AztecReader();
      default:
        throw new IllegalArgumentException("No encoder available for format " + format);
    }
  }

  private static final class TrivialBinarizer extends Binarizer {
    private final BitMatrix matrix;

    TrivialBinarizer(BitMatrix matrix) {
      super(new TrivialLuminanceSource(matrix));
      this.matrix = matrix;
    }

    @Override
    public BitArray getBlackRow(int y, BitArray row) throws NotFoundException {
      return matrix.getRow(y, row);
    }

    @Override
    public BitMatrix getBlackMatrix() throws NotFoundException {
      return matrix;
    }

    @Override
    public Binarizer createBinarizer(LuminanceSource source) {
      return new TrivialBinarizer(matrix);
    }
  }

  private static final class TrivialLuminanceSource extends LuminanceSource {
    private final BitMatrix matrix;

    TrivialLuminanceSource(BitMatrix matrix) {
      super(matrix.getWidth(), matrix.getHeight());
      this.matrix = matrix;
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
      if (row == null || row.length != matrix.getWidth()) {
        row = new byte[matrix.getWidth()];
      }
      BitArray bitRow = matrix.getRow(y, null);
      for (int i = 0; i < matrix.getWidth(); i++) {
        row[i] = bitRow.get(i) ? 0 : (byte) 255;
      }
      return row;
    }

    @Override
    public byte[] getMatrix() {
      byte[] bytes = new byte[matrix.getWidth() * matrix.getHeight()];
      for (int x = 0; x < matrix.getWidth(); x++) {
        for (int y = 0; y < matrix.getHeight(); y++) {
          if (!matrix.get(x, y)) {
            bytes[x + y * matrix.getWidth()] = (byte) 255;
          }
        }
      }
      return bytes;
    }
  }
}
