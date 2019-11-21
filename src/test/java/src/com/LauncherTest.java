package src.com;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LauncherTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(LauncherTest.class);

    public void testStart() {
        for(int i = 0 ; i <= 30; i++){
            HashMap<String, ArrayList<String>> results =  Launcher.start();
            assertEquals(results.size(),3);
            for(ArrayList<String> result : results.values()){
                Set<String> targetSet = new HashSet<>(result);
                assertEquals(targetSet.size(),1);
                assertEquals(result.size(),10);
                boolean letterCheck = result.contains("G") || result.contains("R") || result.contains("B");
                assertTrue(letterCheck);
            }
            logger.info("TEST "+i+" OK");
        }
        logger.info("ALL TESTS OK");
    }
}