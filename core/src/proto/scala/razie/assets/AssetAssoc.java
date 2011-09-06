/**
 * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
 * details.
 */
package razie.assets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * this asset type is associated to/with...
 * 
 * @author razvanc99
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@Inherited
public @interface AssetAssoc {
   /**
    * the type of the asset modelled by this class. You can leave it null ONLY if registering with Assets.manage()
    */
   String name () ;

   /** the type of the asset modelled by this class */
   String a() ;

   /** the type of the asset modelled by this class */
   String z() ;

   /** the type of the asset modelled by this class */
   String stereotype() default "assoc";

   /** the type of the asset modelled by this class */
   String aRole() default "";

   /** the type of the asset modelled by this class */
   String zRole() default "";

   /** the type of the asset modelled by this class */
   String card() default "0-n";

   /** the value is a description of the asset type */
   String descr();
}
