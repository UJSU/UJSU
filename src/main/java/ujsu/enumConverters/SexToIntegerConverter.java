package ujsu.enumConverters;

import org.springframework.core.convert.converter.Converter;

import ujsu.enums.Sex;

public class SexToIntegerConverter implements Converter<Sex, Integer> {
    @Override
    public Integer convert(Sex sex) {
        return sex != null ? sex.getCode() : null;
    }
}