package co.aegisrpg.framework.persistence.serializer.mysql;

import com.dieselpoint.norm.serialize.DbSerializable;
import co.aegisrpg.utils.SerializationUtils.Json;
import org.bukkit.Location;

public class LocationSerializer implements DbSerializable {

	@Override
	public String serialize(Object in) {
		return Json.serializeLocation((Location) in);
	}

	@Override
	public Location deserialize(String in) {
		return Json.deserializeLocation(in);
	}

}
