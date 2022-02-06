package cn.langpy.core;

/**
 * @name：
 * @function：
 * @author：zhangchang
 * @date 2022/1/28 13:27
 */
public interface DataHandlerInterface<E> extends FunctionalInterface{
    E handle(E line);
}
