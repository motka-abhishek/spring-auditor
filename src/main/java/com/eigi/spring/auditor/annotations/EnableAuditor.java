package com.eigi.spring.auditor.annotations;

import com.eigi.spring.auditor.configs.AuditorAspectConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AuditorAspectConfig.class)
public @interface EnableAuditor {
}