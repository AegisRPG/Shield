package co.odisea.models.nerd;

import co.odisea.api.mongodb.annotations.ObjectClass;
import co.odisea.api.mongodb.models.hours.HoursService;
import co.odisea.framework.exceptions.postconfigured.InvalidInputException;
import co.odisea.framework.persistence.mongodb.MongoPlayerService;
import co.odisea.utils.Utils;
import dev.morphia.query.Query;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@ObjectClass(Nerd.class)
public class NerdService extends MongoPlayerService<Nerd> {
	private final static Map<UUID, Nerd> cache = new ConcurrentHashMap<>();

	public Map<UUID, Nerd> getCache() {
		return cache;
	}

	public List<Nerd> find(String partialName) {
		Query<Nerd> query = database.createQuery(Nerd.class);
		query.and(query.criteria("pastNames").containsIgnoreCase(partialName));
		long count = query.count();
		if (count > 50)
			throw new InvalidInputException("Too many name matches for &e" + partialName + " &c(" + count + ")");

		try (var cursor = query.find()) {
			final List<Nerd> fuzzyMatches = cursor.toList();
			final List<Nerd> exactMatches = new ArrayList<>();

			for (Nerd nerd : fuzzyMatches)
				for (String pastName : nerd.getPastNames())
					if (pastName.equalsIgnoreCase(partialName))
						exactMatches.add(nerd);

			Map<UUID, Integer> hoursMap = new HashMap<>() {{
				HoursService service = new HoursService();
				for (Nerd nerd : exactMatches.isEmpty() ? fuzzyMatches : exactMatches)
					put(nerd.getUuid(), service.get(nerd).getTotal());
			}};

			Set<UUID> sorted = Utils.sortByValueReverse(hoursMap).keySet();
			return new ArrayList<>(sorted).stream()
				.map(Nerd::of)
				.collect(toList());
		}
	}

	@Nullable
	public Nerd findExact(String name) {
		Query<Nerd> query = database.createQuery(Nerd.class);
		query.and(query.criteria("name").equalIgnoreCase(name));
		try (var cursor = query.find()) {
			return cursor.tryNext();
		}
	}

	public List<Nerd> getNerdsWithBirthdays() {
		Query<Nerd> query = database.createQuery(Nerd.class);
		query.and(query.criteria("birthday").notEqual(null));
		try (var cursor = query.find()) {
			return cursor.toList();
		}
	}

	public Nerd getFromAlias(String alias) {
		Query<Nerd> query = database.createQuery(Nerd.class);
		query.and(query.criteria("aliases").hasThisOne(alias));
		try (var cursor = query.find()) {
			return cursor.tryNext();
		}
	}

}
