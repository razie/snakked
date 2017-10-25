/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XObject;

/**
 * XML utilities to parse/use XML documents - the basic idea is using xpath most
 * of the time to access the contents of the xml doc
 * 
 * $
 * 
 * @author razvanc99
 * 
 */
public class RiXmlUtils {
   /**
    * read an XML document, parse and return the DOM
    * 
    * @param url
    *           the url. If you want a file use (new File(pathToUse +
    *           fname)).toURL()
    */
   public static Document readXml(InputStream is, String id) throws SomeRtException {
      Document doc = null;
      try {
         InputSource xmlInp = new InputSource(is);
         xmlInp.setSystemId(id);
         DOMParser parser = new DOMParser();
         parser.parse(xmlInp);
         doc = parser.getDocument();
         Element root = doc.getDocumentElement();
         root.normalize();
         return doc;
      } catch (java.net.MalformedURLException mue) {
         throw new SomeRtException("Can't read XML file: " + id, mue);
      } catch (java.io.IOException ioe) {
         throw new SomeRtException("Can't read XML file: " + id, ioe);
      } catch (SAXParseException spe) {
         throw new SomeRtException("Error parsing XML file: " + id + "line: " + spe.getLineNumber()
               + ", uri: " + spe.getSystemId(), spe);
      } catch (SAXException se) {
         throw new SomeRtException("Error parsing XML file: " + id, se);
      } catch (Exception pce) {
         throw new SomeRtException("Error parsing XML file: " + id, pce);
      }
   }

   /**
    * read an XML document, parse and return the DOM
    * 
    * @param url
    *           the url. If you want a file use (new File(pathToUse +
    *           fname)).toURL()
    */
   public static Document readXml(URL url) throws SomeRtException {
      try {
         return readXml(url.openStream(), url.toExternalForm());
      } catch (java.net.MalformedURLException mue) {
         throw new SomeRtException("Can't read XML file: " + url.toString(), mue);
      } catch (java.io.IOException ioe) {
         throw new SomeRtException("Can't read XML file: " + url.toString(), ioe);
      } catch (Exception pce) {
         throw new SomeRtException("Error parsing XML file: " + url.toString(), pce);
      }
   }

   /**
    * get all children of given type or empty list
    * 
    * @param element
    *           the element to search in
    * @param s
    *           type/tag of children
    * @return list of children of given type or empty, never null
    */
   public static List<Element> getElementsByName(Element elem, String s) {
      List<Element> ret = new ArrayList<Element>();
      for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
         if (child instanceof Element) {
            if (child.getNodeName().equals(s)) {
               ret.add((Element) child);
            }
         }
      }
      return ret;
   }

   private static final class MyPrefixResolver implements PrefixResolver {

      private HashMap<String, String> prefixes;

      MyPrefixResolver(Map<String, String> p) {
         prefixes = new HashMap<String, String>();
         prefixes.put("xi", "http://www.w3.org/2001/XInclude");
         if (p != null) {
            prefixes.putAll(p);
         }
      }

      public String getBaseIdentifier() {
         return null;
      }

      public String getNamespaceForPrefix(String prefix) {
         return prefixes.get(prefix);
      }

      public String getNamespaceForPrefix(String prefix, Node context) {
         return getNamespaceForPrefix(prefix);
      }

      public boolean handlesNullPrefixes() {
         return false;
      }
   }

   private static XObject getValue(Node node, String xPath, Map<String, String> prefixes) {

      MyPrefixResolver prefixResolver = new MyPrefixResolver(prefixes);
      try {
         return XPathAPI.eval(node, xPath, prefixResolver);
      } catch (TransformerException e) {
         throw new SomeRtException("?", e);
      }
   }

   public static String getStringValue(Node node, String xPath, Map<String, String> prefixes) {
      XObject xo = getValue(node, xPath, prefixes);
      return xo == null ? null : xo.str();
   }

   public static List<Element> getNodeList(Node node, String xPath, Map<String, String> prefixes) {
      XObject xo = getValue(node, xPath, prefixes);
      try {
         if (xo == null) {
            return null;
         } else {
            List<Element> ret = new ArrayList<Element>();
            for (int i = 0; i < xo.nodelist().getLength(); i++) {
               Node child = xo.nodelist().item(i);
               if (child instanceof Element) {
                  ret.add((Element) child);
               }
            }
            return ret;
         }
      } catch (TransformerException e) {
         throw new SomeRtException("?", e);
      }
   }

   public static Element getNode(Node node, String xPath, Map<String, String> prefixes) {
      List<Element> nl = getNodeList(node, xPath, prefixes);
      if (nl != null) {
         if (nl.size() > 1)
            throw new SomeRtException("More than 1 node found for xPath " + xPath);
         if (nl.size() == 1)
            return nl.get(0);
      }
      return null;
   }

   public static Document createDocument() {
      Document doc = null;
      try {
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
         doc = docBuilder.newDocument();

         Element element = doc.getDocumentElement();
         // do i actually need to do this here? why?
         if (element != null) 
            element.normalize();
      } catch (Throwable t) {
         t.printStackTrace();
      }
      
      return doc;
   }

   /** if aFile is given, will write to file, otherwise return as String */
   public static String writeDoc(Document doc, File aFile) {
      String s = null;
      try {
         OutputFormat format = new OutputFormat("XML", "UTF-8", true);
         if (aFile != null) {
            FileOutputStream out = new FileOutputStream(aFile);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(doc);
            out.close();
            out.flush();
         } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(doc);
            out.close();
            out.flush();
            s = out.toString();
         }
      } catch (Exception e) {
         throw new RuntimeException("Exception while writing new XML document: \n", e);
      }
      return s;
   }

   public static String getOptNodeVal(Element elem) {
      String ret = null;
      if (elem != null) {
         for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {
               ret = ret == null ? child.getNodeValue() : ret + child.getNodeValue();
            }
         }
      }
      return ret;
   }

}
