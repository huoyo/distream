package cn.langpy.core;


public interface DataHandlerInterface<E> extends FunctionalInterface{
    E handle(E line);
}
