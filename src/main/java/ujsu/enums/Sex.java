package ujsu.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Sex implements CodedEnum{
	NULL(0, "Не указан"), MALE(1, "Мужской"), FEMALE(2, "Женский");

	private final int code;
	private final String displayName;

	private static final Map<Integer, Sex> BY_CODE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(Sex::getCode, Function.identity()));

	public static Sex fromCode(int code) {
		Sex sex = BY_CODE.get(code);
		if (sex == null)
			throw new IllegalArgumentException("Unknown code: " + code + " for enum: " + Sex.class.getSimpleName());
		return sex;
	}

}