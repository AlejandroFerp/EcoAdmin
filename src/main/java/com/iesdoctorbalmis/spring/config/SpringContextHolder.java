package com.iesdoctorbalmis.spring.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Proporciona acceso estático al ApplicationContext de Spring.
 * Necesario para que los EntityListeners de JPA (ejecutados fuera del contexto normal)
 * puedan acceder a beans como CodigoService.
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.ctx = applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        return ctx.getBean(type);
    }
}
