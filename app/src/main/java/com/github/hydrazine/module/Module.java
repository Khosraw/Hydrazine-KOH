package com.github.hydrazine.module;

/**
 * @author xTACTIXzZ
 * <p>
 * This interface represents a module
 */
public interface Module extends Runnable {

    String getModuleName();

    String getDescription();

    void start();

    void stop(String cause);

    void configure();

}
