package com.pestcontrolenterprise;

/**
 * @author myzone
 * @date 4/25/14
 */
public interface UserSession extends AutoCloseable {

    User getUser();

    /**
     * @todo consider to move this method to WorkerSession because for AdminSession it won`t work
     */
    void changePassword(String newPassword) throws IllegalStateException;

    @Override
    void close() throws IllegalStateException;

}
