package skiplist;

import java.lang.reflect.Field;
import java.util.Random;

final class SkipListTestSupport {
    private SkipListTestSupport() {}

    static SkipList seededList(long seed) {
        SkipList list = new SkipList();
        // Test-only access: keep both levels and operations reproducible without
        // changing the production API. Replace with constructor injection if added.
        try {
            Field field = SkipList.class.getDeclaredField("random");
            field.setAccessible(true);
            ((Random) field.get(list)).setSeed(seed);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not seed the skip list", e);
        }
        return list;
    }

    static SkipList withInitialCoinFlips(boolean... flips) {
        for (long seed = 0; seed < 100_000; seed++) {
            Random random = new Random(seed);
            boolean matches = true;
            for (boolean flip : flips) {
                if (random.nextBoolean() != flip) {
                    matches = false;
                    break;
                }
            }
            if (matches) return seededList(seed);
        }
        throw new AssertionError("No seed found for requested initial levels");
    }
}
