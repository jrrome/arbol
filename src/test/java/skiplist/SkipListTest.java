package skiplist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static skiplist.SkipListTestSupport.*;

class SkipListTest {

    private SkipList list;

    private static byte[] b(String value) {
        return value == null
                ? null
                : value.getBytes(StandardCharsets.UTF_8);
    }

    private static void assertBytes(String expected, byte[] actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertArrayEquals(b(expected), actual);
        }
    }

    @BeforeEach
    void setUp() {
        list = seededList(42);
    }

    @Test
    void newListIsEmpty() {
        assertEquals(0, list.getNumberOfNodes());
        assertTrue(list.isEmpty());
        assertNull(list.get(b("missing")));
        assertFalse(list.containsKey(b("missing")));
    }

    @Test
    void insertedEntryCanBeRetrieved() {
        list.put(b("ana"), b("value"));

        assertArrayEquals(b("value"), list.get(b("ana")));
        assertTrue(list.containsKey(b("ana")));
        assertEquals(1, list.getNumberOfNodes());
        assertFalse(list.isEmpty());
    }

    @Test
    void updatingExistingKeyReplacesValueWithoutIncreasingSize() {
        list.put(b("ana"), b("old"));
        list.put(b("ana"), b("new"));

        assertArrayEquals(b("new"), list.get(b("ana")));
        assertTrue(list.containsKey(b("ana")));
        assertEquals(1, list.getNumberOfNodes());
    }

    @Test
    void repeatedUpdatesDoNotLeaveOlderEntriesBehind() {
        for (int i = 0; i < 100; i++) {
            list.put(b("ana"), b("value-" + i));
        }

        assertArrayEquals(b("value-99"), list.get(b("ana")));
        assertEquals(1, list.getNumberOfNodes());

        list.delete(b("ana"));

        assertNull(list.get(b("ana")));
        assertFalse(list.containsKey(b("ana")));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ascending", "descending", "shuffled"})
    void supportsDifferentInsertionOrders(String order) {
        List<String> keys = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            keys.add("key-" + i);
        }

        Collections.sort(keys);

        if (order.equals("descending")) {
            Collections.reverse(keys);
        }

        if (order.equals("shuffled")) {
            Collections.shuffle(keys, new Random(123));
        }

        for (String key : keys) {
            list.put(b(key), b("value:" + key));
        }

        assertEquals(keys.size(), list.getNumberOfNodes());

        for (String key : keys) {
            assertTrue(list.containsKey(b(key)), key);
            assertArrayEquals(
                    b("value:" + key),
                    list.get(b(key)),
                    key
            );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "c", "z"})
    void missingKeysBeforeBetweenAndAfterEntriesAreAbsent(String missing) {
        list.put(b("b"), b("B"));
        list.put(b("d"), b("D"));

        assertNull(list.get(b(missing)));
        assertFalse(list.containsKey(b(missing)));
        assertEquals(2, list.getNumberOfNodes());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "c", "z"})
    void deletingMissingKeyPreservesExistingEntries(String missing) {
        list.put(b("b"), b("B"));
        list.put(b("d"), b("D"));

        list.delete(b(missing));

        assertEquals(2, list.getNumberOfNodes());
        assertFalse(list.isEmpty());
        assertArrayEquals(b("B"), list.get(b("b")));
        assertArrayEquals(b("D"), list.get(b("d")));
    }

    @Test
    void deletingFromEmptyListDoesNotMakeSizeNegative() {
        list.delete(b("missing"));
        list.delete(b("missing"));

        assertEquals(0, list.getNumberOfNodes());
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c"})
    void deletingFirstMiddleOrLastKeyPreservesOtherEntries(String deleted) {
        for (String key : List.of("a", "b", "c")) {
            list.put(b(key), b("value:" + key));
        }

        list.delete(b(deleted));

        assertEquals(2, list.getNumberOfNodes());

        for (String key : List.of("a", "b", "c")) {
            assertEquals(
                    !key.equals(deleted),
                    list.containsKey(b(key)),
                    key
            );

            if (key.equals(deleted)) {
                assertNull(list.get(b(key)), key);
            } else {
                assertArrayEquals(
                        b("value:" + key),
                        list.get(b(key)),
                        key
                );
            }
        }
    }

    @Test
    void deletingLastKeyLeavesListEmpty() {
        list.put(b("ana"), b("value"));
        list.delete(b("ana"));

        assertEquals(0, list.getNumberOfNodes());
        assertTrue(list.isEmpty());
        assertFalse(list.containsKey(b("ana")));
        assertNull(list.get(b("ana")));
    }

    @Test
    void deletingSameKeyTwiceOnlyDecrementsSizeOnce() {
        list.put(b("a"), b("A"));
        list.put(b("b"), b("B"));

        list.delete(b("a"));
        list.delete(b("a"));

        assertEquals(1, list.getNumberOfNodes());
        assertFalse(list.isEmpty());
        assertFalse(list.containsKey(b("a")));
        assertArrayEquals(b("B"), list.get(b("b")));
    }

    @Test
    void deletedKeyCanBeReinsertedWithNewValue() {
        list.put(b("a"), b("old"));
        list.delete(b("a"));
        list.put(b("a"), b("new"));

        assertEquals(1, list.getNumberOfNodes());
        assertArrayEquals(b("new"), list.get(b("a")));
        assertTrue(list.containsKey(b("a")));
    }

    @Test
    void independentListsDoNotShareEntries() {
        SkipList other = seededList(42);

        list.put(b("a"), b("first"));
        other.put(b("a"), b("second"));

        list.delete(b("a"));

        assertTrue(list.isEmpty());
        assertArrayEquals(b("second"), other.get(b("a")));
        assertEquals(1, other.getNumberOfNodes());
    }

    @Test
    void emptyStringKeyIsARealEntryAndNotTheSentinel() {
        list.put(b(""), b("empty-key"));
        list.put(b("a"), b("A"));

        assertArrayEquals(b("empty-key"), list.get(b("")));
        assertTrue(list.containsKey(b("")));

        list.put(b(""), b("updated"));

        assertArrayEquals(b("updated"), list.get(b("")));
        assertEquals(2, list.getNumberOfNodes());

        list.delete(b(""));

        assertFalse(list.containsKey(b("")));
        assertArrayEquals(b("A"), list.get(b("a")));
        assertEquals(1, list.getNumberOfNodes());
    }

    @Test
    void keysWithSharedPrefixesCaseAndUnicodeRemainDistinct() {
        List<String> keys = List.of(
                "a",
                "aa",
                "aaa",
                "ab",
                "A",
                "á",
                "ñ",
                "树",
                "🌳",
                " ",
                "\u0000"
        );

        for (String key : keys) {
            list.put(b(key), b("value:" + key));
        }

        assertEquals(keys.size(), list.getNumberOfNodes());

        for (String key : keys) {
            assertArrayEquals(
                    b("value:" + key),
                    list.get(b(key)),
                    key
            );
        }

        list.delete(b("aa"));

        assertArrayEquals(b("value:a"), list.get(b("a")));
        assertArrayEquals(b("value:aaa"), list.get(b("aaa")));
        assertFalse(list.containsKey(b("aa")));
    }

    @Test
    void longKeysWithCommonPrefixRemainDistinct() {
        String prefix = "x".repeat(10_000);

        list.put(b(prefix + "a"), b("A"));
        list.put(b(prefix + "b"), b("B"));

        assertArrayEquals(b("A"), list.get(b(prefix + "a")));
        assertArrayEquals(b("B"), list.get(b(prefix + "b")));
        assertNull(list.get(b(prefix)));
        assertEquals(2, list.getNumberOfNodes());
    }

    @Test
    void emptyStringValueIsPreserved() {
        list.put(b("a"), b(""));

        assertArrayEquals(b(""), list.get(b("a")));
        assertTrue(list.containsKey(b("a")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullValuesAreRejectedWithoutChangingState(boolean populated) {
        if (populated) list.put(b("a"), b("value"));
        int previousSize = list.size();

        assertThrows(NullPointerException.class, () -> list.put(b("a"), null));
        assertThrows(NullPointerException.class, () -> list.put(b("missing"), null));

        assertEquals(previousSize, list.size());
        assertEquals(populated ? 1 : 0, list.getNumberOfNodes());
        assertEquals(!populated, list.isEmpty());
        assertBytes(populated ? "value" : null, list.get(b("a")));
        assertFalse(list.containsKey(b("missing")));

        list.put(b("b"), b("B"));
        assertArrayEquals(b("B"), list.get(b("b")));
    }

    @Test
    void emptyKeyAndValueStillCountAsAnEntry() {
        list.put(new byte[0], new byte[0]);

        assertEquals(0, list.size());
        assertEquals(1, list.getNumberOfNodes());
        assertFalse(list.isEmpty());
        assertTrue(list.containsKey(new byte[0]));

        list.delete(new byte[0]);
        assertTrue(list.isEmpty());
    }

    @Test
    void mutatingInsertedArraysDoesNotChangeStoredDataOrOrdering() {
        byte[] key = {1};
        byte[] value = {10};
        list.put(key, value);
        list.put(new byte[]{2}, new byte[]{20});

        key[0] = 3;
        value[0] = 99;

        assertArrayEquals(new byte[]{10}, list.get(new byte[]{1}));
        assertArrayEquals(new byte[]{20}, list.get(new byte[]{2}));
        assertFalse(list.containsKey(new byte[]{3}));
    }

    @Test
    void mutatingReplacementAndReturnedValuesDoesNotChangeStoredData() {
        list.put(b("a"), new byte[]{1});
        byte[] replacement = {2, 3};
        list.put(b("a"), replacement);
        replacement[0] = 99;

        byte[] returned = list.get(b("a"));
        assertArrayEquals(new byte[]{2, 3}, returned);
        returned[1] = 88;

        assertArrayEquals(new byte[]{2, 3}, list.get(b("a")));
        assertEquals(3, list.size());
        assertEquals(1, list.getNumberOfNodes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void iteratorEntriesDoNotExposeStoredArrays(boolean fromKey) {
        list.put(new byte[]{1}, new byte[]{10});
        list.put(new byte[]{2}, new byte[]{20});
        var iterator = fromKey ? list.iteratorFrom(new byte[]{1}) : list.iterator();
        var first = iterator.next();
        assertArrayEquals(new byte[]{1}, first.getKey());
        assertArrayEquals(new byte[]{10}, first.getValue());

        first.getKey()[0] = 3;
        first.getValue()[0] = 99;

        assertArrayEquals(new byte[]{10}, list.get(new byte[]{1}));
        assertArrayEquals(new byte[]{20}, list.get(new byte[]{2}));
        assertFalse(list.containsKey(new byte[]{3}));
        assertArrayEquals(new byte[]{2}, iterator.next().getKey());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void emptyIteratorDoesNotReturnTheSentinel() {
        var iterator = list.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void sizeTracksPayloadBytesAcrossUpdatesAndDeletes() {
        list.put(b("a"), new byte[]{1, 2});
        assertEquals(3, list.size());
        list.put(b("a"), new byte[]{3});
        assertEquals(2, list.size());
        list.put(b("b"), new byte[0]);
        assertEquals(3, list.size());
        list.delete(b("a"));
        assertEquals(1, list.size());
        list.delete(b("b"));
        assertEquals(0, list.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullKeysAreRejectedWithoutChangingState(boolean populated) {
        if (populated) {
            list.put(b("a"), b("A"));
        }

        assertThrows(
                NullPointerException.class,
                () -> list.put(null, b("value"))
        );

        assertThrows(
                NullPointerException.class,
                () -> list.get(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> list.containsKey(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> list.delete(null)
        );

        assertEquals(populated ? 1 : 0, list.getNumberOfNodes());
        assertEquals(!populated, list.isEmpty());

        if (populated) {
            assertArrayEquals(b("A"), list.get(b("a")));
        } else {
            assertNull(list.get(b("a")));
        }

        list.put(b("b"), b("B"));

        assertArrayEquals(b("B"), list.get(b("b")));
    }

    @Test
    void insertionAfterShortNodeWorksWhenHigherLevelsExist() {
        // Heights: a=0, z=1. The predecessor for b has no forward[1].
        list = withInitialCoinFlips(false, true, false);

        list.put(b("a"), b("A"));
        list.put(b("z"), b("Z"));

        assertDoesNotThrow(() -> list.put(b("b"), b("B")));

        assertArrayEquals(b("A"), list.get(b("a")));
        assertArrayEquals(b("B"), list.get(b("b")));
        assertArrayEquals(b("Z"), list.get(b("z")));
        assertEquals(3, list.getNumberOfNodes());
    }

    @Test
    void updatingShortNodeDoesNotCreateHiddenDuplicate() {
        // Heights: a=1, b=0. Looking at level 1 would miss the existing b.
        list = withInitialCoinFlips(true, false, false);

        list.put(b("a"), b("A"));
        list.put(b("b"), b("old"));
        list.put(b("b"), b("new"));

        assertArrayEquals(b("new"), list.get(b("b")));
        assertEquals(2, list.getNumberOfNodes());

        list.delete(b("b"));

        assertNull(list.get(b("b")));
        assertFalse(list.containsKey(b("b")));
        assertEquals(1, list.getNumberOfNodes());
    }

    @Test
    void removingTallestNodeAllowsFurtherInsertionAndSearch() {
        // Heights: a=0, b=2. Removing b empties both upper levels.
        list = withInitialCoinFlips(false, true, true, false);

        list.put(b("a"), b("A"));
        list.put(b("b"), b("B"));

        list.delete(b("b"));

        list.put(b("c"), b("C"));

        assertArrayEquals(b("A"), list.get(b("a")));
        assertArrayEquals(b("C"), list.get(b("c")));
        assertFalse(list.containsKey(b("b")));
        assertEquals(2, list.getNumberOfNodes());
    }
}
