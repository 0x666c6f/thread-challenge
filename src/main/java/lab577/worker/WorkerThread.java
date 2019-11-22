package lab577.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lab577.channel.CommunicationChannel;
import lab577.messages.Message;
import lab577.messages.MessageSide;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementation of the thread algorithm and logic. This runnable will be given to the thread, so each thread will have the same algorithm to solve this problem
 */
public class WorkerThread implements Runnable {

    private final ArrayList<String> stack;
    private final HashMap<String, CommunicationChannel> channels;
    private final BlockingQueue<Message> requestQueue;
    private String target;
    private final String name;
    private final HashMap<String, Message> pendingRequests;
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    private final ConcurrentHashMap<String, String> exitedThreads;

    /**
     * Constructor of the runnable for the thread
     *
     * @param name          {@code String} representing the name of the current thread
     * @param stackArg      {@code ArrayList<String>} representing the inventory of the balls we own
     * @param channels      {@code HashMap<String, CommunicationChannel>} representing the different Communication channels we have with the different threads.
     * @param requestQueue  {@code BlockingQueue<Message>} representing the queue we will listen to to receive the incoming requests
     * @param exitedThreads {@code ConcurrentHashMap<String,String>} representing the exited threads, and the ball they exited with. This map is shared between all the threads
     * @see CommunicationChannel
     */
    public WorkerThread(String name, ArrayList<String> stackArg, HashMap<String, CommunicationChannel> channels, BlockingQueue<Message> requestQueue, ConcurrentHashMap<String, String> exitedThreads) {
        this.name = name;
        this.stack = stackArg;
        this.channels = channels;
        this.requestQueue = requestQueue;
        this.pendingRequests = new HashMap<>();
        this.exitedThreads = exitedThreads;
        logger.info("Initializing Worker Thread " + name);
        processMajorColor();
    }

    /**
     * Process the major color based on our ball inventory. The target will be the one with the highest number.<br>
     * If Green and Red totals are equals, the one selected as highest will be Red. Once calculation is done, it will set our class target with the highest color.
     */
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

    /**
     * Process the new target to select based on our former target. It will then set our class target with this value.
     */
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

