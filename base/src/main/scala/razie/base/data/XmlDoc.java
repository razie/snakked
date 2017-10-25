/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.base.data;

import java.io.File;
import java.io.StringBufferInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.razie.pub.base.log.Log;

/**
 * represents an xml document - generally for configuration. For dynamic data, use the XmlDb class.
 * 
 * <p>
 * This is the central point for xml access, usually with xpath.
 * 
 * <p>
 * Registry: when decoupling loading xml docs from their actual use in code
 * 
 * <p>Sample: d = new XmlDoc().load ("mydoc", new URL("http://...."))
 * <p>If the doc is in the classpath, get an URL like so:
 * 
 * <code>
 * new XmlDoc().load("docname", singleton.getClass().getResource(DFLT_CATALOG));
 * </code>
 * 
 * <p>
 * If the document's root node includes an attribute "razieReloadMillis" then the document will be 
 * reloaded if touched.
 * 
 * <p>Usage: each configuration document is normally registered in the Registry like so:
 * 
 * <code>
 * XmlDoc.Reg.docAdd ("userConfig", new XmlDoc().load("userConfig", new URL("file://........")))
 * </code>
 *
 * <p>Then, in your code, use it like so: 
 * <code>
 *  for (Element e : Reg.doc(MediaConfig.MEDIA_CONFIG).xpl(
 *               "/config/storage/host[@name='" + Agents.me().name + "']/media")) {
 *          location = e.getAttribute("localdir");
 *          ...
 * </code>
 * 
 * <p>Do me a favor and use only the xpl/xpe/xpa accessors, please...
 *
 * @author razvanc
 */
public class XmlDoc {
    protected Document            document;
    public URL                    myUrl;
    protected String              name;
    protected Element             root;
    protected Map<String, String> prefixes         = null;    // lazy
    public long                   fileLastModified = -1;
    private long                  lastChecked      = 0;
    // TODO use a reasonable interval, make configurable - maybe per db
    public long                   reloadMilis      = 1000 * 3;

    /** the root element - i'm getting bored typing */
    public Element e() {
        return root;
    }

    /**
     * add prefix to be used in resolving xpath in this doc...if you use multiple schemas, pay
     * attention to this
     */
    public void addPrefix(String s, String d) {
        if (prefixes == null)
            prefixes = new HashMap<String, String>();
        prefixes.put(s, d);
    }

    private static File fileFromUrl(URL url) {
       File f;
       try {
          f = new File(url.toURI());
       } catch (URISyntaxException e) {
          f = new File(url.toExternalForm());
       }

       return f;
    }

    protected void checkFile() {
        if (this.reloadMilis > 0 && System.currentTimeMillis() - this.lastChecked >= this.reloadMilis) {
            File f = fileFromUrl(this.myUrl);
            long ft = f != null ? f.lastModified() : -1;
            if (ft != this.fileLastModified) {
                Log.logThis ("RELOAD_UPDATED_XMLDB name="+name);
                load(name, myUrl);
            }
        }
    }

    public XmlDoc load(String name, URL url) {
       Log.logThis("XmlDoc:loading from URL=" + url);
        this.myUrl = url;
        this.name = name;
        this.document = RiXmlUtils.readXml(url);
        this.root = this.document.getDocumentElement();
        if (this.root != null && this.root.hasAttribute("razieReloadMillis")) {
            this.reloadMilis = Long.parseLong(this.root.getAttribute("razieReloadMillis"));
        }

        try {
            File f = fileFromUrl(url);
            fileLastModified = f != null ? f.lastModified() : -1;
            this.lastChecked = System.currentTimeMillis();
        } catch (Exception e) {
           Log.logThis("XMLDOC won't be refreshed automatically: Can't get datetime for file URL=" + url);
            this.reloadMilis = 0;
        }

        return this;
    }

    protected XmlDoc load(String name, Document d) {
        this.name = name;
        this.document = d;
        this.root = d.getDocumentElement();
        return this;
    }

