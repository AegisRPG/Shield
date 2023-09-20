package co.aegisrpg.framework.persistence.serializer.mongodb;

import co.aegisrpg.utils.SerializationUtils.Json;
import com.mongodb.BasicDBObject;
import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import static co.aegisrpg.utils.SerializationUtils.Json.serialize;

public class ItemStackConverter extends TypeConverter implements SimpleValueConverter {

	public ItemStackConverter(Mapper mapper) {
		super(ItemStack.class, CraftItemStack.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null) return null;

		return BasicDBObject.parse(Json.toString(serialize((ItemStack) value)));
	}

	@Override
	public Object decode(Class<?> aClass, Object value, MappedField mappedField) {
		if (value == null) return null;
		return Json.deserializeItemStack(((BasicDBObject) value).toJson());
	}

}
