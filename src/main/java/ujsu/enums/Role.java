package ujsu.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role implements CodedEnum {
	STUDENT(1, "Студент"), ADMIN(2, "Администратор");

	private final int code;
	private final String displayName;

	private static final Map<Integer, Role> BY_CODE = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(Role::getCode, Function.identity()));

	public static Role fromCode(int code) {
		Role role = BY_CODE.get(code);
		if (role == null)
			throw new IllegalArgumentException("Unknown code: " + code + " for enum: " + Sex.class.getSimpleName());
		return role;
	}
}