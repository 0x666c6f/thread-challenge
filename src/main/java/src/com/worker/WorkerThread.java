package src.com.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.com.channel.CommunicationChannel;
import src.com.messages.Message;
import src.com.messages.MessageSide;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class WorkerThread implements Runnable {

    private ArrayList<String> stack;
    private HashMap<String, CommunicationChannel> channels;
    private BlockingQueue<Message> requestQueue;
    private String target;
    private String name;
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    public WorkerThread(String name, ArrayList<String> stackArg, HashMap<String, CommunicationChannel> channels, BlockingQueue<Message> requestQueue) {
        this.stack = stackArg;
        this.channels = channels;
        this.name = name;
        this.requestQueue = requestQueue;
        logger.info("Initializing Worker Thread " + name);
        processMajorColor();
    }


    @Override
    public void run() {
        logger.info("Starting Worker Thread " + name);

        boolean exit = false;
        while (!exit) {

            Message incomingMessage = null;
            while (requestQueue.size() > 0) {
                try {
                    incomingMessage = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (incomingMessage != null) {
                    String threadName = incomingMessage.getThreadName();
                    BlockingQueue<Message> callback = channels.get(threadName).getCallbackQueue();

                    logger.info("Incoming request from " + threadName + " = " + incomingMessage.toString());
                    processIncomingRequest(threadName, callback, incomingMessage);
                }
            }

            for (Map.Entry<String, CommunicationChannel> entry : channels.entrySet()) {
                String threadName = entry.getKey();
                CommunicationChannel channel = entry.getValue();

                if (channel.getRequestQueue() != null ) {
                    Message result = processOutgoingRequest(threadName, channel);
                    if (result != null) {
                        if(result.getResponse() != null && result.getResponse() == false) {
                            logger.info("Got rejects on target " + target + " from " + threadName);
                            reprocessTarget();
                            channel.setHasInventory(true);
                        } else if(result.getResponse() == null){
                            channel.setHasInventory(false);
                        }

                    }
                }
            }
            Set<String> targetSet = new HashSet<String>(stack);
            logger.info("Current stack :" + stack.toString());
            logger.info("Current set :" + targetSet.toString());

            if (targetSet.size() == 1 && stack.size() == 10) {
                logger.info(this.name.toUpperCase() + " FINISHED");
                exit = true;
            }
        }


        logger.info(this.name.toUpperCase() + " EXITED");
        this.requestQueue = null;
        Thread.currentThread().interrupt();
    }

    private void processMajorColor() {
        int greenTotal = 0;
        int redTotal = 0;
        int blueTotal = 0;
        for (String ball : stack) {
            switch (ball) {
                case "G":
                    greenTotal++;
                    break;
                case "R":
                    redTotal++;
                    break;
                case "B":
                    blueTotal++;
                    break;
            }
        }

        if (blueTotal >= greenTotal && blueTotal >= redTotal) {
            target = "B";
        } else if (greenTotal >= blueTotal && greenTotal >= redTotal) {
            target = "G";
        } else {
            target = "R";
        }

        logger.info("Major color from stack is = " + target);

    }

    private void reprocessTarget() {

        logger.info("Switching target from = " + target);

        switch (target) {
            case "R":
                target = "G";
                break;
            case "G":
                target = "B";
                break;
            case "B":
                target = "R";
                break;
        }

        logger.info("New target is = " + target);

    }


    private void processIncomingRequest(String threadName, BlockingQueue<Message> callback, Message incomingMessage) {
        Message response = new Message();
        if (incomingMessage.getSide() == MessageSide.REQUEST) {
            response.setBall(incomingMessage.getBall());
            response.setSide(MessageSide.RESPONSE);
            response.setRequestId(incomingMessage.getRequestId());
            response.setThreadName(incomingMessage.getThreadName());
            if (incomingMessage.getBall().equals(target)) {
                logger.info("Incoming request from " + threadName + " is rejected because it is our target");

                response.setResponse(false);
            } else if (!stack.contains(incomingMessage.getBall())) {
                logger.info("Incoming request from " + threadName + " is rejected because we don't have it");

                response.setResponse(null);
            } else {
                logger.info("Incoming request from " + threadName + " is not on our target, accepting it");

                Boolean removed = this.stack.remove(incomingMessage.getBall());
                if(!removed){
                    logger.error("Couldn't remove " + incomingMessage.getBall() + " from our stack " + this.stack.toString());
                    response.setResponse(null);
                } else {
                    logger.info("Sucessfully removed " + incomingMessage.getBall() + " from our stack " + this.stack.toString());
                    response.setResponse(true);
                }
            }
            callback.offer(response);
        }

    }

    private Message processOutgoingRequest(String threadName, CommunicationChannel channel) {
        //Ask for a ball from majority

        Message message = new Message(target, MessageSide.REQUEST, this.name);

        if(channel.getCallbackQueue().size() == 0){
            Boolean result = channel.getRequestQueue().offer(message);
            if (!result) {
                return null;
            }
            logger.info("Succesfully sent request to " + threadName);
        }

        Message response = null;
        try {
            response = channel.getCallbackQueue().poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (response != null) {
            //logger.info("Succesfully received request response from " + threadName);

            if (response.getResponse() != null && target.equals(response.getBall()) && message.getRequestId().equals(response.getRequestId())) {

                if (response.getResponse() == true) {
                    logger.info("Request was accepted from " + threadName);
                    this.stack.add(target);
                } else if (response.getResponse() == false) {
                    logger.warn("Request was rejected from " + threadName);
                }

            } else {
                logger.warn("Request was null from " + threadName + " : " + response.toString());
            }

        } else {
            logger.warn("Response was null from " + threadName);
            channel.getRequestQueue().remove(message);
            logger.info("Removed message from queue");
        }

        return response;
    }


}
