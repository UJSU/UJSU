package ujsu.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StudyType implements CodedEnum {
	BACHELOUR(1, "Бакалавриат"), SPECIALIST(2, "Специалитет"), MASTER(3, "Магистратура"), GRADUATE(4, "Аспирантура");

	private final int code;
	private final String displayValue;

	private static final Map<Integer, StudyType> BY_CODE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(StudyType::getCode, Function.identity()));

	public static StudyType fromCode(int code) {
		StudyType studyType = BY_CODE.get(code);
		if (studyType == null)
			throw new IllegalArgumentException("Unknown code: " + code + " for enum: " + Sex.class.getSimpleName());
		return studyType;
	}
}
