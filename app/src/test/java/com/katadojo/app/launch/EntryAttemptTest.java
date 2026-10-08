package com.katadojo.app.launch;
import org.junit.Test;
import static org.junit.Assert.*;
public class EntryAttemptTest {
    @Test public void sameAttemptUrlIsStableAndCleanHistoryPreservesQuery() {
        EntryAttempt attempt = new EntryAttempt();
        String source = "https://not-configured.invalid/?a=one%20two&a=three#part";
        String request = attempt.url(source);
        assertEquals(request, attempt.url(source));
        assertTrue(request.contains("n7qvp=" + attempt.id));
        assertEquals(source, EntryAttempt.strip(request));
        assertNotEquals(request, new EntryAttempt().url(source));
    }
    @Test public void replacesOldAttemptAndRemovesOnlyItsParameter() {
        EntryAttempt attempt = new EntryAttempt();
        String request = attempt.url("https://not-configured.invalid/?n7qvp=old&token=keep");
        assertFalse(request.contains("n7qvp=old"));
        assertEquals("https://not-configured.invalid/?token=keep", EntryAttempt.strip(request));
        assertEquals("https://not-configured.invalid/", EntryAttempt.strip("https://not-configured.invalid/?n7qvp=old"));
    }
}
