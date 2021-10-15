package uk.ac.herc.common.security.mf;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {

    private static final ApplicationContextHolder INSTANCE = new ApplicationContextHolder();

    private ApplicationContext context;


    /**
     * This method will throw beanNotFound exception, when the there is not a bean configured in the app.
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> tClass) {
        return INSTANCE.context.getBean(tClass);
    }

    public static<T> T getBeanIfExist(Class<T> tClass){
        try{
            return INSTANCE.context.getBean(tClass);
        }catch (Exception e){
            //the bean may not configured
            return null;
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext context) {
        INSTANCE.context = context;
    }
}
