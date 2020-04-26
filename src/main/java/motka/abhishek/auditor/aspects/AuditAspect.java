package motka.abhishek.auditor.aspects;

import motka.abhishek.auditor.annotations.DoNotAudit;
import motka.abhishek.auditor.annotations.Mask;
import motka.abhishek.auditor.interfaces.LogAppender;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Aspect
@Configurable(autowire = Autowire.BY_TYPE)
public class AuditAspect
{

    private final Logger logger = Logger.getLogger(AuditAspect.class.getName());

    private static final String MASKED_PARAM = "**********";

    private List<Class<?>> primitiveTypes = Arrays.asList(Integer.class,
                                                          Short.class,
                                                          Byte.class,
                                                          Long.class,
                                                          Float.class,
                                                          Double.class,
                                                          Boolean.class,
                                                          String.class,
                                                          Character.class);
    @Autowired(required = false)
    private List<LogAppender> logAppenders;

    @Before("execution(* (@motka.abhishek.auditor.annotations.Audit *).*(..))")
    public void logMethodEntry(JoinPoint joinPoint)
    {
        Method method = getJoinPointMethod(joinPoint);
        if (Objects.nonNull(method.getAnnotation(DoNotAudit.class))) return;

        String entryMethodLog = new StringBuilder(getAppendedLogs())
                .append("Entering >> ")
                .append(getFullyQualifiedMethodName(joinPoint))
                .append(" ")
                .append(getParamsAsString(joinPoint))
                .toString();
        logger.info(entryMethodLog);
    }

    @AfterReturning(pointcut = "execution(* (@motka.abhishek.auditor.annotations.Audit *).*(..))", returning = "retValue")
    public void logMethodExit(JoinPoint joinPoint, Object retValue) {
        Method method = getJoinPointMethod(joinPoint);
        if (Objects.nonNull(method.getAnnotation(DoNotAudit.class))) return;

        String exitMethodLog = new StringBuilder(getAppendedLogs())
                .append("Exiting << ")
                .append(getFullyQualifiedMethodName(joinPoint))
                .append(" ")
                .append("Returned: ")
                .append(getReturnValueAsString(method, retValue))
                .toString();
        logger.info(exitMethodLog);
    }

    @AfterThrowing(pointcut = "execution(* (@motka.abhishek.auditor.annotations.Audit *).*(..))", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, final Throwable ex) {
        Method method = getJoinPointMethod(joinPoint);
        if (Objects.nonNull(method.getAnnotation(DoNotAudit.class))) return;

        String methodExceptionLog = new StringBuilder(getAppendedLogs())
                .append("Exiting << ")
                .append(getFullyQualifiedMethodName(joinPoint))
                .append(" ")
                .append("Exception: ")
                .append(getExceptionAsString(ex))
                .toString();

        logger.info(methodExceptionLog);
    }

    private String getExceptionAsString(final Throwable throwable) {
        StringBuilder exceptionBuilder = new StringBuilder(throwable.getClass().getName())
                .append(" Message: ")
                .append(throwable.getMessage());
        Arrays.stream(throwable.getStackTrace()).forEach(ste -> exceptionBuilder.append("\n\t\t\t").append(ste.toString()));
        return exceptionBuilder.toString();
    }

    private String getAppendedLogs() {
        StringBuilder appendedLogBuilder = new StringBuilder("");
        if (Objects.isNull(logAppenders) || logAppenders.isEmpty()) return appendedLogBuilder.toString();
        appendedLogBuilder.append("[ ");
        logAppenders.stream().forEach(la -> appendedLogBuilder.append(la.appendToLog()).append(" "));
        return appendedLogBuilder.append("] ").toString();
    }

    private String getClassName(JoinPoint joinPoint){
        return getClass(joinPoint).getName();
    }

    private Class<?> getClass(final JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass();
    }

    private String getMethodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getName();
    }

    private String getFullyQualifiedMethodName(JoinPoint joinPoint) {
        return getClassName(joinPoint) + "." +  getMethodName(joinPoint);
    }

    private Method getJoinPointMethod(final JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod();
    }

    private String getParamsAsString(final JoinPoint joinPoint) {
        Method method = getJoinPointMethod(joinPoint);
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Parameter[] parameters = method.getParameters();
        Object[] arguments = joinPoint.getArgs();

        StringBuilder paramBuilder = new StringBuilder("Arguments: { ");
        for (int i = 0; i < arguments.length; ++i) {
            paramBuilder.append(
                    getParamAsString(
                            parameters[i].getName(),
                            arguments[i],
                            paramAnnotations[i]
                    )
                ).append(" ");
        }
        paramBuilder.append("}");
        return paramBuilder.toString();
    }

    private String getParamAsString(String paramName, Object argument, Annotation[] paramAnnotations) {
        boolean isMasked = false;
        Mask maskedAnnotation = null;

        for (Annotation annotation : paramAnnotations) {
            if (annotation instanceof DoNotAudit) return "";
            if (annotation instanceof Mask) {
                maskedAnnotation = (Mask) annotation;
                isMasked =  true;
                break;
            }
        }

        return new StringBuilder(paramName)
                .append("=")
                .append(getParamValueAsString(argument, isMasked, maskedAnnotation))
                .toString();
    }

    private boolean isPrimitive(Object o) {
        return Objects.isNull(o) || primitiveTypes.contains(o.getClass());
    }

    private String getParamValueAsString(Object argument, boolean isMasked, Mask maskedAnnotation) {
        return isMasked ? getMaskedParamAsString(argument, maskedAnnotation) : getParamAsString(argument);
    }

    private String getMaskedParamAsString(Object argument, Mask maskedAnnotation) {
        return isPrimitive(argument)
                ? MASKED_PARAM
                : ReflectionToStringBuilder.toStringExclude(
                        argument,
                        Arrays.asList(maskedAnnotation.value())
                );
    }

    private String getParamAsString(Object o) {
        return isPrimitive(o) ? o + "" : ToStringBuilder.reflectionToString(o);
    }

    private String getReturnValueAsString(Method method, Object returnVal) {
        Mask maskedAnnotation = method.getAnnotatedReturnType().getAnnotation(Mask.class);

        return Objects.isNull(maskedAnnotation)
                ? getParamAsString(returnVal)
                : getMaskedParamAsString(returnVal, maskedAnnotation);
    }
}
