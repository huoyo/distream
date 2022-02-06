package cn.langpy.core;


import java.lang.annotation.Annotation;

/**
 * @name：
 * @function：
 * @author：zhangchang
 * @date 2022/1/28 13:27
 */
public interface DataHandler<E> extends DataHandlerInterface<E>{

    E handle(E line);

    @Override
    default Class<? extends Annotation> annotationType() {
        return null;
    }
}
