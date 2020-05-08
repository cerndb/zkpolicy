package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKTreeTest {
    ZKConfig config;
    ZKClient zkClient;
    ZKTree zkTree;
    TestingServer zkTestServer;
    String colorMatch;
    String colorNoMatch;
    String colorReset;

    @Test
    public void testColorCodeExplanation() throws Exception {
        zkTestServer = new TestingServer();
        this.config = new ZKConfig(zkTestServer.getConnectString(), 2000, "GREEN", "RED", "", "");
        this.zkClient = new ZKClient(config);
        this.zkTree  = new ZKTree(zkClient);

        this.colorMatch = ZKPolicyDefs.Colors.GREEN.getANSIValue();
        this.colorNoMatch = ZKPolicyDefs.Colors.RED.getANSIValue();
        this.colorReset = ZKPolicyDefs.Colors.RESET.getANSIValue();

        String expectedOutput = "* " + this.colorMatch + "GREEN" + this.colorReset + ": znodes matching the query\n"+
        "* " + this.colorNoMatch + "RED" + this.colorReset + ": znodes not matching the query\n";

        assertEquals(expectedOutput, zkTree.colorCodeExplanation());

    }

}
