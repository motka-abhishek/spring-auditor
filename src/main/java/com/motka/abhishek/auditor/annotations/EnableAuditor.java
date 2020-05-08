package com.motka.abhishek.auditor.annotations;

import com.motka.abhishek.auditor.configs.AuditorAspectConfig;
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
