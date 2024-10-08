/**
 * Razvan's public code. 
 * Copyright 2008 based on Apache license (share alike) see LICENSE.txt for details.
 */
package com.razie.pub.comms;

import com.razie.pub.base.data.ByteArray;
import com.razie.pub.base.log.Log;
import razie.base.AttrAccess;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * communications utils - read streams, sockets and URLs
 */
public class Comms {
  public final static long MAX_BUF_SIZE=8L * 1024L * 1024L; // 8m bytes per request... more than enough?
  public final static int MAX_CONNECT_TIMEOUT=500; // fast conn timeout
  public final static int MAX_TIMEOUT=30000; // 15 sec timeout

  static Log logger = Log.factory.create(Comms.class);

  public static boolean trustAllInitialized = false;

  public static int getResponseCode(URLConnection uc) {
    try {
      return ((HttpURLConnection) uc).getResponseCode();
    } catch (IOException e) {
      // this is what the getRespCdode above returns by default anyways
      return -1;
    }
  }

  /** handle and throw for basic errors */
  public static BiConsumer<URLConnection, Integer> dfltHandler = (URLConnection uc, Integer tout) -> {
    String resCode = uc.getHeaderField(0);
    String code = null;

    // find the actual code
    if (resCode != null) {
      code = resCode.split(" ")[1];
    }

    if (code != null && code.trim().equals("302")) { // 204 is No Content
      String loc = uc.getHeaderField("Location");
      String msg =
          "["+resCode+"] Redirect 302 from url " + uc.getURL().toString() +
              ", to=" + loc;
      logger.trace(3, msg + loc);
      throw new CommRtException(msg, uc, getResponseCode(uc))
          .withDetails(loc)
          .withLocation302(loc);
    } else {
      boolean isok = code != null && code.trim().matches("2[0-9][0-9]"); // 2xx is ok

      if (code == null || !isok) { // 204 is No Content, 201 created
        int respCode = getResponseCode(uc);
        String content = readStream(((HttpURLConnection) uc).getErrorStream());
        final int TAKE = 25;
        String ec = content != null && content.length() > TAKE ? content.substring(0,TAKE) : ""+content;

        // if not max timeout, the caller specified a time out so if
        String timedout = tout != MAX_TIMEOUT ? "(maybe timeout) " : " ";

        String msg =
            "[" + resCode + "] Could not fetch data from url " + timedout + uc.getURL().toString() +
                ", resCode=" + resCode +
                ", code=" + code +
                ", responseCode=" + respCode+
                ", errStream=" + ec;
        String fec = content != null && content.length() > 2000 ? content.substring(0,2000) : ""+content;
        logger.warn("Comms Exception: " + msg + "big error response=" + fec);
        throw new CommRtException(msg, uc, getResponseCode(uc)).withDetails(content);
      }
    }
  };

