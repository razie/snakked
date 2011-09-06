
[Razie's Snakked](http://github.com/razie/snakked) is a scala naked objects framework with a twist.

Why? So you can easily source and manipulate any data in the major formats, via the major protocols, among others.


Snakking
========

razie.Snakk gives simple methods to suck content from URLs, including files etc in a few formats.

List of blog titles from an XML and JSON feed:

    val xmlFeed = url("http://feeds.razie.com/Razblog?format=xml")
    val jsonFeed = url("http://blog.razie.com/feeds/posts/default?alt=json")

    for (n <- xml(xmlFeed)) \ "channel" \ "item" \ "title") println (n.text)

    for (n <- json(jsonFeed)) \ "feed" \ "entry" \ "title" \@ "$t") println (n)



