package com.telekom.m2m.cot.restsdk.realtime;

/**
 * A SubscriptionListener receives real time notifications to which it was subscribed to.
 */
public interface SubscriptionListener {

    /**
     * This method will be called by the {@link CepConnector} for each matching notification that it receives.
     *
     * @param channel the channel that the listener subscribed to.
     * @param notification the {@link Notification} that contains all the matching response lines.
     */
    void onNotification(String channel, Notification notification);


    /**
     * If the CepConnector receives an error then it will pass it on to all it's listeners by calling this method.
     *
     * @param channel name of the channel where the error happened.
     * @param error the {@link Throwable} that contains the error.
     */
    void onError(String channel, Throwable error);

}
