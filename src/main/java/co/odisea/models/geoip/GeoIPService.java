package co.odisea.models.geoip;

import co.odisea.Shield;
import co.odisea.api.mongodb.annotations.ObjectClass;
import co.odisea.api.mongodb.models.nickname.Nickname;
import co.odisea.framework.persistence.mongodb.MongoPlayerService;
import co.odisea.utils.HttpUtils;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ObjectClass(GeoIP.class)
public class GeoIPService extends MongoPlayerService<GeoIP> {
    private final static Map<UUID, GeoIP> cache = new ConcurrentHashMap<>();

    public Map<UUID, GeoIP> getCache() {
        return cache;
    }

    private static String URL = "https://api.ipstack.com/%s?access_key=%s";
    private static final String KEY = Shield.getInstance().getConfig().getString("tokens.ipstack");
    private static final List<String> ignore = List.of();

    static {
        Shield.getInstance().addConfigDefault("tokens.ipstack", "abcdef");
    }

    @SneakyThrows
    public GeoIP request(UUID uuid, String ip) {
        GeoIP geoip = get(uuid);

        if (geoip.isOnline()) {
            if (!ip.equals(geoip.getIp())) {
                geoip = call(uuid, ip);
                save(geoip);
            }
        }

        if (geoip.getTimestamp() == null) {
            geoip = call(uuid, ip);
            save(geoip);
        }

        cache(geoip);
        return geoip;
    }

    @SneakyThrows
    private GeoIP call(UUID uuid, String ip) {
        GeoIP original = get(uuid);

        if (ignore.contains(uuid.toString()))
            return original;

        Shield.log("Requesting GeoIP info for " + Nickname.of(uuid) + " (" + ip + ")");

        GeoIP geoip = HttpUtils.mapJson(GeoIP.class, URL, ip, KEY);
        geoip.setUuid(uuid);
        geoip.setTimestamp(LocalDateTime.now());
        geoip.setSecurity(original.getSecurity());
        geoip.setMitigated(original.isMitigated());
        geoip.setTimeFormat(original.getTimeFormat());
        getCache().put(uuid, geoip);
        return geoip;
    }

    public List<GeoIP> getPlayers(String ip) {
        return getAll().stream().filter(geoIP -> geoIP.getIp().equals(ip)).collect(Collectors.toList());
    }

    public List<GeoIP> getAll() {
        try (var cursor = database.createQuery(GeoIP.class).find()) {
            return cursor.toList();
        }
    }

    @Override
    public void save(GeoIP geoip) {
        if (geoip != null && geoip.getIp() != null)
            super.save(geoip);
    }

}
