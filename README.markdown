    /**  ____    __    ____  ____  ____/___      ____  __  __  ____
     *  (  _ \  /__\  (_   )(_  _)( ___) __)    (  _ \(  )(  )(  _ \
     *   )   / /(__)\  / /_  _)(_  )__)\__ \     )___/ )(__)(  ) _ <
     *  (_)\_)(__)(__)(____)(____)(____)___/    (__)  (______)(____/
     *                      
     *  Copyright (c) Razvan Cojocaru, 2007+, Creative Commons Attribution 3.0
     */

What?
=====

Snakked is a scala "naked objects" framework, with a twist, great for Rapid Application Development.

Naked Objects
-------------

In Razie's interpretation, what naked objects stands for is direct interaction between the users and the domain objects. What the users of an application do is manage the objects and their relationships, directly via generated or custom UIs or via APIs.

A naked objects framework should allow for:
- model/domain definition
- unified access and management interface of (domain) entities
- simple/default views and interaction
- some default support for persistency



Why?
----
So anyone can interact with a running application, having access to all or some of the application's objects. 

What is so special about the scrispter? It has full syntax coloring and content assist, for starters! 
It can be embedded into any application and shares the same port for telnet as well as web access...

How?
----

In the sbt Project file for your project (project/build/Project.scala), add the dependency: 

    val scrip = "com.razie" %% "snakked" % "0.1-SNAPSHOT"

If you want to build it, see Building.markdown


Roadmap
-------
May add some more scripting languages in the future but otherwise I don't think this will evolve.

It may also get nicer.


Examples
========

Many examples are in the junits: test_src/razie/scripster/test/

Put some objects in a context and run a simple scala script:

    val context = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScriptScala ("a+b").eval (context) getOrElse "?"

Create and add variables to the context, to pass to the next script:

    val ctx = new ScalaScriptContext(null, "a", "1", "b", "2")
    ScriptScala ("val c = a+b").interactive (ctx) 
    ScriptScala ("c").interactive (ctx) getOrElse "?"

Create a scripster server: razie.scripster.MainScripster.scala

    Scripster.createServer(4445)


Baddies
=======

There's no security - need to add some.
There's no mapping to a "normal" servlet. Jetty support etc.


Architectural notes
===================

Scripster and DCI
-----------------
The simplest application will define its domain model, a few contexts and just give users access to those objects in the respective contexts. That's Scripster for you!

Users are guided through what they can do with the domain objects by the available content assist...

You may want to create UIs to nice-ify the user interaction, but, once you have your domains and contexts, with Scripster, your basic app is up and running in less than 5 minutes.

