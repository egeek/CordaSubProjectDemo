# Demonstration of bundling issue

This project highlights an issue with the corda-gradle-plugin when it comes to building projects that depend on each 
other. In this repository there are two projects, Child1 and Child2. Child2 extends Child1, extending a State that 
has been defined in Child1. That's not really relevant though, the classes just exist to satisfy the need of the 
compiler to build something. The real problem appears to be related just to what is happening in the 
corda-gradle-plugin when it builds the semi-fat JAR.

## Building the demo

The demo is built like any other gradle project, nothing special is required. It might be useful to use 
`--rerun-tasks` to force gradle to rebuild everything. When testing, I used

Windows: `gradlew.bat build --rerun-tasks`
Linux: `./gradlew build --rerun-tasks`

## What to look for (expectation)

The main area of interest is the size and contents of the Child2 JAR, which should be built to 
`child2/build/libs/child2-1.0-SNAPSHOT.jar`. The desired result is that this JAR contains:

* Child2 classes
* Child1 classes
* Kotlin-Logging classes (and the classes of any of it's dependencies)
* BeanUtils classes (and the classes of any of it's dependencies)
* Does not contain Corda, or any of it's dependencies

During my testing, I was unable to get any combination to build a JAR containing just those. Note that nothing in 
Child1 actually uses any classes from Corda, that's not the point though, simply defining Corda (even as 
`cordaCompile`) in `build.gradle` of Child1 appears to be enough for Child2 to bring in Corda and all its dependencies.

## Tests

### Using compile project(':parent:child1') in Child2

By using `compile` instead of `corda project`, the JAR for Child2 is 25MB, and contains Corda, Hibernate, 
Kotlin, and all the other dependencies that Corda defines. Even though both the build scripts for Child1 and Child2 
use the recommended `cordaCompile` and `cordaRuntime` for defining Corda.

My understanding is that this is the correct way configure the `build.gradle` files, Child2 depends on Child1, and 
therefore should use `compile project`.

### Using cordapp project(':parent:child1') in Child2

From the documentation, this isn't the correct approach as it assumes that Child1 will exist as a JAR deployed on the
Corda node. That's not the desired approach, instead, the expectation is that the classes from Child1 will be 
included in Child2. 

Child1 is not another JAR that will be deployed on the node, it's more of a Library that utilises Corda. 
Never-the-less, this configuration means that the resultant JAR is small, but, it only contains the contents of 
Child2. Classes from Child1 are not in the JAR, and since that JAR will not be deployed on the Corda node, 
the code will fail at Runtime.

### Using cordaCompile project(':parent:child1') in Child2

From my understanding of the Corda documentation, this isn't right at all, as `cordaCompile` should be reserved for 
just Corda dependencies. However, in the interests of completeness I tried it out, and the results were pretty much 
the same as for `cordapp project`.

### Enabling failing the build on Duplicates in the JAR 

Turning on `DuplicatesStrategy.FAIL` by uncommenting the lines in the main `build.gradle` script shows that not only 
are the dependencies of Child1 being included incorrectly in Child2, but, that the corda-gradle-plugin is including 
them more than once.

## JAR contents

### Using compile project(':parent:child1') 

File size:
```
-rwxrwxrwx 1 steve steve 25573942 Jan 29 23:47 child2-1.0-SNAPSHOT.jar
```

JAR contents:
```
╭─ steve@STUDY-DESK   /mnt/c/Users/egeek/Development/r3/SubProjectDemo/parent/child2/build/libs     master                     3154   00:00:34  30.01.19 
╰─ jar -tf child2-1.0-SNAPSHOT.jar | grep Child
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$Companion.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$DefaultImpls.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child1/Child1.class
net/corda/core/utilities/ProgressTracker$Child.class
net/corda/core/utilities/ProgressTracker$setChildProgressTracker$subscription$1.class
net/corda/core/utilities/ProgressTracker$setChildProgressTracker$subscription$2.class

╭─ steve@STUDY-DESK   /mnt/c/Users/egeek/Development/r3/SubProjectDemo/parent/child2/build/libs     master                     3155   00:00:52  30.01.19 
╰─ jar -tf child2-1.0-SNAPSHOT.jar | grep corda
co/egeek/corda/
co/egeek/corda/bug/
co/egeek/corda/bug/gradle/
co/egeek/corda/bug/gradle/plugin/
co/egeek/corda/bug/gradle/plugin/child2/
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State.class
co/egeek/corda/bug/gradle/plugin/child1/
co/egeek/corda/bug/gradle/plugin/child1/Child1$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$Companion.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$DefaultImpls.class
co/egeek/corda/bug/gradle/plugin/child1/Child1$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child1/Child1.class
co/egeek/corda/bug/gradle/plugin/child1/ICanLog.class
net/corda/
net/corda/core/
net/corda/core/CordaException.class
net/corda/core/CordaInternal.class
net/corda/core/CordaOID.class
net/corda/core/CordaRuntimeException.class
net/corda/core/CordaThrowable.class
net/corda/core/DoNotImplement.class
net/corda/core/Utils$toFuture$1$1.class
net/corda/core/Utils$toFuture$1$subscription$1.class
net/corda/core/Utils$toObservable$1$1.class
...
```

### Using cordaCompile project(':parent:child1')

File size:
```
-rwxrwxrwx 1 steve steve 75044 Jan 30 00:05 child2-1.0-SNAPSHOT.jar
```

JAR contents:
```
╭─ steve@STUDY-DESK   /mnt/c/Users/egeek/Development/r3/SubProjectDemo/parent/child2/build/libs     master                     3158   00:05:27  30.01.19 
╰─ jar -tf child2-1.0-SNAPSHOT.jar | grep Child
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State.class

╭─ steve@STUDY-DESK   /mnt/c/Users/egeek/Development/r3/SubProjectDemo/parent/child2/build/libs     master                     3158   00:10:32  30.01.19 
╰─ jar -tf child2-1.0-SNAPSHOT.jar | grep corda
co/egeek/corda/
co/egeek/corda/bug/
co/egeek/corda/bug/gradle/
co/egeek/corda/bug/gradle/plugin/
co/egeek/corda/bug/gradle/plugin/child2/
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State.class
```

### Using cordaCompile project(':parent:child1')

File size:
```
-rwxrwxrwx 1 steve steve 75044 Jan 29 23:42 child2-1.0-SNAPSHOT.jar
```

JAR contents:
```
╭─ steve@STUDY-DESK   /mnt/c/Users/egeek/Development/r3/SubProjectDemo/parent/child2/build/libs     master                     3151   23:45:29  29.01.19 
╰─ jar -tf child2-1.0-SNAPSHOT.jar | grep Child
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State.class

╭─ steve@STUDY-DESK   /mnt/c/Users/egeek/Development/r3/SubProjectDemo/parent/child2/build/libs     master                     3152   23:46:30  29.01.19 
╰─ jar -tf child2-1.0-SNAPSHOT.jar | grep corda
co/egeek/corda/
co/egeek/corda/bug/
co/egeek/corda/bug/gradle/
co/egeek/corda/bug/gradle/plugin/
co/egeek/corda/bug/gradle/plugin/child2/
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion$log$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$Companion.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State$logMessage$1.class
co/egeek/corda/bug/gradle/plugin/child2/Child2State.class
```

## Corda-gradle-plugin 4.0.39

The output of corda-gradle-plugin 4.0.39 is quite different to 3.2.1

| Test          | Result
| --------------|--------------------------------------------------------------------------------------------|
| compile       | JAR is 17.1MB, doesn't contain Corda (good), but contains duplicated classes (e.g. Kotlin 
|               | Logging & BeanUtils)
| cordapp       | JAR is 5.3KB and only contains Child2 classes, no dependencies!
| cordaCompile  | JAR is 5.3KB and only contains Child2 classes, no dependencies!


