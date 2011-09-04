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
 - plugin: entity management, interaction etc
 - custom views


How?
----

In the sbt Project file for your project (project/build/Project.scala), add the dependency: 

    val snakk = "com.razie" %% "snakked" % "0.1-SNAPSHOT"

If you want to build it, see Building.markdown


Roadmap
-------

1. complete the basis: model and inventories
2. add little bit UI
3. XP extras and snakking


Snakking
-----

razie.Snakk gives simple methods to suck content from URLs, including files etc in a few formats.

List of blog titles from an XML and JSON feed:

    val xmlFeed = "http://feeds.razie.com/Razblog?format=xml"
    val jsonFeed = "http://blog.razie.com/feeds/posts/default?alt=json"

    for (n <- xml(url(xmlFeed)) \ "channel" \ "item" \ "title") println (n.text)

    for (n <- json(url(jsonFeed)) \ "feed" \ "entry" \ "title" \@ "$t") println (n)
    

Architectural notes
===================

Snakked and DCI
-----------------

