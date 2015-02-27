package ch.usi.dag.disl.test.junit;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SmokeTest {

    @Test
    public void thisAlwaysPasses() {

    }

    @Test
    @Ignore
    public void thisIsIgnored() {

    }
}
