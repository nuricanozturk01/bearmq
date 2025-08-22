package com.bearmq.client.config;

import com.bearmq.client.Names;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@ComponentScan(Names.BEAN_NAME)
@EnableConfigurationProperties({
        BearConfig.class,
        BearRetryConfig.class,
})
@SuppressWarnings("unused")
public @interface EnableBear {
}
