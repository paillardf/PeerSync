package com.peersync.network.content.transfer;

/**
 * Interface used to notify of interesting events emminating from
 * the ActiveTransferTracker.
 */
public interface SyncActiveTransferTrackerListener {

    /**
     * Called when a new client session is created.
     * 
     * @param transfer the new session
     */
    void sessionCreated(SyncActiveTransfer transfer);

    /**
     * Called when an existing client session is garbage
     * collected due to lack of use.
     * 
     * @param transfer the idle session
     */
    void sessionCollected(SyncActiveTransfer transfer);

}
