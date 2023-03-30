# MIMA

This is an experiment of creating "MIni MAven" reusable library to use Maven Resolver wherever you are.

## What is this about?

Reusing Maven Resolver is hard, especially if you need same thing to work in a library that may be used
from within Maven (ie. as part of Mojo) but also from outside of Maven (like part of some CLI).

The purpose of MIMA is to address this issue, and make your library reusable wherever it runs. But let's step
back a bit.

Using Maven Resolver as a library was always hard: for start, resolver alone is "incomplete", in a sense that
code in https://github.com/apache/maven-resolver repository **does not contain any model classes to work
with**. They are provided by Maven itself, when resolver is integrated. Moreover, is incomplete in a sense
that **not all components are implemented**. Resolver in Maven is "completed" by resolver-provider Maven
module here https://github.com/apache/maven/tree/maven-3.9.x/maven-resolver-provider but even this alone module
is not enough: just check it's dependencies, there are models, builders. And finally, to be able to properly
augment user environment from outside of Maven, obey things as user `settings.xml` etc., you even need more.

While some half-solutions did exist so far (most popular was Resolver's `ServiceLocator`), these solutions were 
never complete: `ServiceLocator` never offered "full experience" as compared to Resolver in Sisu, it was missing
the dynamism Sisu offered (for example, it was not extensible). Moreover, `ServiceLocator` is **deprecated** in
latest Resolver releases, for a reason: it forces Resolver developers to apply some (archaic) compromises, while
developing Resolver components, forbids ctor injection, components must have default ctors and have to be 
written like "this or that", when managed by Sisu or `ServiceLocator`, components cannot be immutable, etc. 
`ServiceLocator` pretty much forces you to write components you had to write in "good old" Plexus days.

Moreover, recent changes in Maven 3.9.x explicitly **prevents creation of new `RepositorySystem` instances**, for 
a good reason: if you run within Maven, you have everything "offered on a plate" (just inject it), no need to 
reinvent the wheel, you have full environment initialized with user specs and setup. No need to reinvent the wheel: 
less code, less bugs. This made `ServiceLocator` defunct in Maven 3.9.x and beyond as well.

Finally, Maven project never offered one-stop shop solution to use Resolver as a library. There was `ServiceLocator`
(incomplete, deprecated, to be dropped), Guice module (incomplete, likely to be deprecated) and Sisu indexes (incomplete)
but nothing as "a whole", a glue that holds things together for most popular use of Resolver: make it usable as 
it "as in Maven but outside of Maven".

MIMA tries to go one step beyond: It's goal is to make Resolver easily reusable "as a library", outside of Maven
but inside of it as well. And to do that in transparent way.

## In a nutshell

MIMA provides several artifacts:

* [context](context/) - This is the artifact you should depend on in `compile` scope, and use to get Resolver 
  environment, customize it, etc.
* [runtime](runtime/) - Various runtimes for different purposes and use cases, these should be in `runtime` scope.

To demonstrate, an example [app](app/) "demo project" is provided: it creates a "library" artifact, that contains
some business logic that needs Resolver (to calculate classpath for given artifact) and it works as "plain library"
as UTs demonstrate. But, to introduce a twist, this same library is then used as dependency in "library-maven-plugin",
a Maven Plugin that does the same thing by reusing the "library". Here also, Invoker ITs prove that the Mojo works
as expected. The `app` subproject has been tested with Maven 3.9.x and Maven 3.8.x to behave as expected.

## Runtimes

MIMA offers following runtimes:
* [embedded-maven](runtime/embedded-maven) - this runtime should be always present (is dependency-free, all are in 
  `provided` scope). It "activates" when runtime finds itself inside a Maven instance, and installs itself with
  highest priority.
* [standalone-sisu](runtime/standalone-sisu) - this runtime may be always present (but in case of Maven Plugin it will 
  generate a ton of warnings due wrong Maven artifact scopes), but is best to simply exclude it in Maven Plugin POMs.
  This is a "fallback" runtime, when no other runtime is present, has lowest priority. It is meant to be used outside
  a Maven instances, and has lowest priority.
* [embedded-sisu](runtime/embedded-sisu) - (unused in demo) this runtime may be used in case you have an application
  that is already using Sisu for DI, like apps using Ollie https://github.com/takari/ollie or Sonatype Nexus2 is.

