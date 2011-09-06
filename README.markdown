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


Snakking
--------

razie.Snakk gives simple methods to suck content from URLs, including files etc in a few formats and then access them in a unified manner, based on an addressing sceheme like XPATH.

List of blog titles from an XML and JSON feed:

    val xmlFeed = "http://feeds.razie.com/Razblog?format=xml"
    val jsonFeed = "http://blog.razie.com/feeds/posts/default?alt=json"

    for (n <- xml(url(xmlFeed)) \ "channel" \ "item" \ "title") println (n.text)

    for (n <- json(url(jsonFeed)) \ "feed" \ "entry" \ "title" \@ "$t") println (n)
    

Naked Objects
-------------

In Razie's interpretation, what naked objects stands for is direct interaction between the users (and developers) and the domain objects. What the users of an application do is manage the objects and their relationships, directly via generated or custom UIs or via APIs.

A naked objects framework should allow for:

 - model/domain definition
 - simple/default views and interaction
 - some default support for persistency
 - custom views
 - unified access and management interface of (domain) entities
 - plugin: entity management, interaction etc

The first three are, by now, classic. The others are natural extensions.


Roadmap
-------

 - complete the basis: model and inventories
 - add little bit UI
   - basic properties and navigation from mutant
   - tree
   - table
   - graph
   - full graphical UI
 - XP extras and snakking
   - snakk sourcing
      - snakk java beans (needs testing)
      - snakk EMF beans
      - snakk snakks (unify the model here?)
      - snakk RDB?
   - snakk views
      - graph navigation, complete edge implementation
      - snakked tree view
   - snakk mapping
 - full workbench


Why "Snakked" ?
------------

Well, it really means
 - scala naked objects => scala naked => s-naked => snakked (use kk to underline the lack of reptilian dependencies)
 - funny spelling for snack (~ed), (~ing)


How?
----

In the sbt Project file for your project (project/build/Project.scala), add the dependency: 

    val snakk = "com.razie" %% "snakked" % "0.1-SNAPSHOT"

Snakking:
1. snakk java beans (done, needs testing)
2. snakk EMF beans
3. snakk adapters


Architectural notes
===================


Snakked and DCI
-----------------


Bits and pieces for the curious
-------------------------------

You can do a lot better and not waste your time with this section. For the really curious with lots of time on their hands, read on.

Different kinds of keys and unique Ids are in razie.g - I like this: reference an entity by a query or some properties or in some other fuzzy way.

First draft of the unified access/management interface is in razie.assets - I'm trying to simplify it.



