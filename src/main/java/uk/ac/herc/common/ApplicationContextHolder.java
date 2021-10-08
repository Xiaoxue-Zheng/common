package uk.ac.herc.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {
    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    public static<T> T getBean(Class<T> tClass){
        return context.getBean(tClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        context=context;
    }
}
