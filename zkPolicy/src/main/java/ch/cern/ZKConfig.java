package ch.cern;

import lombok.*;

/**
 * Class that holds configuration parameters as defined in the config.yaml CLI
 * argument
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKConfig {

    private String zkservers;
    private int timeout;
    private String matchcolor;
    private String mismatchcolor;
    private String jaas;
    private String log4j;

    public void setPropertyJaas() {
        if (this.jaas != null && !this.jaas.isEmpty()) {
            java.lang.System.setProperty("java.security.auth.login.config", this.jaas);
        }
    }

    public void setPropertyLog4j() {
        if (this.jaas != null && !this.jaas.isEmpty()) {
            java.lang.System.setProperty("log4j.configuration", "file:"+this.log4j);
        }
    }
}