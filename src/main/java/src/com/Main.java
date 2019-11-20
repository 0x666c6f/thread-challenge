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

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    public static void main(String[] args){

        logger.info("Initializing threads...");
        String thread1Name = "thread0";
        String thread2Name = "thread1";
        String thread3Name = "thread2";

         String[] stack1 = new String[]{"R","R","R","G","G","G","B","B","B","R"};
        String[] stack2 = new String[]{"G","G","G","R","R","B","B","B","R","R"};
        String[] stack3 = new String[]{"B","B","B","B","R","G","G","G","G","R"};

//        String[] stack1 = new String[]{"R","R","G","G","G","G","B","B","B","R"};
//        String[] stack2 = new String[]{"G","G","R","R","R","B","B","B","R","R"};
//        String[] stack3 = new String[]{"B","B","B","B","R","G","G","G","G","R"};

        BlockingQueue<Message> requestQueue1 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> requestQueue2 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> requestQueue3 = new ArrayBlockingQueue<>(1);

        BlockingQueue<Message> callbackQueue12 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> callbackQueue21 = new ArrayBlockingQueue<>(1);

        BlockingQueue<Message> callbackQueue13 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> callbackQueue31 = new ArrayBlockingQueue<>(1);

        BlockingQueue<Message> callbackQueue23 = new ArrayBlockingQueue<>(1);
        BlockingQueue<Message> callbackQueue32 = new ArrayBlockingQueue<>(1);

        HashMap<String, BlockingQueue<Message>> callbackQueues12 = new HashMap<>();
        callbackQueues12.put(thread1Name, callbackQueue12);
        callbackQueues12.put(thread2Name, callbackQueue21);

        HashMap<String, BlockingQueue<Message>> callbackQueues13 = new HashMap<>();
        callbackQueues13.put(thread1Name, callbackQueue13);
        callbackQueues13.put(thread3Name, callbackQueue31);

        HashMap<String, BlockingQueue<Message>> callbackQueues23 = new HashMap<>();
        callbackQueues23.put(thread2Name, callbackQueue23);
        callbackQueues23.put(thread3Name, callbackQueue32);



        CommunicationChannel channel1to2 = new CommunicationChannel(requestQueue2,callbackQueues12);
        CommunicationChannel channel1to3 = new CommunicationChannel(requestQueue3,callbackQueues13);

        CommunicationChannel channel2to1 = new CommunicationChannel(requestQueue1, callbackQueues12);
        CommunicationChannel channel2to3 = new CommunicationChannel(requestQueue3,callbackQueues23);

        CommunicationChannel channel3to1 = new CommunicationChannel(requestQueue1,callbackQueues13);
        CommunicationChannel channel3to2 = new CommunicationChannel(requestQueue2,callbackQueues23);

        HashMap<String,CommunicationChannel> threadQueuesMap1 = new HashMap<>();
        threadQueuesMap1.put(thread2Name,channel1to2);
        threadQueuesMap1.put(thread3Name, channel1to3);
        HashMap<String, CommunicationChannel> threadQueuesMap2 = new HashMap<>();
        threadQueuesMap2.put(thread1Name, channel2to1);
        threadQueuesMap2.put(thread3Name, channel2to3);
        HashMap<String, CommunicationChannel> threadQueuesMap3 = new HashMap<>();
        threadQueuesMap3.put(thread1Name, channel3to1);
        threadQueuesMap3.put(thread2Name,channel3to2);


        WorkerThread worker1 = new WorkerThread(thread1Name, new ArrayList( Arrays.asList(stack1)),threadQueuesMap1,requestQueue1 );
        WorkerThread worker2 = new WorkerThread(thread2Name, new ArrayList( Arrays.asList(stack2)),threadQueuesMap2,requestQueue2 );
        WorkerThread worker3 = new WorkerThread(thread3Name, new ArrayList( Arrays.asList(stack3)),threadQueuesMap3,requestQueue3 );

        Thread thread1 = new Thread(worker1);
        thread1.start();

        Thread thread2 = new Thread(worker2);
        thread2.start();

        Thread thread3 = new Thread(worker3);
        thread3.start();
    }


}
