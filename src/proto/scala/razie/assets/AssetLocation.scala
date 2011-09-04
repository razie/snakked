/**  ____    __    ____  ____  ____,,___     ____  __  __  ____
 *  (  _ \  /__\  (_   )(_  _)( ___)/ __)   (  _ \(  )(  )(  _ \           Read
 *   )   / /(__)\  / /_  _)(_  )__) \__ \    )___/ )(__)(  ) _ <     README.txt
 *  (_)\_)(__)(__)(____)(____)(____)(___/   (__)  (______)(____/    LICENSE.txt
 */
package razie.assets

import java.io.File;
import java.io.Serializable;
import java.net.URL;

import com.razie.pub.FileUtils;
//import com.razie.pub.comms.AgentHandle;
import com.razie.pub.comms.Agents;
import com.razie.pub.comms.Comms;
import razie.g.GLoc

/**
 * the location of an asset, either a remote url like below or a directory.
 * 
 * a location is always on the form: protocol://host:port/PATH
 * 
 * for mutant, the format is mutant://host:port::PATH
 * 
 * for others it's http://host:port/url
 * 
 * <p>
 * inspired from OSS/J's application environment, highly simplified (arguably...).
 * 
 * TODO 3-2 PERF parse only on demand and lazy store after parsing
 * 
 * @author razvanc99
 */
class AssetLocation (o:String) {
   private val (iRemoteUrl, iLocalPath) = setURL(o)
   
   lazy val gloc = new razie.g.GLoc (iRemoteUrl, localPath)

   def getHost() = host // TODO inine
   def getPort() = port // TODO inine
   
   def host = hostport._1
   def port = hostport._2
  
   /** points to a mutant location */
   def isMutant = 
      this.iRemoteUrl != null && (this.iRemoteUrl.startsWith("mutant://") || this.iRemoteUrl.contains("::"))

   /** returns true if this NewAppEnv points to a local directory */
   def isLocal () = 
      if (iLocalPath==null && iRemoteUrl==null)
         true
      else if (isMutant) {
         val h = this.host
         if (h.equals(Agents.getMyHostName()) || "local".equals(h)
               || Comms.isLocalhost(h)) true else false
      } else {
         this.iLocalPath != null && this.iLocalPath.length() > 0;
      }

   /** returns true if this points to a remote server */
   def isRemote () = 
      if (isMutant) {
         !isLocal;
      } else {
         this.iRemoteUrl != null && this.iRemoteUrl.length() > 0;
      }

   override def toString = 
      if (this.iRemoteUrl != null ) this.iRemoteUrl else this.iLocalPath;

   /**
    * make an http URL, if remote. the remote reference could use other protocols, like mutant://
    * and this will convert it to http://
    */
   def toHttp () = 
      if (isMutant) {
         "http://" + this.host + ":" + this.port;
      } else
      this.iRemoteUrl;

   /** smart setting of the actual URL */
   private[this] def setURL(url:String ) : (String,String) = {
      var ru : String = null
      var lp : String = null
      
      if (url == null) {
         ru = null
      } else {
         // it's an URL. factory is then the default, I take it?
         if (url.indexOf("mutant:") >= 0) {
            // format: mutant://computer:port::localpath
            ru = url;
            lp = null;
         } else if (url.indexOf("http:") >= 0) {
            ru = url;
            lp = null;
         } else {
            lp = setLocalPath(url);
            // this is a local PATH, need to make sure it ends with a "/" - all other code will
            // simply concatenate file names to it
            if (lp != null && !"".equals(lp) && !lp.endsWith("/")
                  && !lp.endsWith("\\")) {
               lp += "/";
            }
         }

         // TODO not sure why i do this
//         if (!isMutant && ru != null && ru.endsWith("/")) {
//            ru = ru.substring(0, ru.length() - 1);
//         }
      }
      (ru, lp)
   }

   def localPath : String = getLocalPath
   def getLocalPath () : String = {
     val p = origgetLocalPath
     if (p != null && ! p.endsWith("/")) 
        p+"/"
        else p
   }

   def origgetLocalPath () : String = 
      if (this.iRemoteUrl != null) {
         if (this.iRemoteUrl.contains("::")) {
            val sp = this.iRemoteUrl.split("::");
            if (sp.length > 1 ) sp(1) else null
         } else
         null;
      } else
         this.iLocalPath;

