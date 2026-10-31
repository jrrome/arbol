package skiplist;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

class SkipListIterator implements Iterator<Map.Entry<byte[], byte[]>> {

    Node nextNode;

    SkipListIterator(Node firstNode) {
        this.nextNode = firstNode;
    }

    @Override
    public boolean hasNext() {
        return nextNode != null;
    }

    @Override
    public Map.Entry<byte[], byte[]> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Node current = nextNode;
        nextNode = nextNode.forward[0];
        return new AbstractMap.SimpleImmutableEntry<>(current.key.clone(), current.value.clone());
    }
}
