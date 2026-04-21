package RespParser;

import java.util.InvalidPropertiesFormatException;

public record RespStreamId(long timestamp, long sequence) implements Comparable<RespStreamId> {

    public static RespStreamId parse(String id) {
        String[] parts = id.split("-");
        return new RespStreamId(
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1])
        );
    }

    public static boolean validateStreamId(String streamId) throws NumberFormatException, InvalidPropertiesFormatException {
        if (streamId.equals("*")) return true;

        String[] streamIdArray = streamId.split("-");
        if (streamIdArray.length != 2) return false;

        long timestamp = Long.parseLong(streamIdArray[0]);
        if (streamIdArray[1].equals("*")) return true;
        long sequence = Long.parseLong(streamIdArray[1]);

        if (timestamp == 0 && sequence == 0)
            throw new InvalidPropertiesFormatException("ERR The ID specified in XADD must be greater than 0-0");

        return true;
    }


    @Override
    public int compareTo(RespStreamId other) {
        int cmp = Long.compare(this.timestamp, other.timestamp);
        if (cmp != 0) return cmp;
        return Long.compare(this.sequence, other.sequence);
    }

    @Override
    public String toString() {
        return timestamp + "-" + sequence;
    }

}
