// Copyright 2025 ISP RAS
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

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.aztec.encoder.AztecCode;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.oned.CodaBarWriter;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;
import com.google.zxing.oned.Code93Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.oned.EAN8Writer;
import com.google.zxing.oned.ITFWriter;
import com.google.zxing.oned.UPCAWriter;
import com.google.zxing.oned.UPCEWriter;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.QRCode;

public class HighLevelEncodeFuzzer {
  private static final int ENCODERS = 6;

  private static final ErrorCorrectionLevel[] EC_LEVELS = ErrorCorrectionLevel.values();

  private static final SymbolShapeHint[] SHAPES = SymbolShapeHint.values();

  public static void main(String[] args) {
      try {
        fuzzerTestOneInput(Files.readString(Path.of(args[0])));
      } catch (IOException e) {
        return;
      }
  }

  public static void fuzzerTestOneInput(String data) {
    if (data == null || data.length() < 3) {
      return;
    }

    int encoder = data.charAt(0) % ENCODERS;
    int variant = data.charAt(1);
    String contents = data.substring(2);

    try {
      switch (encoder) {
        case 0:
          qrCode(contents, variant);
          break;
        case 1:
          dataMatrix(contents, variant);
          break;
        case 2:
          aztec(contents, variant);
          break;
        case 3:
          pdf417(contents, variant);
          break;
        default:
          oneD(contents, variant, encoder);
          break;
      }
    } catch (WriterException | IllegalArgumentException | IllegalStateException
             | UnsupportedOperationException | IndexOutOfBoundsException
             | ArithmeticException | NegativeArraySizeException e) {
      return;
    }
  }

  private static void qrCode(String contents, int variant) throws WriterException {
    Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
    hints.put(EncodeHintType.CHARACTER_SET, (variant & 1) == 0 ? "UTF-8" : "ISO-8859-1");
    if ((variant & 2) == 0) {
      hints.put(EncodeHintType.QR_COMPACT, Boolean.TRUE);
    }
    QRCode code = com.google.zxing.qrcode.encoder.Encoder.encode(
        contents, EC_LEVELS[variant % EC_LEVELS.length], hints);
    code.getMode();
    code.getVersion();
    code.getMaskPattern();
    code.getMatrix();
    code.toString();
  }

  private static void dataMatrix(String contents, int variant) {
    String encoded = com.google.zxing.datamatrix.encoder.HighLevelEncoder.encodeHighLevel(
        contents, SHAPES[variant % SHAPES.length], null, null);
    encoded.length();
  }

  private static void aztec(String contents, int variant) {
    AztecCode code = com.google.zxing.aztec.encoder.Encoder.encode(
        contents, variant % 95, com.google.zxing.aztec.encoder.Encoder.DEFAULT_AZTEC_LAYERS);
    code.isCompact();
    code.getSize();
    code.getLayers();
    code.getCodeWords();
    code.getMatrix();
  }

  private static void pdf417(String contents, int variant) throws WriterException {
    PDF417 encoder = new PDF417((variant & 1) == 0);
    encoder.generateBarcodeLogic(contents, variant % 9);
    encoder.getBarcodeMatrix();
  }

  private static void oneD(String contents, int variant, int encoder) {
    switch (variant % 9) {
      case 0:
        new Code128Writer().encode(contents);
        break;
      case 1:
        new Code39Writer().encode(contents);
        break;
      case 2:
        new Code93Writer().encode(contents);
        break;
      case 3:
        new CodaBarWriter().encode(contents);
        break;
      case 4:
        new ITFWriter().encode(contents);
        break;
      case 5:
        new EAN13Writer().encode(contents);
        break;
      case 6:
        new EAN8Writer().encode(contents);
        break;
      case 7:
        new UPCAWriter().encode(contents, com.google.zxing.BarcodeFormat.UPC_A, 100, 100);
        break;
      default:
        new UPCEWriter().encode(contents);
        break;
    }
  }
}
