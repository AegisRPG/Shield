package co.odisea.framework.persistence.serializer.mongodb;

import co.odisea.Shield;
import co.odisea.features.chat.ChatManager;
import co.odisea.models.chat.Channel;
import co.odisea.models.chat.PrivateChannel;
import co.odisea.models.chat.PublicChannel;
import com.mongodb.BasicDBList;
import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChannelConverter extends TypeConverter implements SimpleValueConverter {

	public ChannelConverter(Mapper mapper) {
		super(Channel.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value == null) return null;

		if (value instanceof PublicChannel publicChannel)
			return new PublicChannelConverter().encode(publicChannel, optionalExtraInfo);
		if (value instanceof PrivateChannel privateChannel)
			return new PrivateChannelConverter().encode(privateChannel, optionalExtraInfo);

		Shield.warn("Unknown class for channel in encoding: " + value.getClass() + " (" + value + ")");
		return null;
	}

	@Override
	public Object decode(Class<?> aClass, Object value, MappedField mappedField) {
		if (value == null) return null;

		if (value instanceof String name)
			return ChatManager.getChannel(name);

		if (value instanceof BasicDBList list) {
			return new PrivateChannel(list.stream().map(Object::toString).toList());
		}

		Shield.warn("Unknown class for channel in decoding: " + value.getClass() + " (" + value + ")");
		return null;
	}

}
