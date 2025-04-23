package Model;

import java.util.HashMap;
import java.util.Map;

public class LSA {
    // Link State Advertisement (LSA) class
    // This class represents a link state advertisement
    //The router's LSDB stores all the LSAs received from neighbours
    //LSAs include the router ID, sequence number, and a map of links with their costsq

    private final String advertisingRouterId;
    private final Map<String, Integer> links; // Map of link IDs to their costs
    private final long sequenceNumber;

    public LSA(String advertisingRouterId, Map<String, Integer> links, long sequenceNumber) {
        this.advertisingRouterId = advertisingRouterId;
        this.links = links;
        this.sequenceNumber = sequenceNumber;
    }

    public String getAdvertisingRouterId() {
        return advertisingRouterId;
    }

    public Map<String, Integer> getLinks() {
        System.out.println("[LSA " + java.time.LocalDateTime.now() + " ] Links: " + links);
        return links;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(advertisingRouterId).append(";").append(sequenceNumber).append(";");
        for (Map.Entry<String, Integer> entry : links.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        return sb.toString();
    }

    public static LSA deserialize(String data) {
        String[] parts = data.split(";");
        String routerId = parts[0];
        long seqNum = Long.parseLong(parts[1]);
        Map<String, Integer> linkMap = new HashMap<>();
        String[] linkEntries = parts[2].split(",");
        for (String entry : linkEntries) {
            String[] linkParts = entry.split(":");
            linkMap.put(linkParts[0], Integer.valueOf(linkParts[1]));
        }

        System.out.println("[LSA " + java.time.LocalDateTime.now() + " ] Deserialized LSA: " + routerId + " with sequence number " + seqNum);
        System.out.println("[LSA " + java.time.LocalDateTime.now() + " ] Links: " + linkMap);

        return new LSA(routerId, linkMap, seqNum);
    }
}
