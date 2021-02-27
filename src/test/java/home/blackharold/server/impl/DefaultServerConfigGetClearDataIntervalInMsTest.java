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
public class DefaultServerConfigGetClearDataIntervalInMsTest extends AbstractDefaultServerConfigTest {
    @DataPoints
    public static String[][] testCases = new String[][]{
            {"999", "storage.clear.data.interval.ms should be >= 1000 ms"},
            {"qw", "storage.clear.data.interval.ms should be a number"}
    };
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    @Theory
    public void getClearDataIntervalInMs(final String... testData) throws Exception {
        String value = testData[0];
        String message = testData[1];

        Properties p = new Properties();
        p.setProperty("storage.clear.data.interval.ms", value);
        thrown.expect(NourConfigException.class);
        thrown.expectMessage(is(message));

        defaultServerConfig = createDefaultServerConfigMock(p);
        defaultServerConfig.getClearDataIntervalInMs();
    }
}


