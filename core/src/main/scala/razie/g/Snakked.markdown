Snakked - Razie's naked objects
===============================

You can read about "naked objects" on wikipedia. Basically it's a domain-oriented software 
developement methodology, where the focus is on the "naked objects" of the domain model.

This is what you see, this is what you get, period!

Design
------

A GRef uniquely identifies an entity (call it domain object, asset whatever). Such an entity
has
 * a type (meta or class)
 * an ID
 * a location
 
Entities can be uniquely identified in as many ways as there are search options out there:
 * unique/distinct ID
 * query
 * xpath

The basic responsibility of the framework is making sure you can find entities, via 
GAMResolver; manage entities via GCRUD and finally do stuff with/on entities, via
GAMAct.

The underlying idea is that all there is, the world, is just a graph of entities and their
associations/relationships. We model it all here and let you interface specific sub-domains
via plug-ins. Then you write your generic code regardless of the application domain or the 
shape of the domain objects...as long as you know their specs...

