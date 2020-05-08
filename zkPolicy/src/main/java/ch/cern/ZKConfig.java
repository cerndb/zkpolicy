package ch.cern;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

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
    private String defaultauditpath;

    public ZKConfig(File configFile) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.readerForUpdating(this).readValue(configFile);
        this.setPropertyJaas();
        this.setPropertyLog4j();
    }

    public void setPropertyJaas() {
        if (this.jaas != null && !this.jaas.isEmpty()) {
            java.lang.System.setProperty("java.security.auth.login.config", this.jaas);
        }
    }

    public void setPropertyLog4j() {
        if (this.log4j != null && !this.log4j.isEmpty()) {
            java.lang.System.setProperty("log4j.configuration", "file:"+this.log4j);
        }
    }


}