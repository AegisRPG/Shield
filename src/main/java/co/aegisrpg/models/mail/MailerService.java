package co.aegisrpg.models.mail;

import co.aegisrpg.api.mongodb.annotations.ObjectClass;
import co.aegisrpg.framework.persistence.mongodb.MongoPlayerService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Mailer.class)
public class MailerService extends MongoPlayerService<Mailer> {
    private final static Map<UUID, Mailer> cache = new ConcurrentHashMap<>();

    public Map<UUID, Mailer> getCache() {
        return cache;
    }

    @Override
    protected boolean deleteIf(Mailer mailer) {
        return mailer.getMail().isEmpty() && mailer.getPendingMail().isEmpty();
    }

}
