package ujsu.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EnglishKnowlegeLevel implements CodedEnum {
	NOT_REQUIRED(0, "Не требуется"), A1(1, "A1"), B1(2, "A2"), B2(3, "B2"), C1(4, "C1"), C2(5, "C2");
	
	private final int code;
	private final String displayValue;

	private static final Map<Integer, EnglishKnowlegeLevel> BY_CODE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(EnglishKnowlegeLevel::getCode, Function.identity()));

	public static EnglishKnowlegeLevel fromCode(int code) {
		EnglishKnowlegeLevel englishKnowlegeLevel = BY_CODE.get(code);
		if (englishKnowlegeLevel == null)
			throw new IllegalArgumentException("Unknown code: " + code + " for enum: " + EnglishKnowlegeLevel.class.getSimpleName());
		return englishKnowlegeLevel;
	}
}