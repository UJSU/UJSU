package ujsu;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import ujsu.enums.CodedEnum;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

@Configuration
public class DataJdbcConfig extends AbstractJdbcConfiguration {

	@Override
	protected List<?> userConverters() {
		return findCodedEnums().stream().flatMap(enumClass -> createConverters(enumClass).stream())
				.collect(Collectors.toList());
	}

	private List<Class<? extends Enum<?>>> findCodedEnums() {
		try (ScanResult scanResult = new ClassGraph().enableClassInfo().acceptPackages("ujsu.enums").scan()) {

			return scanResult.getClassesImplementing(CodedEnum.class.getName()).loadClasses().stream()
					.filter(Class::isEnum).map(cls -> (Class<? extends Enum<?>>) cls).collect(Collectors.toList());
		}
	}

	private List<Converter<?, ?>> createConverters(Class<? extends Enum<?>> enumClass) {
		try {
			Method fromCodeMethod = enumClass.getMethod("fromCode", int.class);
			if (!fromCodeMethod.getReturnType().equals(enumClass))
				throw new IllegalArgumentException("fromCode must return " + enumClass.getName());

			Converter<Integer, Enum<?>> readConverter = new Converter<Integer, Enum<?>>() {
	            @Override
	            public Enum<?> convert(Integer source) {
	                if (source == null) return null;
	                try {
	                    return (Enum<?>) fromCodeMethod.invoke(null, source);
	                } catch (Exception e) {
	                    throw new RuntimeException(
	                        "Failed to convert code " + source + " to enum " + enumClass.getSimpleName(), e);
	                }
	            }
	        };

	        Converter<Enum<?>, Integer> writeConverter = new Converter<Enum<?>, Integer>() {
	            @Override
	            public Integer convert(Enum<?> source) {
	                if (source == null)
	                	return null;
	                if (!(source instanceof CodedEnum))
	                    throw new IllegalArgumentException("Enum must implement CodedEnum: " + enumClass.getName());
	                return ((CodedEnum) source).getCode();
	            }
	        };

			return Arrays.asList(readConverter, writeConverter);

		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"Enum " + enumClass.getSimpleName() + " must have static fromCode(int) method", e);
		}
	}
}