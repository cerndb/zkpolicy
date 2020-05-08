package ch.cern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class that holds configuration parameters as defined in the config.yaml CLI
 * argument
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKQueryElement {

    private String name;
    private String rootpath;
    private String[] acls;
    private ZKQuery query;
}