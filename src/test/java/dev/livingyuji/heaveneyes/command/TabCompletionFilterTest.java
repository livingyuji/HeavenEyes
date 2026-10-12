package dev.livingyuji.heaveneyes.command;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TabCompletionFilterTest {

    private List<String> filter(Collection<String> items, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(lower))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void testPrefixFiltering() {
        List<String> commands = List.of("help", "version", "status", "report", "ban", "tempban", "unban");
        List<String> result = filter(commands, "b");
        assertEquals(List.of("ban"), result);

        List<String> tempResult = filter(commands, "t");
        assertEquals(List.of("tempban"), tempResult);
    }

    @Test
    public void testCaseInsensitivity() {
        List<String> options = List.of("Speed", "Flight", "Reach", "KillAura");
        List<String> result = filter(options, "k");
        assertEquals(List.of("KillAura"), result);

        List<String> resultUpper = filter(options, "SP");
        assertEquals(List.of("Speed"), resultUpper);
    }

    @Test
    public void testNoDuplicateAndSorted() {
        List<String> items = List.of("zebra", "apple", "apple", "banana");
        List<String> result = filter(items, "a");
        assertEquals(List.of("apple"), result);
    }
}
