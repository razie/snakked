    /**  ____    __    ____  ____  ____/___      ____  __  __  ____
     *  (  _ \  /__\  (_   )(_  _)( ___) __)    (  _ \(  )(  )(  _ \
     *   )   / /(__)\  / /_  _)(_  )__)\__ \     )___/ )(__)(  ) _ <
     *  (_)\_)(__)(__)(____)(____)(____)___/    (__)  (______)(____/
     *                      
     *  Copyright (c) Razvan Cojocaru, 2007+, Creative Commons Attribution 3.0
     */

What?
-------

Snakked is a scala "naked objects" framework, with a twist, great for Rapid Application Development.


Snakking
========

razie.Snakk gives simple methods to suck content from URLs, including files etc in a few formats and then access them in a unified manner, based on an addressing sceheme like XPATH.

List of blog titles from an XML and JSON feed:
    import razie.Snakk._

    val xmlFeed  = url ("http://feeds.razie.com/Razblog?format=xml")
    val jsonFeed = url ("http://blog.razie.com/feeds/posts/default?alt=json")

    for (n <- xml(xmlFeed) \ "channel" \ "item" \ "title") println (n.text)

    for (n <- json(jsonFeed) \ "feed" \ "entry" \ "title" \@ "$t") println (n)

You can easily read from an HTTP POST as well as the GET above:

    val xmlFeed  = url ("http://feeds.razie.com/Razblog?format=xml", razie.AA("field1"->"f1"), "POST")

If you only want to get the body of the server response, wihtout the XPATH wrappers, then

    val response = body(xmlFeed)

See more examples in core/src/test - some are copied below:

Snakking a bean example:

    case class Student (name: String, age: Int)
    case class Class   (name: String, students: Student*)
    case class School  (name: String, classes: Class*)

    val school = Snakk bean School("my school",
      Class("1st grade",
        Student("Joe", 6),
        Student("Ann", 7)),
      Class("2nd grade",
        Student("Mary", 8),
        Student("George", 7)))

    @Test def test11 = expect("Ann" :: "George" :: Nil) { school \ "classes" \ "students[age==7]" \@ "name" }
    @Test def test12 = expect("Ann" :: "George" :: Nil) { school \\ "students[age==7]" \@ "name" }


Can snakk a bean by type of attribute - in example below we use C3 class name. Useful when you just don't care and want to find a needle in the haystack.

    case class C1 { val c=C2 }
    case class C2 { val c=C3 }
    case class C3 { val c="wow" }
  
    @Test def test21  = expect("wow" :: Nil) { Snakk.bean(C1) \\ "C3" \@ "c" }


More features
-------------

The idea of snakking is just rapid sampling of data of all sorts. Here are some, I think cool and helpful, features:

 - Recursivity using the double backslash \\\\ operator
 - embedded pattern matching operator ~=
 - fallback attribute sourcing using OR
 - in XML snakking, if a requested attribute is missing, we'll fallback on the value of a node with the same name
 - bean snakking 
  - any field or method (for instance apache jxpath will only look at getters, which is not suiteable for snakking scala code)
  - custom filters for method names (because of the above) with Snakk.bean (root, filters)
  - careful when using wildcards and recursivity - it will call all sorts of methods if they're not filtered...


Embedded pattern matching operator: 

    val G = school \\ "students[name~='G.*']" \@@ "name"

OR - fallback (the alternative is not evaluated unless the attribute is null/missing)

    val someone:String = (school \\ "students[age==7]" \@@ "name") OR ("nobody")

XML attribute fallback

    i.e. <x name="tutu" /> can also be <x><name>tutu<name></x> and addressed with x \@ "name"


Uses and articles
==============

This article can shed more details on other uses of snakked: http://blog.razie.com/2012/05/free-flowing-wiki-domain-models-and.html

Snakked is used in http://www.racerkidz.com


Naked Objects
==============

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
===============

Well, it really means

 - scala naked objects => scala naked => s-naked => snakked (use kk to underline the lack of reptilian dependencies)
 - funny spelling for snack (~ed), (~ing)


How?
----

In the sbt Project file for your project (project/build/Project.scala), add the dependency: 

    val snakk = "com.razie" %% "snakked" % "0.4.3-SNAPSHOT"

Or Maven style:

    <dependency>
        <groupId>com.razie</groupId>
        <artifactId>snakked_2.9.1</artifactId>
        <version>0.4.3-SNAPSHOT</version>
    </dependency>




Architectural notes
===================


Mixing pot
----------

No project is a net new invention, all are built on the shoulders of others. Here's some of the stuff that I remember throwing in this mixing pot.

 * REST  - simple entity based addressing and management via HTTP
 * XCAP  - unified access to configuration in an XPath manner
 * XPATH - well, itself
 * OSS/J - set of OSS through Java standards - based on a nice common generic entity management API


Snakking:

1. snakk java beans (done, needs testing)
2. snakk EMF beans
3. snakk adapters


Snakked and DCI
-----------------


Bits and pieces for the curious
-------------------------------

You can do a lot better and not waste your time with this section. For the really curious with lots of time on their hands, read on.

Different kinds of keys and unique Ids are in razie.g - I like this: reference an entity by a query or some properties or in some other fuzzy way.

First draft of the unified access/management interface is in razie.assets - I'm trying to simplify it.

XP and graph navigation including links and associations in razie.XP

Graph support in razie.g as well


