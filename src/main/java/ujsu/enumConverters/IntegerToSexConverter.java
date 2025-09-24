package ujsu.enumConverters;

import org.springframework.core.convert.converter.Converter;

import ujsu.enums.Sex;

public class IntegerToSexConverter implements Converter<Integer, Sex> {

	@Override
	public Sex convert(Integer source) {
		return source != null ? Sex.fromCode(source) : null;
	}
}