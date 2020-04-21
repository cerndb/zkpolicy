package ch.cern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKPolicyCliTest {
  ZKPolicyCli zkpCli;

  @BeforeAll
  public void startZookeeper() throws Exception {

    }
}