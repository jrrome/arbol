package skiplist;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static skiplist.SkipListTestSupport.seededList;

class SkipListRandomizedTest {
    @ParameterizedTest(name = "mixed operations, seed={0}")
    @ValueSource(longs = {0, 1, 2, 42, 123, 4096, 987654321, -1})
    void mixedOperationsMatchTreeMapAfterEveryStep(long seed) {
        SkipList list = seededList(seed);
        Map<String, String> expected = new TreeMap<>();
        Random operations = new Random(seed ^ 0x5DEECE66DL);
        List<String> keys = new ArrayList<>(List.of("", "A", "a", "á", "树"));
        for (int i = 0; i < 64; i++) keys.add("key-" + i);

        for (int step = 0; step < 2_000; step++) {
            String key = keys.get(operations.nextInt(keys.size()));
            String context = "seed=" + seed + ", step=" + step + ", key=" + key;
            switch (operations.nextInt(5)) {
                case 0, 1 -> {
                    String value = operations.nextInt(8) == 0 ? null : "value-" + step;
                    list.set(key, value);
                    expected.put(key, value);
                }
                case 2 -> {
                    list.delete(key);
                    expected.remove(key);
                }
                case 3 -> assertEquals(expected.get(key), list.get(key), context);
                case 4 -> assertEquals(expected.containsKey(key), list.containsKey(key), context);
                default -> throw new AssertionError("Unexpected operation");
            }
            assertEquals(expected.size(), list.size(), context);
            assertEquals(expected.isEmpty(), list.isEmpty(), context);
            // Check the whole key universe, including absent and null-valued keys.
            for (String candidate : keys) {
                String message = context + ", checking=" + candidate;
                assertEquals(expected.containsKey(candidate), list.containsKey(candidate), message);
                assertEquals(expected.get(candidate), list.get(candidate), message);
            }
        }
    }

    @ParameterizedTest(name = "empty and refill, seed={0}")
    @ValueSource(longs = {0, 42, 4096, 987654321})
    void repeatedFillAndDrainCyclesLeaveNoStaleEntries(long seed) {
        SkipList list = seededList(seed);
        Random order = new Random(seed);
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 300; i++) keys.add("key-" + i);

        for (int cycle = 0; cycle < 4; cycle++) {
            Collections.shuffle(keys, order);
            for (String key : keys) list.set(key, "cycle-" + cycle + ":" + key);
            assertEquals(keys.size(), list.size());
            for (String key : keys) assertEquals("cycle-" + cycle + ":" + key, list.get(key));

            Collections.shuffle(keys, order);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                list.delete(key);
                assertFalse(list.containsKey(key), key);
                assertNull(list.get(key), key);
                assertEquals(keys.size() - i - 1, list.size());
                assertEquals(i == keys.size() - 1, list.isEmpty());
            }
            for (String key : keys) {
                assertFalse(list.containsKey(key), key);
                assertNull(list.get(key), key);
            }
        }
    }
}