   /** will get canonic path unless the path is in the classpath */
   private[this] def setLocalPath(lp:String ) : String = {
      if (lp != null) {
         if(lp.startsWith("jar:") ) lp else FileUtils.toCanonicalPath(lp);
      } else null
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   override def hashCode() : Int = {
//      final int PRIME = 31;
      // int result = super.hashCode();
      var result = 1;
      result = 31 * result + (if(iLocalPath == null)  0 else iLocalPath.hashCode());
      result = 31 * result + (if(iRemoteUrl == null)  0 else iRemoteUrl.hashCode());
      result;
   }

   /** turn into a URL. If looking for files local or classpath, use toUrl (fileName) */
   def toURL ={
      var url : URL = null;

      try {
         if (this.isLocal) {
            val f = new File(this.iLocalPath);
            if (f.exists()) {
               url = f.getCanonicalFile().toURL();
            }
         } else {
            url = new URL(this.iRemoteUrl);
         }
      } catch {
         case e:Exception => throw new IllegalStateException("can't turn into URL, NewAppEnv=" + this.toString(), e);
      }
      url;
   }

   /**
    * toUrl when this NewAppEnv localpath contains the directory and the parameter is the actual
    * filename. For remote appoEnv, the fileName is ignored
    */
   def toURL(fileName:String) = {
      var url:URL = null;

      try {
         if (this.isLocal) {
            // treat classpath url's differently
            if (this.iLocalPath.startsWith("jar:")) {
               url = new URL(this.iLocalPath + fileName);
            } else {
               val f = new File(this.iLocalPath + fileName);
               if (f.exists()) {
                  url = f.getCanonicalFile().toURL();
               }
            }
         } else {
            url = new URL(this.iRemoteUrl);
         }
      } catch {
         case e:Exception =>
         throw new IllegalStateException("can't turn into URL, NewAppEnv=" + this.toString() + " fileName="
               + fileName, e);
      }
      url;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   override def equals(obj:Any):Boolean = {
      if (obj == null)
         return false;
      val other = if (obj.isInstanceOf[GLoc]) AssetLocation.fromGLoc(obj.asInstanceOf[GLoc]) else obj.asInstanceOf[AssetLocation]
      if (iLocalPath == null) {
         if (other.iLocalPath != null)
            return false;
      } else if (!iLocalPath.equals(other.iLocalPath))
         return false;
      if (iRemoteUrl == null) {
         if (other.iRemoteUrl != null)
            return false;
      } else if (!iRemoteUrl.equals(other.iRemoteUrl))
         return false;
      true;
   }

   private def hostport : (String,String) = {
      if (this.iRemoteUrl != null && this.iRemoteUrl.contains("://")) {
         val l = this.iRemoteUrl.split("://")(1);
         var ipport = l.split("/", 2)(0); // remove any uninteresting path in a URL
         ipport = ipport.split("::", 2)(0); // mutant://host:port::localpath
         
         // is there always a port? OR run at the default 8080...
         val colon = ipport.lastIndexOf(":");

         if (colon > 0) {
            val port = ipport.substring(colon + 1);
            val srcIp = ipport.substring(0, colon);
            (srcIp, port)
         }
         else 
            (ipport, "8080")
      }
      else 
            (Agents.getMyHostName(), Agents.me().port) // default to my port
   }

   def hipport = 
      if (this.iRemoteUrl != null) {
        if (this.iRemoteUrl.contains("::")) 
           this.iRemoteUrl.split("::", 2)(0); // mutant://host:port::localpath
        else 
           this.iRemoteUrl 
      }
      else 
         null
   
   def protocol :String = 
      if (this.iRemoteUrl != null && this.iRemoteUrl.contains("://")) {
         this.iRemoteUrl.split("://")(0);
      } else
      null;

   def toGLoc = new razie.g.GLoc (hipport, origgetLocalPath)
}

object AssetLocation {
   def fromGLoc (x:razie.g.GLoc)= new AssetLocation (x.toXX)
   
   implicit def fs (s:String) : AssetLocation = new AssetLocation (s)

   def mutantEnv(host:String , dir:String ) = {
      var d = Agents.agent(host);
      if (d == null) {
         // try by ip
         d = Agents.agentByIp(host);
      }

      if (d == null) {
         throw new IllegalArgumentException("Unknown host/ip: " + host);
      }

      new AssetLocation("mutant://" + host + ":" + d.port + "::" + prepLocalDir(dir));
   }

   def mutantEnv(dir:String ) ={
      val me = Agents.agent(Agents.getMyHostName());
      // NOTE mutant URLs must contain hostname not IP !!!
      new AssetLocation("mutant://" + me.hostname + ":" + me.port + "::" + prepLocalDir(dir));
   }

   private def prepLocalDir(d:String ) = {
      val dir = if(d.startsWith("jar:") ) d else FileUtils.toCanonicalPath(d)

      // this is a local PATH, need to make sure it ends with a "/" - all other code will
      // simply concatenate file names to it
      if (dir != null && !"".equals(dir) && !dir.endsWith("/") && !dir.endsWith("\\")) {
         dir + "/";
      }
      else dir;
   }

   val LOCAL = new AssetLocation (null)
}
