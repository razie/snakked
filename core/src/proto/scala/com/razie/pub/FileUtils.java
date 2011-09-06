/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details. No warranty implied nor any liability assumed for this code.
 */
package com.razie.pub;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * a bunch of utilities for dealing with files and urls and such
 * 
 * @author razvanc99
 * 
 */
public class FileUtils {

   public static File fileFromUrl(URL url) {
      File f;
      try {
         f = new File(url.toURI());
      } catch (URISyntaxException e) {
         f = new File(url.toExternalForm());
      }

      return f;
   }

   /**
    * get the canonical path: replace ../ with parent dir etc
    *
    * @param myPath input path
    * @return the canonical path
    */
   public static String toCanonicalPath(String myPath) {
      if (myPath == null) {
         return myPath;
      }

      String path = myPath.replace('\\', '/');
      path = path.replaceAll("/\\./", "/");

      // NOT sure why this section was here - removed it...
      // if (!path.startsWith("file://")) {
      // path = path.replaceAll("//", "/");
      // path = path.replaceAll("file:", "");
      // }

      int idx = 0;
      while ((idx = path.indexOf("../")) >= 0) {
         int index2 = idx + 2; // the second part of the string
         int index1 = idx; // must find this new index
         int countFileSeparator = 0;
         for (int i = index1; i >= 0; i--) {
            char ch = path.charAt(i);
            if (ch == '/') {
               countFileSeparator++;
               if (countFileSeparator == 2) {
                  countFileSeparator = 0;
                  index1 = i;
                  break;
               }
            }
         }
         if (countFileSeparator == 1) {
            index1 = 0;
         }
         path = path.substring(0, index1) + path.substring(index2);
      }
      if (path.startsWith("/") && path.indexOf(":/") == 2) // like this: /c:/gugu --> c:/gugu
      {
         return path.substring(1);
      }
      return path;
   }

   /**
    * extract just the filename from a given path
    * 
    * @param fullFileNm
    * @return only the filename
    */
   public static String fileNameFromPath(String fullFileNm) {
      String result = "";
      int idx = fullFileNm.lastIndexOf("/");
      if (idx == -1) {
         idx = fullFileNm.lastIndexOf("\\");
      }
      if (idx == -1) {
         result = fullFileNm;
      } else {
         result = fullFileNm.substring(idx + 1);
      }
      return result;
   }
}
