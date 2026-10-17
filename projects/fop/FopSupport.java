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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

final class FopSupport {
  private static final URI BASE = URI.create("file:///fuzz/");

  static {
    // Several renderers log through java.util.logging independently of the
    // event broadcaster. One log line per input would dominate the run.
    Logger.getLogger("org.apache.fop").setLevel(Level.OFF);
    Logger.getLogger("org.apache.xmlgraphics").setLevel(Level.OFF);
  }

  /**
   * Denies every resource FOP would otherwise fetch — external graphics, fonts,
   * catalogs — so a fuzzed document cannot reach the filesystem or the network.
   */
  private static final ResourceResolver DENY = new ResourceResolver() {
    @Override
    public Resource getResource(URI uri) {
      return new Resource(InputStream.nullInputStream());
    }

    @Override
    public OutputStream getOutputStream(URI uri) {
      return OutputStream.nullOutputStream();
    }
  };

  static FopFactory factory() {
    FopFactoryBuilder builder = new FopFactoryBuilder(BASE, DENY);
    builder.setStrictFOValidation(false);
    builder.setStrictUserConfigValidation(false);
    builder.setSourceResolution(72f);
    return builder.build();
  }

  static final FopFactory FACTORY = factory();

  /**
   * JAXP's default ErrorListener prints every XML parse error to stderr, which
   * for a fuzzer is one diagnostic per malformed input. This one is silent but
   * still fails the transform on a fatal error, so the harness sees the throw.
   */
  /**
   * Xerces' own DefaultErrorHandler prints "[Fatal Error] ..." to stderr for
   * every malformed document, and the Transformer's ErrorListener does not
   * reach it — the identity transform creates its own SAX parser. Building the
   * SAXSource here lets the handler be silenced and external entities be
   * switched off, so a fuzzed FO document cannot do XXE either.
   */
  private static final org.xml.sax.ErrorHandler SILENT_SAX = new org.xml.sax.ErrorHandler() {
    @Override
    public void warning(org.xml.sax.SAXParseException exception) {
    }

    @Override
    public void error(org.xml.sax.SAXParseException exception) {
    }

    @Override
    public void fatalError(org.xml.sax.SAXParseException exception)
        throws org.xml.sax.SAXException {
      throw exception;
    }
  };

  private static final ErrorListener SILENT = new ErrorListener() {
    @Override
    public void warning(TransformerException exception) {
    }

    @Override
    public void error(TransformerException exception) {
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      throw exception;
    }
  };

  /**
   * The user agent's event listener is replaced with a no-op: FOP reports every
   * validation warning through java.util.logging by default, and one log line
   * per input is both noise and a throughput cost.
   */
  private static SAXSource source(String fo)
      throws ParserConfigurationException, org.xml.sax.SAXException {
    SAXParserFactory parsers = SAXParserFactory.newInstance();
    parsers.setNamespaceAware(true);
    parsers.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    parsers.setFeature("http://xml.org/sax/features/external-general-entities", false);
    parsers.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    org.xml.sax.XMLReader reader = parsers.newSAXParser().getXMLReader();
    reader.setErrorHandler(SILENT_SAX);
    return new SAXSource(reader, new org.xml.sax.InputSource(new StringReader(fo)));
  }

  static void render(String fo, String mimeType)
      throws FOPException, TransformerConfigurationException,
             ParserConfigurationException, org.xml.sax.SAXException,
             javax.xml.transform.TransformerException {
    FOUserAgent agent = FACTORY.newFOUserAgent();
    agent.getEventBroadcaster().addEventListener(new EventListener() {
      @Override
      public void processEvent(Event event) {
      }
    });
    Fop fop = FACTORY.newFop(mimeType, agent, OutputStream.nullOutputStream());
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setErrorListener(SILENT);
    Transformer transformer = factory.newTransformer();
    transformer.setErrorListener(SILENT);
    transformer.transform(source(fo), new SAXResult(fop.getDefaultHandler()));
  }

  private FopSupport() {
  }
}
