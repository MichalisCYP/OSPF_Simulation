package Model;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LSDB {

    private final Map<String, LSA> lsaMap;

    public LSDB() {
        this.lsaMap = new HashMap<>();
    }

    public void addLSA(LSA lsa) {
        lsaMap.put(lsa.getAdvertisingRouterId(), lsa);
    }

    public LSA getLSA(String routerId) {
        return lsaMap.get(routerId);
    }

    public boolean hasLSA(String routerId) {
        return lsaMap.containsKey(routerId);
    }

    public Collection<LSA> getAllLSAs() {
        return lsaMap.values();
    }
}