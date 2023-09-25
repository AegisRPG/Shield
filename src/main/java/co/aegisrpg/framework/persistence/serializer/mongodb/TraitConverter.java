package co.aegisrpg.framework.persistence.serializer.mongodb;

import co.aegisrpg.api.common.utils.ReflectionUtils;
import co.aegisrpg.api.mongodb.MongoService;
import co.aegisrpg.utils.Utils;
import com.mongodb.DBObject;
import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import lombok.SneakyThrows;
import net.citizensnpcs.api.trait.Trait;

import java.util.List;

public class TraitConverter extends TypeConverter implements SimpleValueConverter {

	public TraitConverter(Mapper mapper) {
		super(Utils.combine(List.of(Trait.class), ReflectionUtils.subTypesOf(Trait.class, Trait.class.getPackageName())).toArray(new Class[0]));
	}

	@Override
	@SneakyThrows
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (!(value instanceof Trait trait))
			return null;

		final DBObject serialize = MongoService.serialize(trait);
		serialize.put("className", trait.getClass().getName());
		return serialize;
	}

	@Override
	@SneakyThrows
	public Object decode(Class<?> aClass, Object value, MappedField mappedField) {
		return MongoService.deserialize((DBObject) value);
	}
}
