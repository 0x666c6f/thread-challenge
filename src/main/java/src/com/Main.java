package src.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.com.channel.CommunicationChannel;
import src.com.messages.Message;
import src.com.worker.WorkerThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class Main {
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    public static void main(String[] args) {

        logger.info("Initializing threads...");

        //Initialize thread names
        String thread1Name = "thread1";
        String thread2Name = "thread2";
        String thread3Name = "thread3";

        //Initialize thread stacks
        ArrayList<String> stack1 = new ArrayList<>(Arrays.asList("R", "R", "R", "G", "G", "G", "B", "B", "B", "R"));
        ArrayList<String> stack2 = new ArrayList<>(Arrays.asList("G", "G", "G", "R", "R", "B", "B", "B", "R", "R"));
        ArrayList<String> stack3 = new ArrayList<>(Arrays.asList("B", "B", "B", "B", "R", "G", "G", "G", "G", "R"));


        //Initialize thread request queues
        BlockingQueue<Message> requestQueue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> requestQueue2 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> requestQueue3 = new ArrayBlockingQueue<>(1);

        //Initialize thread callback queues between Thread 1 and 2
        //Callback for 1->2
        BlockingQueue<Message> callbackQueue12 = new ArrayBlockingQueue<>(1);
        //Callback for 2->1
        BlockingQueue<Message> callbackQueue21 = new ArrayBlockingQueue<>(1);

        //Initialize thread callback queues between Thread 1 and 3
        //Callback for 1->3
        BlockingQueue<Message> callbackQueue13 = new ArrayBlockingQueue<>(1);
        //Callback for 3->1
        BlockingQueue<Message> callbackQueue31 = new ArrayBlockingQueue<>(1);

        //Initialize thread callback queues between Thread 2 and 3
        //Callback for 2->3
        BlockingQueue<Message> callbackQueue23 = new ArrayBlockingQueue<>(1);
        //Callback for 3->2
        BlockingQueue<Message> callbackQueue32 = new ArrayBlockingQueue<>(1);

        //Create callback map between thread 1 and 2
        HashMap<String, BlockingQueue<Message>> callbackQueues12 = new HashMap<>();
        //Response callback for Thread 1 for a response from Thread 2 is stored with Thread 1 name
        callbackQueues12.put(thread1Name, callbackQueue12);
        //Response callback for Thread 2 for a response from Thread 1 is stored with Thread 2 name
        callbackQueues12.put(thread2Name, callbackQueue21);

        //Create callback map between thread 1 and 3
        HashMap<String, BlockingQueue<Message>> callbackQueues13 = new HashMap<>();
        //Response callback for Thread 1 for a response from Thread 3 is stored with Thread 1 name
        callbackQueues13.put(thread1Name, callbackQueue13);
        //Response callback for Thread 3 for a response from Thread 1 is stored with Thread 3 name
        callbackQueues13.put(thread3Name, callbackQueue31);

        //Create callback map between thread 2 and 3
        HashMap<String, BlockingQueue<Message>> callbackQueues23 = new HashMap<>();
        //Response callback for Thread 2 for a response from Thread 3 is stored with Thread 2 name
        callbackQueues23.put(thread2Name, callbackQueue23);
        //Response callback for Thread 3 for a response from Thread 2 is stored with Thread 3 name
        callbackQueues23.put(thread3Name, callbackQueue32);


        //Initializing channels for Thread 1
        CommunicationChannel channel1to2 = new CommunicationChannel(requestQueue2, callbackQueues12);
        CommunicationChannel channel1to3 = new CommunicationChannel(requestQueue3, callbackQueues13);

        //Initializing channels for Thread 2
        CommunicationChannel channel2to1 = new CommunicationChannel(requestQueue1, callbackQueues12);
        CommunicationChannel channel2to3 = new CommunicationChannel(requestQueue3, callbackQueues23);

        //Initializing channels for Thread 3
        CommunicationChannel channel3to1 = new CommunicationChannel(requestQueue1, callbackQueues13);
        CommunicationChannel channel3to2 = new CommunicationChannel(requestQueue2, callbackQueues23);

        //Initializing channels map for Thread 1
        HashMap<String, CommunicationChannel> threadQueuesMap1 = new HashMap<>();
        threadQueuesMap1.put(thread2Name, channel1to2);
        threadQueuesMap1.put(thread3Name, channel1to3);
        //Initializing channels map for Thread 2
        HashMap<String, CommunicationChannel> threadQueuesMap2 = new HashMap<>();
        threadQueuesMap2.put(thread1Name, channel2to1);
        threadQueuesMap2.put(thread3Name, channel2to3);
        //Initializing channels map for Thread 3
        HashMap<String, CommunicationChannel> threadQueuesMap3 = new HashMap<>();
        threadQueuesMap3.put(thread1Name, channel3to1);
        threadQueuesMap3.put(thread2Name, channel3to2);

        //Initializing runnable workers for the threads
        WorkerThread worker1 = new WorkerThread(thread1Name, stack1, threadQueuesMap1, requestQueue1);
        WorkerThread worker2 = new WorkerThread(thread2Name, stack2, threadQueuesMap2, requestQueue2);
        WorkerThread worker3 = new WorkerThread(thread3Name, stack3, threadQueuesMap3, requestQueue3);

        //Starting threads
        Thread thread1 = new Thread(worker1);
        thread1.start();

        Thread thread2 = new Thread(worker2);
        thread2.start();

        Thread thread3 = new Thread(worker3);
        thread3.start();
    }


}
