# spring-auditor
Logging library for Spring Applications.

## Getting Started

Logging is the primary requirement for all Production Application. Almost in all our Spring Applications we implement Logging with help of Aspect Oriented Programming and we end up duplicating the same code in all applications. It inspired me to create a small library which leverages Spring AOP and Logback for Logging in Spring Application.

Any Spring Application which requires logging can just add this library as dependency in their Maven project and start using it. This library will log entry to all methods with arguments passed and exit of all methods with value returned or exception if thrown. It also gives you flexibility to exclude methods or parameter from being logged or mask critical information.

## Add spring-auditor In Your Application

In your project's pom.xml file add dependency for spring-auditor:

```$xslt
<dependency>
    <groupId>com.motka.abhishek</groupId>
    <artifactId>auditor</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Enable Logging with Auditor

* Add `@EnableAuditor` annotation on any of your Spring Configuration class (class annotated with Spring `@Configuration` annotation).
* Annotate any spring component whose methods you want to log with `@Audit` annotation
    * If you want to log only specific methods of a class then you can annotate individual method with `@Audit` annotation to log.
* You have option to exclude any method or any parameter of any method from Logging
    * To exclude any method from logging annotate that method with `@DoNotAudit` annotation.
    * To exclude any parameter from logging annotate parameter of method with `@DoNotAudit` annotation.
* To mask any critical information annotate that parameter or return type with `@Mask` annotation
    * If masked return value or parameter is of primitive type (Integer, Short, Byte, Long, Float, Double,  Boolean, String, Character) then value of it will be masked with `*******`.
    * If masked return value or parameter is not of primitive type then all the fields mentioned as value of `@Mask({field1, field2})` will be excluded from logs.
    
## Logging Format

Once you enable the Spring-Auditor for your application, 2 filed will be created in logs folder in current directory with below naming convention:

```$xslt
1. audit.<hostname>.<date>.log
2. error.<hostname>.<date>.log
```    

All entry and exit of the method will be logged in audit.\*.log file and all exceptions thrown by the application will be logged in error.\*.log file.

Logs in the above files have following format:

```$xslt
2020-05-08 12:33:50,930 IMLMDVABHISHEKPA INFO  http-nio-8080-exec-1 AuditAspect [ auditor-test ] - Entering >>> motka.abhishek.auditortest.TestObject.print Arguments: { name=abhishek }
2020-05-08 12:33:50,938 IMLMDVABHISHEKPA INFO  http-nio-8080-exec-1 AuditAspect [ auditor-test ] - Entering >>> motka.abhishek.auditortest.TestService.hello Arguments: { name=abhishek }
2020-05-08 12:33:50,942 IMLMDVABHISHEKPA INFO  http-nio-8080-exec-1 AuditAspect [ auditor-test ] - Exiting <<< motka.abhishek.auditortest.TestService.hello Returned: Hello abhishek
2020-05-08 12:33:50,943 IMLMDVABHISHEKPA ERROR http-nio-8080-exec-1 com.eigi.spring.auditor.errorLogger [ auditor-test ] - Exiting <<< motka.abhishek.auditortest.TestObject.print Exception: java.lang.NullPointerException Message: Empty name
			motka.abhishek.auditortest.TestObject.print(TestObject.java:22)
			motka.abhishek.auditortest.TestObject$$FastClassBySpringCGLIB$$3c53d81a.invoke(<generated>)
			org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
			org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:771)
			org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
			org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749)
			org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:56)
			org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
			org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749)
			org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke(AfterReturningAdviceInterceptor.java:55)
			org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
			org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:749)
			org.springframework.aop.aspectj.AspectJAfterThrowingAdvice.invoke(AspectJAfterThrowingAdvice.java:62)
			...
```

This can be generalized as:

`<Time Stamp> <Hostname> <Log Level> <Thread> <Logger> [ <Application Name> ] - [ <Log Appenders> ] <Message>`

## Log Appenders

Often we need to customize our logs and required to add extra details like TraceId or application specific details. Spring-Auditor provides LogAppender interface for the same purpose. If you want to add extra details to logs then you need to create a spring bean which implements LogAppender interface and return those extra details from `appendLog()` method of the interface.

Here is a sample LogAppender which adds trace id to the logs:

```$xslt
@Component
public class TestAppender implements LogAppender
{
    @Override
    public String appendToLog()
    {
        return UUID.randomUUID().toString();
    }
}
```

After you add log appender, the logs will look like below:

`2020-05-08 12:42:30,244 IMLMDVABHISHEKPA INFO  http-nio-8080-exec-1 AuditAspect [ auditor-test ] - [ bc82bce0-f71e-4e55-9613-b045cc378552 ] Entering >>> motka.abhishek.auditortest.TestObject.print Arguments: { name=abhishek }
`

In above log you can see Trace Id added: `[ bc82bce0-f71e-4e55-9613-b045cc378552 ]`.

## Improvements

Currently this library is limited to spring components only. Spring-Auditor can't log method executions of simple POJO.

If you are calling one method from another method of the same class then method call of same class method won't be logged.

For example:

```$xslt
@Service
@Audit
public class TestService
{
    public String hello(String name) {
        printHello(name);
        return "Hello " + name;
    }
    
    public String printHello(String name) {
        System.out.println(name);
        return name;
    }
}
```
In this example method call for `hello()` will be logged but `printHello()` won't be logged. This is the limitation due to Spring AOP's proxy based implementation.

We can use AspectJ with LTW or CTW to apply logging to POJO and solve same class method logging issue as well. Currently this feature is in progress.