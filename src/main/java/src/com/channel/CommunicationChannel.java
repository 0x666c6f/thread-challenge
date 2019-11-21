package src.com.channel;

import src.com.messages.Message;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Class regrouping the different queues used to communicate between threads. One thread will have one {@code CommunicationChannel} per thread
 * It will contain the request queue of the targeted thread, and the callbacks that we have with it.
 * The callbacks are directional, we will then have 2 callbacks for a specific thread.
 * In the callbacks map, the callback we will listen will be the one with the key matching our thread name.
 * Example:
 *  Let's say we have Thread A and Thread B
 *  Channel for A to B will be :
 *  ChannelAB{
 *      requestQueueB
 *      Map{
 *          Thread A , CallbackBtoA
 *          Thread B , CallbackAtoB
 *      }
 *  }
 *
 * To send a request from A to B, we will send it on requestQueueB
 * To listen for the response, we will listen on CallbackBtoA
 *
 */
public class CommunicationChannel {
    private final BlockingQueue<Message> requestQueue;
    private final HashMap< String, BlockingQueue<Message>> callbackQueues;

    /**
     * Constructor of the communication channel we have for a specific thread
     *
     * @param  requestQueue
     *         {@code BlockingQueue<Message>} representing the queue the targeted thread will listen to to receive the incoming requests
     * @param  callbackQueues
     *         {@code HashMap<String, BlockingQueue<Message>>} representing the different callback queues we have with the targeted thread.
     */
    public CommunicationChannel(BlockingQueue<Message> requestQueue, HashMap< String, BlockingQueue<Message>> callbackQueues) {
        this.requestQueue = requestQueue;
        this.callbackQueues = callbackQueues;
    }

    public BlockingQueue<Message> getRequestQueue() {
        return requestQueue;
    }

    public HashMap< String, BlockingQueue<Message>> getCallbackQueues() {
        return callbackQueues;
    }

}
