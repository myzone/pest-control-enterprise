package com.pestcontrolenterprise.util;


/**
 * @author myzone
 * @date 5/24/14
 */
public interface PartialSupplier<V, E extends Throwable> {

    V get() throws E;

}
