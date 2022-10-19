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
3) `cd child-project`.
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
