package cn.langpy.core;


import java.lang.annotation.Annotation;


public interface DataHandler<E> extends DataHandlerInterface<E>{

    E handle(E line);

    @Override
    default Class<? extends Annotation> annotationType() {
        return null;
    }
}
