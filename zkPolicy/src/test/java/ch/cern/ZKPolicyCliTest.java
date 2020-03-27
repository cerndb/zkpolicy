package ch.cern;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKPolicyCliTest {
    ZKPolicyCli zkpCli;

    @BeforeAll
    public void startZookeeper() throws Exception {

    }

    @Test
    public void testDefinitionStage() throws Exception {
        String[] args = { "-h", "-v" };
        zkpCli = new ZKPolicyCli(args);
        CommandLine cli = zkpCli.ParseCl(zkpCli.optionsMetadata, true);
        assertTrue(cli.hasOption("help"));
        assertTrue(cli.hasOption("version"));
    }
}