### Duplicates in corda-gradle-plugin 4.0.39

Some of the duplicates in the resultant JAR when using corda-gradle-plugin 4.0.39 with the `compile project
(':parent:child1)` option. If it wasn't for the duplicates then this JAR would contain all the expected classes.

```
> Task :parent:child2:jar
Cordapp metadata not defined for this gradle build file. See https://docs.corda.net/head/cordapp-build-systems.html#separation-of-cordapp-contracts-flows-and-services
Encountered duplicate path "META-INF/kotlin-logging-common.kotlin_module" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "META-INF/kotlin-logging.kotlin_module" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KLoggable$DefaultImpls.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KLoggable.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KLogger.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KLogger.kotlin_metadata" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KLogging.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KMarkerFactory.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KMarkerFactory.kotlin_metadata" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KotlinLogging.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KotlinLogging.kotlin_metadata" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/KotlinLoggingMDCKt.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/Marker.kotlin_metadata" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/MarkerKt.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/NamedKLogging.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/internal/KLoggerFactory.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/internal/KLoggerNameResolver.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/internal/LocationAwareKLogger.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/internal/LocationIgnorantKLogger.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/internal/MessageInvokerKt.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "mu/internal/MessageInvokerKt.kotlin_metadata" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "META-INF/LICENSE.txt" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "META-INF/NOTICE.txt" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "META-INF/maven/commons-beanutils/commons-beanutils/pom.properties" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "META-INF/maven/commons-beanutils/commons-beanutils/pom.xml" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BaseDynaBeanMapDecorator$MapEntry.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BaseDynaBeanMapDecorator.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BasicDynaBean.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BasicDynaClass.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanAccessLanguageException.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanComparator.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanIntrospectionData.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanIntrospector.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$1.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$10.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$11.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$12.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$2.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$3.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$4.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$5.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$6.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$7.class" during copy operation configured with DuplicatesStrategy.WARN
Encountered duplicate path "org/apache/commons/beanutils/BeanMap$8.class" during copy operation configured with DuplicatesStrategy.WARN
...
```

## Current status

The code in the repository has been left so that the build is configured in the most correct (in the point of view of
the generated JARs) configuration. Its set to use `corda-gradle-plugin:4.0.39` with `compile project(:parent:child1)`
in Child2's `build.gradle`. It might be better to work from this version of the plugin rather than trying to port any
fixes back to 3.2.1.  