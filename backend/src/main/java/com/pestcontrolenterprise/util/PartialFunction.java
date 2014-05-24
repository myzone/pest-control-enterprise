package com.pestcontrolenterprise.util;


/**
 * @author myzone
 * @date 5/24/14
 */
public interface PartialFunction<A, R, E extends Throwable> {

    R apply(A argument) throws E;

}
