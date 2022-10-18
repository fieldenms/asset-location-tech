package fielden.teltonika.server;

public final class Crc16 {

    private final int polynomial;
    private int crc = 0;

    public Crc16(final int polynomial) {
        this.polynomial = polynomial;
    }

    public void update(int value) {
        for (int i = 0; i < 8; i++) {
            final int add = (crc ^ value) & 1;
            crc >>>= 1;
            value >>>= 1;
            if (add == 1)
                crc ^= polynomial;
        }
        crc &= 0xFFFF;
    }

    public void update(final byte[] data) {
        for (final byte b : data) {
            update(b);
        }
    }

    public void update(final int offset, final int length, final byte[] data) {
        for (int i = offset; i < offset + length; i++) {
            update(data[i]);
        }
    }

    public void reset() {
        setCrc(0);
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(final int crc) {
        this.crc = crc;
    }

}
