package ch.cern;

import java.io.File;
import java.util.List;

import org.apache.zookeeper.ZooKeeper;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "enforce", aliases = { "E" }, description = "Enforce policy on znodes", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKEnforceCli implements Runnable {
    @ParentCommand
    private ZKPolicyCli parent;

    static class FileEnforceGroup {
        @Option(names = { "-i", "--input" }, required = true, description = "File with policy definitions to enforce")
        File policiesFile;
    }

    static class CliEnforceGroup {
        @Option(names = { "-P", "--policy" }, required = true, description = "Policies to enforce on matching nodes")
        String[] policies;

        @Parameters(paramLabel = "[QUERY_NAME]", description = "Query to be executed: ${COMPLETION-CANDIDATES}", completionCandidates = ZKQueryCli.DefaultQueryCandidates.class)
        String queryName;

        @Option(names = { "-p", "--root-path" }, required = false, description = "Path pattern to match")
        String rootPath = null;

        @Option(names = { "-a", "--acls" }, required = false, description = "ACLs for querying")
        String[] queryACLs;

        @Option(names = { "-d", "--dry-run" }, required = false, description = "Dry run execution")
        boolean dryRun = false;

        @Option(names = { "-A", "--append" }, required = false, description = "Append policy ACLs to znode's ACL")
        boolean append = false;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {
        @ArgGroup(exclusive = false)
        FileEnforceGroup fileEnforceGroup;

        @ArgGroup(exclusive = false)
        CliEnforceGroup cliEnforceGroup;

    }

    @Override
    public void run() {
        if (this.exclusive.fileEnforceGroup == null) {
            this.cliEnforce();
        } else if (this.exclusive.cliEnforceGroup == null) {
            this.cliEnforceFromFIle();
        }

    }

    /**
     * Enforce policies passed through CLI when using the --policy flag
     */
    private void cliEnforce() {
        ZooKeeper zkClient;

        try (ZKConnection zkServer = new ZKConnection()) {
            ZKConfig config = new ZKConfig(parent.configFile);
            zkClient = zkServer.connect(config.getZkservers(), config.getTimeout());
            ZKEnforce zkEnforce = new ZKEnforce(zkClient);

            if (this.exclusive.cliEnforceGroup.dryRun) {
                zkEnforce.enforceDry(this.exclusive.cliEnforceGroup.queryName, this.exclusive.cliEnforceGroup.rootPath,
                        this.exclusive.cliEnforceGroup.queryACLs);
            } else {
                zkEnforce.enforce(this.exclusive.cliEnforceGroup.policies, this.exclusive.cliEnforceGroup.queryName,
                        this.exclusive.cliEnforceGroup.rootPath, this.exclusive.cliEnforceGroup.queryACLs,
                        this.exclusive.cliEnforceGroup.append);
            }
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            System.out.println("No such method: " + this.exclusive.cliEnforceGroup.queryName);
            System.out.println("Please consult the list of default queries using query -h");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Enforce policies defined in --input option file path
     */
    private void cliEnforceFromFIle() {
        ZooKeeper zkClient;

        try (ZKConnection zkServer = new ZKConnection()){
            ZKConfig config = new ZKConfig(parent.configFile);
            zkClient = zkServer.connect(config.getZkservers(), config.getTimeout());
            ZKEnforce zkEnforce = new ZKEnforce(zkClient);
            ZKEnforcePolicySet policySet = new ZKEnforcePolicySet(this.exclusive.fileEnforceGroup.policiesFile);
            List<ZKEnforcePolicyElement> policies = policySet.getPolicies();

            // For each of the policies, execute enforce passing each of the parameters
            for (ZKEnforcePolicyElement policy : policies) {
                zkEnforce.enforce(policy.getAcls(), policy.getQuery().getName(), policy.getQuery().getRootpath(),
                        policy.getQuery().getAcls(), policy.isAppend());

            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}