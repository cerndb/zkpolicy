package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKPatternTest {

    @Test
    public void testCreateGlobPatternList() {
        String[] globList = {"world:*:*","sasl:*:c?rw"};
        List<Pattern> patternList = ZKPattern.createGlobPatternList(globList);
        List<Pattern> expectedList = new ArrayList<Pattern>();
        expectedList.add(Pattern.compile("world:.*:.*"));
        expectedList.add(Pattern.compile("sasl:.*:c.rw"));
        assertEquals(expectedList.size(), patternList.size());
        assertEquals(expectedList.get(0).pattern(), patternList.get(0).pattern());
        assertEquals(expectedList.get(1).pattern(), patternList.get(1).pattern());
    }

    @Test
    public void testNoArgsConstructor() {
        assertNotNull(new ZKPattern());
    }
}