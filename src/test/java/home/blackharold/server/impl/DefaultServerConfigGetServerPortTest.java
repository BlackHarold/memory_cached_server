package home.blackharold.server.impl;


import home.blackharold.blacknour.exception.NourConfigException;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.hamcrest.core.Is.is;

@RunWith(Theories.class)
public class DefaultServerConfigGetServerPortTest extends AbstractDefaultServerConfigTest {
    @DataPoints
    public static String[][] testCases = new String[][]{
            {"-1", "server.port should be between 0 and 65535"},
            {"65536", "server.port should be between 0 and 65535"},
            {"qw", "storage.clear.data.interval.ms should be a number"}
    };
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    @Theory
    public void getServerPort(final String... testData) throws Exception {
        String value = testData[0];
        String message = testData[1];

        Properties p = new Properties();
        p.setProperty("server.port", value);
        thrown.expect(NourConfigException.class);
        thrown.expectMessage(is(message));

        defaultServerConfig = createDefaultServerConfigMock(p);
        defaultServerConfig.getServerPort();
    }
}


