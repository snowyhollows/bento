# Bento - scope-oriented, minimal dependency injection

Bento is an attempt at finding a set of trade-offs that would make Dependency Injection
practical for writing games, including small ones.

It turned out that it works very well for HPC applications as well.

The intended sweet spot for Bento are projects where:

1. There's a lot of configuration-style data to inject (i.e. strings, integers, booleans, enums).
1. For most objects, there's just one implementation of a type.
1. Useful objects have complex life-cycles.
1. It makes sense to only use the constructor injection.
1. Nobody minds adding Bento as a dependency.

It is possible to use Bento if any of all of the above are false,
but then there probably exists a better tool for the job.


