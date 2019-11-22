package lab577;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Test class running the program 100 times to check that the solution works and that we have the same result every time
 */
public class LauncherTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(LauncherTest.class);

    /**
     * Run the {@code Launcher} 100 times, and check that the value returned match the expected value.<br>
     * If all tests are OK, then the test will pass, otherwise it will fail. In case of failure, it is more probable that it
     * stays stuck, than returning a wrong value
     */
    public void testStart() {
        for(int i = 0 ; i <= 100; i++){
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