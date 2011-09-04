/**
 * Razvan's public code. Copyright 2008 based on Apache (share alike) see LICENSE.txt for details.
 *
 * Fairly light and flexible asset/resource management framework, a "naked objects" framework.
 * 
 * <h1>Overview</h1>
 * <p>There's a flurry of patterns and styles out there trying to simplify programming. The domain and model-oriented ones make the most sense. There are yet others that deal with the presentation (naked objects) in a similar manner.
 * 
 * <p>This is my attempt at covering this field, as I strongly believe that this is the way to go and that all applications should be built this way.
 * 
 * <h1>Vision</h1>
 * <p>
 * There are assets and resources all over the place, like movies, devices, a running windows
 * computer etc. These can be uniquely identified and managed (remotely).
 * <p>
 * The more code supports common asset management, the better they can integrate. This entire
 * project is mostly about an asset management framework and related utilities for managing those
 * assets (web servers, soa calls via http or upnp or whatever protocol you'd have).
 * 
 * <h2>What about assets?</h2>
 * 
 * An asset framework concerns with how they are defined, how we interact with them and not so much
 * about how they are implemented...
 * 
 * <h3>How they're defined</h3>
 * 
 * One must define:
 * <ul>
 * <li>the asset class (or a proxy class, if the asset is defined in a different environment) - use the
 * SoaAsset annotation
 * <li>CRUD+query functionality - overload the AssetMgr
 * <li>methods/actions that can be invoked - both SoaAsset as well as the AssetMgr
 * </ul>
 * 
 * <h2>Assets and services</h2>
 * <p>
 * The service-oriented world is good granularity for providing coarse functionality but it misses
 * the point when it comes to programming: you now have to think in terms of controller/stupid data,
 * unless you add a wrapper layer of intelligent objects that delegate their methods to services.
 * <p>
 * That's what this entire framework is about: providing a unified set of frameworks to allow rapid
 * and unified implementation of smart assets which then can be used and controlled remotely
 * <p>
 * This is the difference between <code>somerobotservice.move(myrobot, x, y)</code> and
 * <code>myrobot.move(x,y)</code>.
 * <p>
 * An asset-oriented viewpoint is richer than a service-oriented viewpoint since it implies notions
 * of presentation for the assets, containers of assets and management. It also allows clients to
 * look natural.
 * <p>
 * Why not "object-oriented" but rather "asset-oriented"? The notion of "object" is way too generic
 * and fuzzy for what we want here. Assets borrow from service the remoteness and add intelligence,
 * transparent control, and some notional presentation.
 * <p>
 * Why not "resource-oriented" but rather "asset-oriented"? I find that the notion of "resource" 
 * is not exactly what people have in mind when watching a movie...who's the resource? The player 
 * or the movie? Also, REST does not have a notion of action on resource or sending a message to a 
 * resource and that's how we all perceive and think about the world...
 * 
 * <h2>Inventories or managers</h2>
 * <p>
 * I know entity beans are dead (for good reason) but the notion of entity/home is still pervasive 
 * and makes sense. Factories for objects are abstracted into an <b>inventory</b>, which acts as a locator for
 * assets. The assets are assumed to be intelligent, but from a generic handling perspective, the
 * AssetMgr can use inventories to manage assets. This allows you to wrap external classes and
 * present them as assets. Since the assets are supposed to be used in higher-level scripting, the
 * end-result is almost the same: "asset.dosomething(a,b,c)" can be translated into
 * "findInventory().execute ("dosomething", asset, a,b , c)" be the asset management framework.
 * <p>
 * The recommendation is, of course, that assets be smart and not rely on inventories except for
 * location/management/CRUD".
 * <p>
 * Another benefit of passing executes through inventories is ease of implementing remote delegation
 * etc (there's just one place to remote).
 * 
 * <h1>Assets</h1>
 * <p>
 * Assets are uniquely identified by type, key and location: {@see AssetKey}.
 * <h1>Frameworks</h1>
 * <p>
 * The lightsoa framework provides the service abstraction, which is good for remote block of
 * functionality. On top of this we add the smart asset objects.
 * 
 * <h1>REST</h1>
 * <p>
 * Assets lend themselves well to the REST programming. Same as resources, slight semantic
 * difference. The biggest difference is that one can act on assets, which makes them more suitable
 * for use by humans :)
 * 
 * 
 * STATE: post-concept, changes but not often
 * 
 * @version $Id: package-info.java,v 1.1 2007-10-02 11:54:36 razvanc Exp $
 */
package razie.assets;

