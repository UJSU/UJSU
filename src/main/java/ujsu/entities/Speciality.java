package ujsu.entities;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Speciality {

	@Id
	private Integer id;
	
	private String code;
	private String name;
	public Speciality(String code, String name) {
            // this.code ссылается на поле класса, code - на параметр конструктора
            this.code = code;
            this.name = name;
        }
		public String getCode() {
            return code;
        }

        // Геттер для названия - возвращает значение поля
        public String getName() {
            return name;
        }
}