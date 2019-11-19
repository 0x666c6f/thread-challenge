package src.com.channel;

import src.com.messages.Message;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class CommunicationChannel {
    private BlockingQueue<Message> requestQueue;
    private HashMap< String, BlockingQueue<Message>> callbackQueues;
    private Boolean hasInventory = true;

    public CommunicationChannel(BlockingQueue<Message> requestQueue, HashMap< String, BlockingQueue<Message>> callbackQueues) {
        this.requestQueue = requestQueue;
        this.callbackQueues = callbackQueues;
    }

    public BlockingQueue<Message> getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(BlockingQueue<Message> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public HashMap< String, BlockingQueue<Message>> getCallbackQueues() {
        return callbackQueues;
    }

    public void setCallbackQueues(HashMap< String, BlockingQueue<Message>> callbackQueues) {
        this.callbackQueues = callbackQueues;
    }

    public Boolean getHasInventory() {
        return hasInventory;
    }

    public void setHasInventory(Boolean hasInventory) {
        this.hasInventory = hasInventory;
    }
}
