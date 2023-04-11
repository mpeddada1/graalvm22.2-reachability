# graalvm22.2-reachability

Test project to reproduce issues with concurrent reachability handlers in GraalVM 22.2.

## Steps to reproduce
1) `java --version`
```
openjdk 11.0.16 2022-07-19
OpenJDK Runtime Environment GraalVM CE 22.2.0 (build 11.0.16+8-jvmci-22.2-b06)
OpenJDK 64-Bit Server VM GraalVM CE 22.2.0 (build 11.0.16+8-jvmci-22.2-b06, mixed mode, sharing)
```
2) `mvn clean install -DskipTests`
3) `cd child-module`.
4) `mvn test -Pnative`
5) See the following error:
```
Failures (1):
  JUnit Vintage:MySampleTest:testSample
    MethodSource [className = 'com.example.MySampleTest', methodName = 'testSample', methodParameterTypes = '']
    => java.lang.RuntimeException: Generated message class "com.example.MySampleClass" missing method "getName".
       com.anotherpackage.GeneratedMessage.retrieveMethod(GeneratedMessage.java:31)
       com.anotherpackage.GeneratedMessage.access$000(GeneratedMessage.java:5)
       com.anotherpackage.GeneratedMessage$MethodAccessor.<init>(GeneratedMessage.java:20)
       com.anotherpackage.GeneratedMessage$A.initializeMethodAccessor(GeneratedMessage.java:11)
       com.example.MySampleClass.invokeAccessor(MySampleClass.java:13)
       [...]
     Caused by: java.lang.NoSuchMethodException: com.example.MySampleClass.getName()
       java.lang.Class.getMethod(DynamicHub.java:2108)
       com.anotherpackage.GeneratedMessage.retrieveMethod(GeneratedMessage.java:28)
       [...]
```
6) Change jvm version and sdk version in the parent pom.xml to GraalVM 22.1.0 and see test succeed at runtime.
Behavior of reachibility handlers was changed to [run concurrently by default](https://github.com/oracle/graal/blob/8eca77b66a2d29a02aab7e963a4e84ee34dcad0c/substratevm/src/com.oracle.svm.hosted/src/com/oracle/svm/hosted/ConcurrentReachabilityHandler.java#L50) as of GraalVM 22.2. 


For some reason, the `access.registerMethodOverrideReachabilityHandler()` method gets invoked in GraalVM 22.1.0 but not in GraalVM 22.2.0.

**Workaround**: Disable concurrent reachability handler by providing the following option to the `native-image` builder:
`-H:-RunReachabilityHandlersConcurrently`


## Troubleshooting Notes

(1) Run `export USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM=true` before `mvn test -Pnative` also results in same failure:
```
**********VALUE of USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM************
true
com.example.MySampleTest > testSample FAILED


Failures (1):
  JUnit Vintage:MySampleTest:testSample
    MethodSource [className = 'com.example.MySampleTest', methodName = 'testSample', methodParameterTypes = '']
    => java.lang.RuntimeException: Generated message class "com.example.MySampleClass" missing method "getName".
       com.anotherpackage.GeneratedMessage.retrieveMethod(GeneratedMessage.java:31)
       com.anotherpackage.GeneratedMessage.access$000(GeneratedMessage.java:5)
       com.anotherpackage.GeneratedMessage$MethodAccessor.<init>(GeneratedMessage.java:20)
       com.anotherpackage.GeneratedMessage$A.initializeMethodAccessor(GeneratedMessage.java:11)
       com.example.MySampleClass.invokeAccessor(MySampleClass.java:13)
       [...]
     Caused by: java.lang.NoSuchMethodException: com.example.MySampleClass.getName()
       java.base@11.0.17/java.lang.Class.getMethod(DynamicHub.java:2108)
       com.anotherpackage.GeneratedMessage.retrieveMethod(GeneratedMessage.java:28)
       [...]
```

## Does this sample work without the workaround in GraalVM 22.3.1 (latest)?
No, the error is reproducible with GraalVM 22.3.1 as well

```build
===================================================================================
GraalVM Native Image: Generating 'native-tests' (executable)...
===================================================================================
[1/7] Initializing...                                               (5.1s @ 0.22GB)
 Version info: 'GraalVM 22.3.1 Java 11 CE'
 Java version info: '11.0.18+10-jvmci-22.3-b13'
 C compiler: gcc (linux, x86_64, 12.2.0)
 Garbage collector: Serial GC
 2 user-specific feature(s)
 - com.example.MyNativeImageFeature
 - org.graalvm.junit.platform.JUnitPlatformFeature
[junit-platform-native] Running in 'test listener' mode using files matching pattern [junit-platform-unique-ids*] found in folder [${HOME}/graalvm22.2-reachability/child-module/target/test-ids] and its subfolders.
[2/7] Performing analysis...  [******]                             (11.0s @ 1.58GB)
   4,363 (79.10%) of  5,516 classes reachable
   5,430 (58.46%) of  9,288 fields reachable
  19,097 (50.47%) of 37,835 methods reachable
     155 classes,     2 fields, and   545 methods registered for reflection
      58 classes,    58 fields, and    52 methods registered for JNI access
       4 native libraries: dl, pthread, rt, z
[3/7] Building universe...                                          (1.9s @ 0.85GB)
[4/7] Parsing methods...      [*]                                   (1.4s @ 1.92GB)
[5/7] Inlining methods...     [***]                                 (0.9s @ 2.53GB)
[6/7] Compiling methods...    [***]                                 (8.4s @ 1.52GB)
[7/7] Creating image...                                             (1.8s @ 2.11GB)
   6.23MB (37.56%) for code area:    11,308 compilation units
   9.52MB (57.38%) for image heap:  120,459 objects and 7 resources
 859.91KB ( 5.06%) for other data
  16.59MB in total
-----------------------------------------------------------------------------------
Top 10 packages in code area:            Top 10 object types in image heap:
 816.60KB java.util                         1.35MB byte[] for code metadata
 394.53KB com.sun.crypto.provider           1.11MB java.lang.String
 358.69KB java.lang                         1.02MB java.lang.Class
 284.24KB java.util.concurrent            977.52KB byte[] for general heap data
 224.78KB java.text                       824.72KB byte[] for java.lang.String
 207.10KB java.util.stream                410.53KB java.util.HashMap$Node
 205.18KB java.util.regex                 409.03KB c.o.s.c.h.DynamicHubCompanion
 183.88KB java.io                         231.64KB java.util.HashMap$Node[]
 162.89KB sun.security.provider           205.72KB java.lang.String[]
 154.35KB java.math                       195.61KB j.u.c.ConcurrentHashMap$Node
   3.22MB for 190 more packages             1.84MB for 1076 more object types
-----------------------------------------------------------------------------------
      1.1s (3.3% of total time) in 19 GCs | Peak RSS: 4.89GB | CPU load: 6.93
-----------------------------------------------------------------------------------
Produced artifacts:
${HOME}/graalvm22.2-reachability/child-module/target/native-tests (executable)
${HOME}/graalvm22.2-reachability/child-module/target/native-tests.build_artifacts.txt (txt)
===================================================================================
Finished generating 'native-tests' in 32.2s.
[INFO] Executing: ${HOME}/graalvm22.2-reachability/child-module/target/native-tests --xml-output-dir ${HOME}/graalvm22.2-reachability/child-module/target/native-test-reports -Djunit.platform.listeners.uid.tracking.output.dir=${HOME}/graalvm22.2-reachability/child-module/target/test-ids
JUnit Platform on Native Image - report
----------------------------------------

**********VALUE of USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM************
null
com.example.MySampleTest > testSample FAILED


Failures (1):
  JUnit Vintage:MySampleTest:testSample
    MethodSource [className = 'com.example.MySampleTest', methodName = 'testSample', methodParameterTypes = '']
    => java.lang.RuntimeException: Generated message class "com.example.MySampleClass" missing method "getName".
       com.anotherpackage.GeneratedMessage.retrieveMethod(GeneratedMessage.java:31)
       com.anotherpackage.GeneratedMessage.access$000(GeneratedMessage.java:5)
       com.anotherpackage.GeneratedMessage$MethodAccessor.<init>(GeneratedMessage.java:20)
       com.anotherpackage.GeneratedMessage$A.initializeMethodAccessor(GeneratedMessage.java:11)
       com.example.MySampleClass.invokeAccessor(MySampleClass.java:13)
       [...]
     Caused by: java.lang.NoSuchMethodException: com.example.MySampleClass.getName()
       java.base@11.0.18/java.lang.Class.getMethod(DynamicHub.java:2108)
       com.anotherpackage.GeneratedMessage.retrieveMethod(GeneratedMessage.java:28)
       [...]
```

## GraalVM 23 Dev version
https://github.com/oracle/graal/issues/5194 was fixed in GraalVM 23.
Without `-H:-RunReachabilityHandlersConcurrently`, the build succeeds. 
With `-H:-RunReachabilityHandlersConcurrently`, we end up getting the following error:

```
========================================================================================================================
GraalVM Native Image: Generating 'native-tests' (executable)...
========================================================================================================================
[1/8] Initializing...                                                                                    (5.4s @ 0.16GB)
 Version info: 'GraalVM 23.0.0-dev Java 17.0.7+4-jvmci-23.0-b09 CE'
 Java version info: '17.0.7+4-jvmci-23.0-b09'
 Graal compiler: optimization level: '2', target machine: 'x86-64-v3'
 C compiler: gcc (linux, x86_64, 12.2.0)
 Garbage collector: Serial GC (max heap size: 80% of RAM)
 2 user-specific feature(s)
 - com.example.MyNativeImageFeature
 - org.graalvm.junit.platform.JUnitPlatformFeature
[junit-platform-native] Running in 'test listener' mode using files matching pattern [junit-platform-unique-ids*] found in folder [/usr/local/google/home/mpeddada/IdeaProjects/native-image-experiments/graalvm22.2-reachability/child-module/target/test-ids] and its subfolders.
[2/8] Performing analysis...  []                                                                         (6.5s @ 0.63GB)
   3,945 (81.41%) of  4,846 types reachable
   4,858 (52.95%) of  9,175 fields reachable
  17,849 (59.37%) of 30,064 methods reachable
   1,103 types,    95 fields, and   257 methods registered for reflection

------------------------------------------------------------------------------------------------------------------------
                        0.5s (3.8% of total time) in 16 GCs | Peak RSS: 1.67GB | CPU load: 10.19
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /usr/local/google/home/mpeddada/IdeaProjects/native-image-experiments/graalvm22.2-reachability/child-module/target/svm_err_b_20230411T204553.245_pid1486560.md (build_info)
========================================================================================================================
Failed generating 'native-tests' after 12.3s.

The build process encountered an unexpected error:

java.lang.NullPointerException: Cannot invoke "java.util.Set.iterator()" because "triggers" is null
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.ReachabilityHandlerFeature.processReachable(ReachabilityHandlerFeature.java:173)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.ReachabilityHandlerFeature.duringAnalysis(ReachabilityHandlerFeature.java:128)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGenerator.lambda$runPointsToAnalysis$10(NativeImageGenerator.java:768)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.FeatureHandler.forEachFeature(FeatureHandler.java:86)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGenerator.lambda$runPointsToAnalysis$11(NativeImageGenerator.java:768)
	at org.graalvm.nativeimage.pointsto/com.oracle.graal.pointsto.AbstractAnalysisEngine.runAnalysis(AbstractAnalysisEngine.java:179)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGenerator.runPointsToAnalysis(NativeImageGenerator.java:765)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGenerator.doRun(NativeImageGenerator.java:580)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGenerator.run(NativeImageGenerator.java:537)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGeneratorRunner.buildImage(NativeImageGeneratorRunner.java:408)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGeneratorRunner.build(NativeImageGeneratorRunner.java:612)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGeneratorRunner.start(NativeImageGeneratorRunner.java:134)
	at org.graalvm.nativeimage.builder/com.oracle.svm.hosted.NativeImageGeneratorRunner.main(NativeImageGeneratorRunner.java:94)

com.oracle.svm.driver.NativeImage$NativeImageError
	at org.graalvm.nativeimage.driver/com.oracle.svm.driver.NativeImage.showError(NativeImage.java:2035)
	at org.graalvm.nativeimage.driver/com.oracle.svm.driver.NativeImage.build(NativeImage.java:1655)
	at org.graalvm.nativeimage.driver/com.oracle.svm.driver.NativeImage.performBuild(NativeImage.java:1614)
	at org.graalvm.nativeimage.driver/com.oracle.svm.driver.NativeImage.main(NativeImage.java:1588)
```