  /**
   * hack to allow PATCH - see https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch
   *
   * @param methods
   */
  private static void allowMethods(String... methods) {
    try {
      Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

      methodsField.setAccessible(true);

      String[] oldMethods = (String[]) methodsField.get(null);
      Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
      methodsSet.addAll(Arrays.asList(methods));
      String[] newMethods = methodsSet.toArray(new String[0]);

      methodsField.set(null/*static field*/, newMethods);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Stream the response of a URL.
   *
   * @param verb - pass only if it's PATCH
   * @param url can be local or remote
   * @return a string containing the text read from the URL. can be the result of a servlet, a web
   *         page or the contents of a local file. It's null if i couldn't read the file.
   */
  public static URLConnection xpoststreamUrl2A(String verb, String url, AttrAccess httpArgs, String content) {
    HttpURLConnection uc = null;

    int cout = MAX_CONNECT_TIMEOUT;
    int tout = MAX_TIMEOUT;
    for (String a : httpArgs.getPopulatedAttr()) {
      if(a.equals("snakkHttpOptions.readTimeout"))
        tout = Integer.parseInt(httpArgs.sa(a));
      if (a.equals("snakkHttpOptions.connectTimeout"))
        cout = Integer.parseInt(httpArgs.sa(a));
    }

    try {
      initTrustAll();
      InputStream in = null;
      uc = (HttpURLConnection) (new URL(url)).openConnection();
      uc.setConnectTimeout(cout);
      uc.setReadTimeout(tout);

      if(
          "PATCH".equals(verb) ||
          "PUT".equals(verb) ||
          "DELETE".equals(verb) ||

              // let's allow these too, they're in the standard
          "TRACE".equals(verb) ||
          "HEAD".equals(verb) ||
          "OPTIONS".equals(verb)
      ) {
        allowMethods(verb);
        uc.setRequestMethod(verb);
      }

      // see http://www.exampledepot.com/egs/java.net/Post.html
      uc.setDoOutput(true);

      for (String a : httpArgs.getPopulatedAttr()) {
        if(!a.startsWith("snakkHttpOptions."))
          uc.setRequestProperty(a, httpArgs.sa(a));
      }

      OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
      String dataToWrite = "";
      if (content != null && content.length() > 0) {
        dataToWrite = content;
      }
      logger.trace(3, "POSTING: "+verb + " - " + dataToWrite);
      logger.log("POSTING: "+verb + " - " + dataToWrite.length() + " bytes to " + url + " cout="+cout + " tout=" + tout);
      wr.write(dataToWrite);
      wr.flush();

      logger.trace(3, "hdr: ", uc.getHeaderFields());
      dfltHandler.accept(uc, tout);
      return uc;
    } catch (MalformedURLException e) {
      RuntimeException iex = new IllegalArgumentException();
      iex.initCause(e);
      throw iex;
    } catch (CommRtException re) {
      // may come from handling the exception - leave this
      throw re;
    } catch (FileNotFoundException nef) { // 404 causes this - nuts !!
      CommRtException rte = new CommRtException("NotFound exception for url=" + url, uc, getResponseCode(uc));
      rte.initCause(nef);
      throw rte;
    } catch (IOException io) { // 400 causes this - nuts !!
      CommRtException rte = new CommRtException("IO exception for url=" + url, uc, getResponseCode(uc));
      rte.initCause(io);
      throw rte;
    } catch (Exception e1) {
      // server/node down
      throw new RuntimeException("Connection exception for url=" + url + "\n"+e1.getMessage(), e1);
    }
  }

  /**
   * Stream the response of a URL.
   *
   * @param verb - pass only if it's PATCH
   * @param url can be local or remote
   * @return a string containing the text read from the URL. can be the result of a servlet, a web
   *         page or the contents of a local file. It's null if i couldn't read the file.
   */
  public static InputStream xpoststreamUrl2(String verb, String url, AttrAccess httpArgs, String content) {
      InputStream in = null;
      URLConnection uc = xpoststreamUrl2A(verb, url, httpArgs, content);
    try {
      in = uc.getInputStream();
      return in;
    } catch (IOException e1) {
      // server/node down
      throw new RuntimeException("Connection exception for url=" + url, e1);
    }
  }

  /**
   * Stream the response of a URL. - this is used by snakk for GET
   *
   * @param url can be local or remote
   * @param body null or payload to send - even GET can send body
   * @param httpArgs are sent as HTTP request properties
   * @return a string containing the text read from the URL. can be the result of a servlet, a web
   *         page or the contents of a local file. It's null if i couldn't read the file.
   */
  public static URLConnection streamUrlA(String url, String body, AttrAccess... httpArgs) {
    HttpURLConnection uc = null;

    int cout = MAX_CONNECT_TIMEOUT;
    int tout = MAX_TIMEOUT;

    try {
      if (httpArgs.length > 0) {
        for (String a : httpArgs[0].getPopulatedAttr()) {
          if (a.equals("snakkHttpOptions.readTimeout"))
            tout = Integer.parseInt(httpArgs[0].sa(a));
          if (a.equals("snakkHttpOptions.connectTimeout"))
            cout = Integer.parseInt(httpArgs[0].sa(a));
        }
      }
    } catch (Throwable t) {
      logger.warn("While parsing snakkHttpOptions: " + t.toString());
    }

    try {
      initTrustAll();
      uc = (HttpURLConnection) (new URL(url)).openConnection();
      uc.setConnectTimeout(cout);
      uc.setReadTimeout(tout);

        if (httpArgs.length > 0 && httpArgs[0] != null) {
          for (String a : httpArgs[0].getPopulatedAttr())
            if(!a.startsWith("snakkHttpOptions."))
              uc.setRequestProperty(a, httpArgs[0].sa(a));
        }

      if (body != null && body.length() > 0) {
        uc.setDoOutput(true);
        allowMethods("GET");
        uc.setRequestMethod("GET");
        OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
        String dataToWrite = "";
        dataToWrite = body;
        logger.trace(3, "POSTING: "+ dataToWrite);
        logger.log("POSTING: "+dataToWrite.length() + " bytes to " + url + " cout="+cout + " tout=" + tout);

        wr.write(dataToWrite);
        wr.flush();
        wr.close();
        uc.getOutputStream().close();  //don't forget to close the OutputStream
      }

      uc.connect();

        logger.trace(3, "hdr: ", uc.getHeaderFields());
      dfltHandler.accept(uc, tout);
      return uc;
    } catch (MalformedURLException e) {
      RuntimeException iex = new IllegalArgumentException();
      iex.initCause(e);
      throw iex;
    } catch (CommRtException re) {
      throw re;
    } catch (FileNotFoundException nef) { // 404 causes this - nuts !!!
      CommRtException rte = new CommRtException("NotFound exception for url=" + url, uc, getResponseCode(uc));
      rte.initCause(nef);
      throw rte;
    } catch (IOException io) { // 400 causes this - nuts !!
      CommRtException rte = new CommRtException("IO exception for url=" + url, uc, getResponseCode(uc));
      rte.initCause(io);
      throw rte;
    } catch (Exception e1) {
      // server/node down
      CommRtException rte = new CommRtException("Connection exception for url=" + url, (URLConnection)null);
      rte.initCause(e1);
      throw rte;
    }
  }

  /**
   * Stream the response of a URL.
   * 
   * @param url can be local or remote
   * @param httpArgs are sent as HTTP request properties
   * @return a string containing the text read from the URL. can be the result of a servlet, a web
   *         page or the contents of a local file. It's null if i couldn't read the file.
   */
  public static InputStream streamUrl(String url, AttrAccess... httpArgs) {
    URLConnection uc = null;

    int cout = MAX_CONNECT_TIMEOUT;
    int tout = MAX_TIMEOUT;
    if(httpArgs.length > 0) {
      for (String a : httpArgs[0].getPopulatedAttr()) {
        if (a.equals("snakkHttpOptions.readTimeout"))
          tout = Integer.parseInt(httpArgs[0].sa(a));
        if (a.equals("snakkHttpOptions.connectTimeout"))
          cout = Integer.parseInt(httpArgs[0].sa(a));
      }
    }

    try {
      initTrustAll();
      InputStream in = null;
      if (url.startsWith("file:")) {
        in = (new URL(url)).openStream();
      } else if (url.startsWith("http:") || url.startsWith("https:")) {
        uc = (new URL(url)).openConnection();
        uc.setConnectTimeout(cout);
        uc.setReadTimeout(tout);

        if (httpArgs.length > 0 && httpArgs[0] != null) {
          for (String a : httpArgs[0].getPopulatedAttr())
            if(!a.startsWith("snakkHttpOptions."))
            uc.setRequestProperty(a, httpArgs[0].sa(a));
        }

        logger.trace(3, "hdr: ", uc.getHeaderFields());
        logger.log("GET: from " + url + " cout="+cout + " tout=" + tout);

        in = uc.getInputStream();

        dfltHandler.accept(uc, tout);
      } else {
        File file = new File(url);
        in = file.toURL().openStream();
      }
      return in;
    } catch (MalformedURLException e) {
      RuntimeException iex = new IllegalArgumentException();
      iex.initCause(e);
      throw iex;
    } catch (CommRtException re) {
      throw re;
    } catch (FileNotFoundException nef) { // 404 causes this - nuts !!!
      CommRtException rte = new CommRtException("NotFound exception for url=" + url, uc, getResponseCode(uc));
      rte.initCause(nef);
      throw rte;
    } catch (IOException io) { // 400 causes this - nuts !!
      CommRtException rte = new CommRtException("IO exception for url=" + url, uc, getResponseCode(uc));
      rte.initCause(io);
      throw rte;
    } catch (Exception e1) {
      // server/node down
      CommRtException rte = new CommRtException("Connection exception for url=" + url + " : " + e1.getMessage(), (URLConnection)null);
      rte.initCause(e1);
      throw rte;
    }
  }

  /**
   * read the given stream into a String and return the string. It will read and concatenate chunks
   * of 100 bytes.
   *
   * @param fis an input stream
   * @return a string containing the text read from the stream. It's null if i couldn't read the
   *         file.
   */
  public static ByteArray readStreamBytes(InputStream fis) {
    if(fis != null) try {
      byte[] buff = new byte[ByteArray.BUFF_QUOTA];
      int n = 0;
      ByteArray xml = new ByteArray();
      while (xml.size() < MAX_BUF_SIZE && (n = fis.read(buff, 0, ByteArray.BUFF_QUOTA)) > 0) {
        xml.append(buff, n);
      }
      return xml;
    } catch (Exception e) { // an error occurs ...
      throw new RuntimeException("Cannot read from input stream ...", e);
    } finally {
      try {
        if(fis != null) fis.close();
      } catch (IOException e) {
        // do nothing here ...
      }
    } else return new ByteArray();
  }

  /**
   * read the given stream into a String and return the string. It will read and concatenate chunks
   * of 100 bytes.
   * 
   * @param fis an input stream
   * @return a string containing the text read from the stream. It's null if i couldn't read the
   *         file.
   */
  public static String readStream(InputStream fis) {
    return readStreamBytes(fis).toString();
  }

  /**
   * read the given stream into a String and return the string. It will read and concatenate chunks
   * of 100 bytes.
   * 
   * @param fis an input stream
   * @return a string containing the text read from the stream. It's null if i couldn't read the
   *         file.
   */
  public static String readStreamNoClose(InputStream fis) {
    try {
      byte[] buff = new byte[ByteArray.BUFF_QUOTA];
      int n = 0;
      ByteArray xml = new ByteArray();
      while (xml.size() < MAX_BUF_SIZE && (n = fis.read(buff, 0, ByteArray.BUFF_QUOTA)) > 0) {
        xml.append(buff, n);
      }
      return xml.toString();
    } catch (Exception e) { // an error occurs ...
      throw new RuntimeException("Cannot read from input stream ...", e);
    } finally {
    }
  }

  /**
   * Serialize to string the response of a URL.
   * 
   * @param url can be local or remote
   * @param httpArgs are sent as HTTP request properties
   * @return a string containing the text read from the URL. can be the result of a servlet, a web
   *         page or the contents of a local file. It's null if i couldn't read the file.
   */
  public static String readUrl(String url, AttrAccess... httpArgs) {
    InputStream s = streamUrl(url, httpArgs);
    if (s == null) {
      return null;
    }
    return readStream(s);
  }

  /**
   * is this the localhost? x can be either hostname or IP, ipv4, ipv6 etc
   * 
   * NOTE this is part of authorization chain
   */
  public static boolean isLocalhost(String x) {
    if ("127.0.0.1".equals(x) || "0:0:0:0:0:0:0:1".equals(x) || "localhost".equals(x))
      // ipv4ipv6 localhost
      return true;
    return false;

  }

  public static String encode(String in) {
    try {
      return java.net.URLEncoder.encode(in, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public static String decode(String in) {
    try {
      return java.net.URLDecoder.decode(in, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  /** from http://www.nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/ */
  public static synchronized void initTrustAll() throws NoSuchAlgorithmException, KeyManagementException {
      if (!trustAllInitialized) {
        logger.log("initTrustAll...");

        trustAllInitialized = true;

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session) {
            return true;
          }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
      }
    }
}
