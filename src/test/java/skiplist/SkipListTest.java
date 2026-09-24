package skiplist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static skiplist.SkipListTestSupport.*;

class SkipListTest {
    private SkipList list;

    @BeforeEach
    void setUp() {
        list = seededList(42);
    }

    @Test
    void newListIsEmpty() {
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertNull(list.get("missing"));
        assertFalse(list.containsKey("missing"));
    }

    @Test
    void insertedEntryCanBeRetrieved() {
        list.set("ana", "value");
        assertEquals("value", list.get("ana"));
        assertTrue(list.containsKey("ana"));
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
    }

    @Test
    void updatingExistingKeyReplacesValueWithoutIncreasingSize() {
        list.set("ana", "old");
        list.set(new String("ana"), "new");
        assertEquals("new", list.get(new String("ana")));
        assertTrue(list.containsKey(new String("ana")));
        assertEquals(1, list.size());
    }

    @Test
    void repeatedUpdatesDoNotLeaveOlderEntriesBehind() {
        for (int i = 0; i < 100; i++) list.set("ana", "value-" + i);
        assertEquals("value-99", list.get("ana"));
        assertEquals(1, list.size());
        list.delete("ana");
        assertNull(list.get("ana"));
        assertFalse(list.containsKey("ana"));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ascending", "descending", "shuffled"})
    void supportsDifferentInsertionOrders(String order) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 200; i++) keys.add("key-" + i);
        Collections.sort(keys);
        if (order.equals("descending")) Collections.reverse(keys);
        if (order.equals("shuffled")) Collections.shuffle(keys, new Random(123));
        for (String key : keys) list.set(key, "value:" + key);
        assertEquals(keys.size(), list.size());
        for (String key : keys) {
            assertTrue(list.containsKey(key), key);
            assertEquals("value:" + key, list.get(key), key);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "c", "z"})
    void missingKeysBeforeBetweenAndAfterEntriesAreAbsent(String missing) {
        list.set("b", "B");
        list.set("d", "D");
        assertNull(list.get(missing));
        assertFalse(list.containsKey(missing));
        assertEquals(2, list.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "c", "z"})
    void deletingMissingKeyPreservesExistingEntries(String missing) {
        list.set("b", "B");
        list.set("d", "D");
        list.delete(missing);
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        assertEquals("B", list.get("b"));
        assertEquals("D", list.get("d"));
    }

    @Test
    void deletingFromEmptyListDoesNotMakeSizeNegative() {
        list.delete("missing");
        list.delete("missing");
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "c"})
    void deletingFirstMiddleOrLastKeyPreservesOtherEntries(String deleted) {
        for (String key : List.of("a", "b", "c")) list.set(key, "value:" + key);
        list.delete(deleted);
        assertEquals(2, list.size());
        for (String key : List.of("a", "b", "c")) {
            assertEquals(!key.equals(deleted), list.containsKey(key), key);
            assertEquals(key.equals(deleted) ? null : "value:" + key, list.get(key), key);
        }
    }

    @Test
    void deletingLastKeyLeavesListEmpty() {
        list.set("ana", "value");
        list.delete("ana");
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.containsKey("ana"));
        assertNull(list.get("ana"));
    }

    @Test
    void deletingSameKeyTwiceOnlyDecrementsSizeOnce() {
        list.set("a", "A");
        list.set("b", "B");
        list.delete("a");
        list.delete("a");
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        assertFalse(list.containsKey("a"));
        assertEquals("B", list.get("b"));
    }

    @Test
    void deletedKeyCanBeReinsertedWithNewValue() {
        list.set("a", "old");
        list.delete("a");
        list.set("a", "new");
        assertEquals(1, list.size());
        assertEquals("new", list.get("a"));
        assertTrue(list.containsKey("a"));
    }

    @Test
    void independentListsDoNotShareEntries() {
        SkipList other = seededList(42);
        list.set("a", "first");
        other.set("a", "second");
        list.delete("a");
        assertTrue(list.isEmpty());
        assertEquals("second", other.get("a"));
        assertEquals(1, other.size());
    }

    @Test
    void emptyStringKeyIsARealEntryAndNotTheSentinel() {
        list.set("", "empty-key");
        list.set("a", "A");
        assertEquals("empty-key", list.get(""));
        assertTrue(list.containsKey(""));
        list.set("", "updated");
        assertEquals("updated", list.get(""));
        assertEquals(2, list.size());
        list.delete("");
        assertFalse(list.containsKey(""));
        assertEquals("A", list.get("a"));
        assertEquals(1, list.size());
    }

    @Test
    void keysWithSharedPrefixesCaseAndUnicodeRemainDistinct() {
        List<String> keys = List.of("a", "aa", "aaa", "ab", "A", "á", "ñ", "树", "🌳", " ", "\u0000");
        for (String key : keys) list.set(key, "value:" + key);
        assertEquals(keys.size(), list.size());
        for (String key : keys) assertEquals("value:" + key, list.get(key), key);
        list.delete("aa");
        assertEquals("value:a", list.get("a"));
        assertEquals("value:aaa", list.get("aaa"));
        assertFalse(list.containsKey("aa"));
    }

    @Test
    void longKeysWithCommonPrefixRemainDistinct() {
        String prefix = "x".repeat(10_000);
        list.set(prefix + "a", "A");
        list.set(prefix + "b", "B");
        assertEquals("A", list.get(prefix + "a"));
        assertEquals("B", list.get(prefix + "b"));
        assertNull(list.get(prefix));
        assertEquals(2, list.size());
    }

    @Test
    void emptyStringValueIsPreserved() {
        list.set("a", "");
        assertEquals("", list.get("a"));
        assertTrue(list.containsKey("a"));
    }

    @Test
    void nullValueIsPresentAndCanBeUpdatedAndDeleted() {
        // The current API allows null values; presence is checked with containsKey.
        list.set("a", null);
        assertNull(list.get("a"));
        assertTrue(list.containsKey("a"));
        assertEquals(1, list.size());
        list.set("a", "value");
        assertEquals("value", list.get("a"));
        list.set("a", null);
        assertNull(list.get("a"));
        assertTrue(list.containsKey("a"));
        assertEquals(1, list.size());
        list.delete("a");
        assertFalse(list.containsKey("a"));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullKeysAreRejectedWithoutChangingState(boolean populated) {
        if (populated) list.set("a", "A");
        assertThrows(NullPointerException.class, () -> list.set(null, "value"));
        assertThrows(NullPointerException.class, () -> list.get(null));
        assertThrows(NullPointerException.class, () -> list.containsKey(null));
        assertThrows(NullPointerException.class, () -> list.delete(null));
        assertEquals(populated ? 1 : 0, list.size());
        assertEquals(!populated, list.isEmpty());
        assertEquals(populated ? "A" : null, list.get("a"));
        list.set("b", "B");
        assertEquals("B", list.get("b"));
    }

    @Test
    void insertionAfterShortNodeWorksWhenHigherLevelsExist() {
        // Heights: a=0, z=1. The predecessor for b has no forward[1].
        list = withInitialCoinFlips(false, true, false);
        list.set("a", "A");
        list.set("z", "Z");
        assertDoesNotThrow(() -> list.set("b", "B"));
        assertEquals("A", list.get("a"));
        assertEquals("B", list.get("b"));
        assertEquals("Z", list.get("z"));
        assertEquals(3, list.size());
    }

    @Test
    void updatingShortNodeDoesNotCreateHiddenDuplicate() {
        // Heights: a=1, b=0. Looking at level 1 would miss the existing b.
        list = withInitialCoinFlips(true, false, false);
        list.set("a", "A");
        list.set("b", "old");
        list.set("b", "new");
        assertEquals("new", list.get("b"));
        assertEquals(2, list.size());
        list.delete("b");
        assertNull(list.get("b"));
        assertFalse(list.containsKey("b"));
        assertEquals(1, list.size());
    }

    @Test
    void removingTallestNodeAllowsFurtherInsertionAndSearch() {
        // Heights: a=0, b=2. Removing b empties both upper levels.
        list = withInitialCoinFlips(false, true, true, false);
        list.set("a", "A");
        list.set("b", "B");
        list.delete("b");
        list.set("c", "C");
        assertEquals("A", list.get("a"));
        assertEquals("C", list.get("c"));
        assertFalse(list.containsKey("b"));
        assertEquals(2, list.size());
    }
}
