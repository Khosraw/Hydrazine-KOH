/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Hydrazine.KOH;

import com.github.hydrazine.Hydrazine;
import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {
    @Test public void appHasAGreeting() {
        Hydrazine classUnderTest = new Hydrazine();
        assertNotNull("app should have a greeting", classUnderTest.getGreeting());
    }
}