    /**
     * return a list of all elements in specified path, just their "name" attribute
     * 
     * @return never null
     */
    public List<String> list(String path) {
        List<String> ret = new ArrayList<String>();
        for (Element e : RiXmlUtils.getNodeList(root, path, null)) {
            ret.add(e.getAttribute("name"));
        }
        return ret;
    }

    /**
     * return a list of all elements in specified path
     * 
     * @return never null
     */
    public static List<Element> xpl(Element node, String path) {
        return RiXmlUtils.getNodeList(node, path, null);
    }

    /**
     * return a list of all elements in specified path
     * 
     * @return never null
     */
    public static List<Element> listEntities(Element node) {
        List<Element> list = new ArrayList<Element>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) {
                list.add((Element) n);
            }
        }
        return list;
    }

    /**
     * get a specific element, by "name"
     * 
     * @param path identifies the xpath
     * @name identifies the name attribute of the element - could also be part of xpath instead
     * @return never null
     */
    public static Element xpe(Element e, String path) {
        return (Element) RiXmlUtils.getNode(e, path, null);
    }

    /**
     * i.e. "/config/mutant/@someattribute"
     * 
     * @param path identifies the xpath
     * @name identifies the name attribute of the element - could also be part of xpath instead
     * @return never null
     */
    public static String xpa(Element e, String path) {
        return RiXmlUtils.getStringValue(e, path, null);
    }

    /**
     * get the balue of a node "name"
     * 
     * @param path identifies the xpath
     * @return never null
     */
    public String getOptNodeVal(String path) {
        Element e = xpe (path);
        return e == null ? "" : RiXmlUtils.getOptNodeVal(e);
    }

    /**
     * return a list of all elements in specified path
     * 
     * @return never null
     */
    public List<Element> xpl(String path) {
        return RiXmlUtils.getNodeList(root, path, prefixes);
    }

    /**
     * get a specific element, by "name"
     * 
     * @param path identifies the xpath
     * @name identifies the name attribute of the element - could also be part of xpath instead
     * @return never null
     */
    public Element xpe(String path) {
        return (Element) RiXmlUtils.getNode(root, path, prefixes);
    }

    /**
     * i.e. "/config/mutant/@someattribute"
     * 
     * @param path identifies the xpath
     * @name identifies the name attribute of the element - could also be part of xpath instead
     * @return never null
     */
    public String xpa(String path) {
        return RiXmlUtils.getStringValue(root, path, null);
    }

    public Document getDocument() {
        return this.document;
    }

    public static XmlDoc createFromString(String name, String str) {
        XmlDoc doc = new XmlDoc();
        Document d = RiXmlUtils.readXml(new StringBufferInputStream(str), "");
        doc.load(name, d);
        return doc;
    }

    
    /** see the registry - register factories that load specific documents */
    public static interface IXmlDocFactory {
        public XmlDoc make();
    }

    /**
     * registry for all static (config) xml documents. You can register a document after loading or
     * a factory responsible for loading a document
     */
    public static class Reg {
        private static Map<String, IXmlDocFactory> factories = new HashMap<String, IXmlDocFactory>();
        protected static Map<String, XmlDoc>       allDocs   = new HashMap<String, XmlDoc>();

        /** TEMP */
        public static void docAdd(String s, XmlDoc d) {
            allDocs.put(s, d);
        }

        /** register a factory - this will load the document with the specified name when needed */
        public static void registerFactory(String s, XmlDoc.IXmlDocFactory factory) {
            factories.put(s, factory);
        }

        /** TEMP */
        public static XmlDoc doc(String s) {
            XmlDoc d = allDocs.get(s);
            if (d != null)
                d.checkFile();
            else if (factories.containsKey(s)) {
                d = factories.get(s).make();
                docAdd(s, d);
            }
            return allDocs.get(s);
        }
    }
}
