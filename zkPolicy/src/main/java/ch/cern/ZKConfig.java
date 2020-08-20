/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
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
 * argument.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKConfig {

  private String zkServers;
  private int timeout;
  private String matchColor;
  private String mismatchColor;
  private String jaas;

  /**
   * Construct ZKConfig using configuration YAML file.
   * 
   * @param configFile YAML config file
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public ZKConfig(File configFile) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    om.readerForUpdating(this).readValue(configFile);
    this.setPropertyJaas();
  }

  /**
   * Set java.security.auth.login.config environment variable
   * Priority:
   *   1. CLI option
   *   2. java.env
   *   3. config file
   */
  public void setPropertyJaas() {
    if (System.getProperty("java.security.auth.login.config") != null && !System.getProperty("java.security.auth.login.config").isEmpty()) {
      return;
    }
    // if jaas is provided in config file then use it
    if (this.jaas != null && !this.jaas.isEmpty()) {
      System.setProperty("java.security.auth.login.config", this.jaas);
      return;
    }
  }
}