        //If we switched target and now we have a target that is from a thread already completed, we need to switch to avoid being blocked
        if (exitedThreads.containsValue(target)) {
            reprocessTarget();
        }
        logger.info("New target is = " + target);
    }


    /**
     * Main algorithm of the thread defining its behavior.<br>
     * To avoid interlocking on start up, we define a random delay before starting.<br>
     * <p>
     * Until the thread has only one type of ball, and all of them, it will do the following:<br>
     * <ol>
     *  <li>Process all incoming requests until its queue is empty</li>
     *  <li>For each thread communication channel it has, it will either send a request to it, or manage its response</li>
     *  <li>Check if the stopping condition is met</li>
     *  <li>When stopping, it will clear its request and callback queues to avoid blocking the other threads</li>
     *  <li>It will then exit</li>
     * </ol>
     */
    @Override
    public void run() {
        logger.info("Starting Worker Thread " + name);
        try {
            //Defining random delay for startup to avoid interlocking
            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean exit = false;
        while (!exit) {

            Message incomingMessage;
            //Checking if we have incoming messages
            while (requestQueue.size() > 0) {
                incomingMessage = requestQueue.poll();
                if (incomingMessage != null && incomingMessage.getThreadName() != null) {
                    //Process a message if we successfully polled it
                    processIncomingRequest(incomingMessage);
                }
            }

            //Processing outgoing messages and responses for each channel we have
            for (Map.Entry<String, CommunicationChannel> entry : channels.entrySet()) {
                String threadName = entry.getKey();
                CommunicationChannel channel = entry.getValue();

                processOutgoingRequest(threadName, channel);
            }

            //Check the unique entry set of our stack
            Set<String> targetSet = new HashSet<>(stack);

            //Check if we need to exit
            if (targetSet.size() == 1 && stack.size() == 10) {
                logger.info(this.name.toUpperCase() + " FINISHED");
                for (String value : targetSet) {
                    this.exitedThreads.put(this.name, value);
                    break;
                }
                exit = true;
            }
        }

        //Clear our queues to avoid blocking the other threads
        for (Map.Entry<String, CommunicationChannel> entry : channels.entrySet()) {
            CommunicationChannel channel = entry.getValue();

            channel.getCallbackQueues().get(this.name).clear();
        }
        this.requestQueue.clear();
        logger.info(this.name.toUpperCase() + " EXITED");
        //Exiting
        Thread.currentThread().interrupt();
    }


    /**
     * Process an incoming request from a message in our request queue.<br>
     * If the requested ball doesn't match our target, we remove it and allow the other thread to take it, it will return a response {@code Message} with a response set as {@code true}<br>
     * If request ball matches our target:<br>
     *  <ul>
     *  <li>If we have a pending request with this thread on this specific ball, we check the timestamp to see who needs to switch target. it will return a response {@code Message} with a response set as {@code true}
     *  The one with the latest timestamp will have to switch. It will return a response {@code Message} with a response set as {@code true}</li>
     * <li>If we don't have any pending request with the thread, we reject it, and the requesting thread will switch color. it will return a response {@code Message} with a response set as {@code false}
     * If it doesn't have the color, it will return a response {@code Message} with a response set as {@code null}</li>
     * </ul>
     *
     * @param incomingMessage {@code String} representing the thread we are trying to send a request to, or treating a response from
     */
    private void processIncomingRequest(Message incomingMessage) {

        //Getting name of the thread sending the message
        String threadNameI = incomingMessage.getThreadName();

        Message response = new Message();

        if (incomingMessage.getSide() == MessageSide.REQUEST && threadNameI != null) {
            //Message is a request
            logger.info("Incoming request from " + threadNameI + " = " + incomingMessage.toString());

            response.setRequestId(incomingMessage.getRequestId());
            response.setThreadName(incomingMessage.getThreadName());
            response.setSide(MessageSide.RESPONSE);
            response.setBall(incomingMessage.getBall());

            if (incomingMessage.getBall().equals(this.target)) {
                //Requested ball is the same as our target
                Message pendingMessage = pendingRequests.get(threadNameI);

                //Checking messages timestamps
                if (pendingMessage != null && pendingMessage.getTimestamp().after(incomingMessage.getTimestamp())) {
                    //Our timestamp is after this request, so we need to change target.
                    logger.info("Incoming request from " + threadNameI + " is same as our target, but accepting it because timestamp was before ours");
                    //Request is accepted
                    response.setResponse(true);
                    //Changing target
                    reprocessTarget();
                } else {
                    //Our timestamp is before this request, so we don't change, the requesting thread will
                    logger.info("Incoming request from " + threadNameI + " is rejected because it is our target");
                    response.setResponse(false);
                }

                //Check if we have the requested ball
            } else if (stack.contains(incomingMessage.getBall())) {
                //We have the requested ball and it is not our target, accepting it

                logger.info("Incoming request from " + threadNameI + " is not on our target, accepting it");

                //We only respond true if we succeeded to remove the ball from our stack
                if (!this.stack.remove(incomingMessage.getBall())) {
                    logger.error("Couldn't remove " + incomingMessage.getBall() + " from our stack");
                    response.setResponse(null);
                } else {
                    logger.info("Successfully removed " + incomingMessage.getBall() + " from our stack");
                    response.setResponse(true);
                }
            } else {
                //We don't have the requested ball, setting response to null
                response.setResponse(null);
            }
            try {
                //Sending response to requesting thread
                if (!exitedThreads.containsKey(threadNameI))
                    channels.get(threadNameI).getCallbackQueues().get(threadNameI).put(response);
            } catch (InterruptedException | NullPointerException e) {
                logger.error("Error while trying to put response : " + e.getMessage());
            }

        }

    }

    /**
     * Sends a request to a thread if we don't have a pending request with it. If we have a pending request with it, we process the response instead.<br>
     * For each request we want to send we timestamp it to be able to check the first sender in case of conflict.<br>
     * We only send a request if we don't already have a pending request with this targeted thread to avoid spamming.<br>
     * Request messages are sent on the targeted thread request queue.<br>
     * <p>
     * When we process a response we can face several situations:
     * <ul>
     *  <li>We don't have any response, in that case we don't do anything</li>
     *  <li>We have a rejected response, in that case we need to change our target</li>
     *  <li>We have a successful response, in that case we add the incoming ball to our stack</li>
     *  <li>We have a response message, but the response is set as null, that means the requested thread doesn't have what we requested</li>
     * </ul>
     *
     * @param threadName {@code String} representing the thread we are trying to send a request to, or treating a response from
     * @param channel    The object storing the request queue of this thread, and the callbacks we have with it
     */
    private void processOutgoingRequest(String threadName, CommunicationChannel channel) {

        //Initializing outgoing message
        Message message = new Message(target, MessageSide.REQUEST, this.name);
        message.setTimestamp(new Timestamp(new Date().getTime()));

        //We check that we don't have any outgoing message for this thread before sending a new one
        if (channel.getCallbackQueues().get(this.name).size() == 0 && !pendingRequests.containsKey(threadName)) {
            //We don't have any pending request for this thread
            boolean result = channel.getRequestQueue().offer(message);
            if (!result) {
                //Targeted thread's request queue is full, couldn't sent message, we do nothing
                return;
            }
            logger.info("Successfully sent request to " + threadName + " = " + message.toString());
            //Add message to our pending requests
            pendingRequests.put(message.getThreadName(), message);
        } else {
            //We have a pending request for this thread, checking for a response
            Message response = null;
            try {
                response = channel.getCallbackQueues().get(this.name).poll();
            } catch (NullPointerException e) {
                logger.error("Error while trying to poll response : " + e.getMessage());
            }

            //We check if we have a response. If we don't have any response, we don't do anything
            if (response != null) {
                //We have a response

                Message pendingMessage = pendingRequests.get(threadName);

                if (pendingMessage != null && response.getRequestId().equals(pendingMessage.getRequestId())) {
                    //Removing message from pending requests for this thread
                    pendingRequests.remove(threadName);
                }

                if (response.getResponse() != null) {

                    if (response.getResponse()) {
                        //Request was accepted, we add it to our stack
                        logger.info("Request was accepted from " + threadName);
                        this.stack.add(response.getBall());
                    } else if (response.getBall().equals(this.target)) {
                        //Request was rejected, we need to change target
                        logger.warn("Request was rejected from " + threadName);
                        reprocessTarget();
                    }
                } else {
                    //The thread didn't have what we asked
                    logger.warn("Response was null from " + threadName + " : " + response.toString());
                    //If we  switched target and now we have a target that is from a thread already completed, we need to switch to avoid being blocked
                    if (exitedThreads.containsValue(target)) {
                        reprocessTarget();
                    }
                }
            }

        }
    }
}
