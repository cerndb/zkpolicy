package ch.cern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * Class that holds configuration parameters as defined in the config.yaml CLI
 * argument
 */
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKConfig {

    private String zkservers;
    private int timeout;
    private String matchcolorname;
    private String matchcolorvalue;
    private String mismatchcolorname;
    private String mismatchcolorvalue;

}