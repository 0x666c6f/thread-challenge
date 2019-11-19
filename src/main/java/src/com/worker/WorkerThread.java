package src.com.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.com.channel.CommunicationChannel;
import src.com.messages.Message;
import src.com.messages.MessageSide;

import java.util.*;
import java.util.concurrent.BlockingQueue;


public class WorkerThread implements Runnable {

    private ArrayList<String> stack;
    private HashMap<String, CommunicationChannel> channels;
    private BlockingQueue<Message> requestQueue;
    private String target;
    private String name;
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    public WorkerThread(String name, ArrayList<String> stackArg, HashMap<String, CommunicationChannel> channels, BlockingQueue<Message> requestQueue) {
        this.name = name;
        this.stack = stackArg;
        this.channels = channels;
        this.requestQueue = requestQueue;
        logger.info("Initializing Worker Thread " + name);
        processMajorColor();
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


    @Override
    public void run() {
        logger.info("Starting Worker Thread " + name);
        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean exit = false;
        while (!exit) {

            Message incomingMessage = null;
            while (requestQueue.size() > 0) {
                incomingMessage = requestQueue.poll();

                if (incomingMessage != null && incomingMessage.getThreadName() != null) {

                    processIncomingRequest(incomingMessage);
                }
            }


            for (Map.Entry<String, CommunicationChannel> entry : channels.entrySet()) {
                String threadName = entry.getKey();
                CommunicationChannel channel = entry.getValue();

                //TODO : tester entry sur key et value != null
                processOutgoingRequest(threadName, channel);
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
        Thread.currentThread().interrupt();
    }


    private void processIncomingRequest(Message incomingMessage) {

        String threadNameI = incomingMessage.getThreadName();

        Message response = new Message();

        if (incomingMessage.getSide() == MessageSide.REQUEST && threadNameI != null) {
            logger.info("Incoming request from " + threadNameI + " = " + incomingMessage.toString());

            response.setRequestId(incomingMessage.getRequestId());
            response.setThreadName(incomingMessage.getThreadName());
            response.setSide(MessageSide.RESPONSE);
            response.setBall(incomingMessage.getBall());

            if (incomingMessage.getBall().equals(this.target)) {
                logger.info("Incoming request from " + threadNameI + " is rejected because it is our target");
                response.setResponse(false);
            } else if (!stack.contains(incomingMessage.getBall())) {
                logger.info("Incoming request from " + threadNameI + " is rejected because we don't have it");
                response.setResponse(null);
            } else {
                logger.info("Incoming request from " + threadNameI + " is not on our target, accepting it");

                if (!this.stack.remove(incomingMessage.getBall())) {
                    logger.error("Couldn't remove " + incomingMessage.getBall() + " from our stack " + this.stack.toString());
                    response.setResponse(null);
                } else {
                    logger.info("Sucessfully removed " + incomingMessage.getBall() + " from our stack " + this.stack.toString());
                    response.setResponse(true);
                }
            }

            try {
                channels.get(threadNameI).getCallbackQueues().get(threadNameI).put(response);
            } catch (InterruptedException | NullPointerException e) {
                logger.error("Error while trying to put response : " + e.getMessage());
            }
        }

    }

    private void processOutgoingRequest(String threadName, CommunicationChannel channel) {

        Message message = new Message(target, MessageSide.REQUEST, this.name);

        if (channel.getCallbackQueues().get(this.name).size() == 0) {
            Boolean result = channel.getRequestQueue().offer(message);
            if (!result) {
                return;
            }
        } else {

            Message response = null;
            try {
                response = channel.getCallbackQueues().get(this.name).poll();
            } catch (NullPointerException e) {
                logger.error("Error while trying to poll response : " + e.getMessage());
            }

            if (response != null) {

                if (response.getResponse() != null && target.equals(response.getBall())) {

                    if (response.getResponse() == true) {
                        logger.info("Request was accepted from " + threadName);
                        this.stack.add(target);
                    } else if (response.getResponse() == false) {
                        logger.warn("Request was rejected from " + threadName);
                        reprocessTarget();
                    }
                } else {
                    logger.warn("Response was null from " + threadName + " : " + response.toString());
                }
            }
        }
    }
}
