package fielden.teltonika;

/**
 * Raw AVL message data from AVL trackers.
 * 
 * @author TG Team
 * 
 */
public class AvlData {

    public static final int BASE_ELEMENT_LENGTH = 8 + 1 + AvlGpsElement.BASE_ELEMENT_LENGTH + AvlIoElement.BASE_ELEMENT_LENGTH;

    private final int capacity;
    private final long timestamp;
    private final byte priority;
    private final AvlGpsElement gps;
    private final AvlIoElement io;

    public AvlData(final long timestamp, final byte priority, final AvlGpsElement gps, final AvlIoElement io, final int capacity) {
        this.timestamp = timestamp;
        this.priority = priority;
        this.gps = gps;
        this.io = io;
        this.capacity = capacity > 0 ? capacity : BASE_ELEMENT_LENGTH + //
                io.byteIo.length * AvlIoElement.ByteIoElement.BASE_ELEMENT_LENGTH + //
                io.shortIo.length * AvlIoElement.ShortIoElement.BASE_ELEMENT_LENGTH + //
                io.intIo.length * AvlIoElement.IntIoElement.BASE_ELEMENT_LENGTH + //
                io.longIo.length * AvlIoElement.LongIoElement.BASE_ELEMENT_LENGTH;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte getPriority() {
        return priority;
    }

    public AvlGpsElement getGps() {
        return gps;
    }

    public AvlIoElement getIo() {
        return io;
    }

    public int getCapacity() {
        return capacity;
    }

}
