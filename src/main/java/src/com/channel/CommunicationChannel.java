package src.com.channel;

import src.com.messages.Message;

import java.util.concurrent.BlockingQueue;

public class CommunicationChannel {
    private BlockingQueue<Message> requestQueue;
    private BlockingQueue<Message> callbackQueue;
    private Boolean hasInventory = true;

    public CommunicationChannel(BlockingQueue<Message> requestQueue, BlockingQueue<Message> callbackQueue) {
        this.requestQueue = requestQueue;
        this.callbackQueue = callbackQueue;
    }

    public BlockingQueue<Message> getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(BlockingQueue<Message> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public BlockingQueue<Message> getCallbackQueue() {
        return callbackQueue;
    }

    public void setCallbackQueue(BlockingQueue<Message> callbackQueue) {
        this.callbackQueue = callbackQueue;
    }

    public Boolean getHasInventory() {
        return hasInventory;
    }

    public void setHasInventory(Boolean hasInventory) {
        this.hasInventory = hasInventory;
    }
}
