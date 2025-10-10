package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ujsu.entities.University;
import ujsu.entities.Speciality;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/fragments")
public class SearchFragmentController {

    @GetMapping("/get-universities-by-input")
    @ResponseBody
    public String getUniversitiesByInput(@RequestParam("university") String input) {
        System.out.println("=== SEARCH REQUEST: '" + input + "' ===");
        List<String> allUniversities = List.of(
            "Московский государственный университет имени М.В. Ломоносова",
            "Национальный исследовательский университет \"Высшая школа экономики\"",
            "Московский физико-технический институт",
            "Московский государственный технический университет имени Н.Э. Баумана",
            "Санкт-Петербургский государственный университет",
            "Новосибирский национальный исследовательский государственный университет",
            "Уральский федеральный университет",
            "Казанский федеральный университет", 
            "Томский государственный университет",
            "Российский университет дружбы народов"
        );
    List<String> filteredUniversities = allUniversities.stream()
            .filter(univ -> univ.toLowerCase().contains(input.toLowerCase()))
            .limit(3) // Ограничиваем до 3 элементов
            .collect(Collectors.toList());
        
        System.out.println("Found " + filteredUniversities.size() + " universities");
        
        StringBuilder html = new StringBuilder();
        
        // ВСЕГДА возвращаем контейнер с классом suggestions-list
        html.append("<div class=\"suggestions-list\">");
        
        if (!filteredUniversities.isEmpty()) {
            for (String university : filteredUniversities) {
                html.append("<button class=\"suggestion-item\" onclick=\"selectSuggestion(this, 'universityInput')\" type=\"button\">")
                    .append(university)
                    .append("</button>");
            }
            
            // Сообщение если есть еще результаты
            long totalFound = allUniversities.stream()
                .filter(univ -> univ.toLowerCase().contains(input.toLowerCase()))
                .count();
            if (totalFound > 3) {
                html.append("<div class=\"more-results-message\">")
                    .append("... и еще ").append(totalFound - 3).append(" вариантов")
                    .append("</div>");
            }
        } else {
            // Сообщение когда ничего не найдено
            html.append("<div class=\"no-suggestions\">")
                .append("Университет не найден")
                .append("</div>");
        }
        
        html.append("</div>"); // Закрываем suggestions-list
        
        return html.toString();
    }
    private final List<Speciality> allSpecialities = List.of(
        new Speciality("09.03.04", "Программная инженерия"),
        new Speciality("09.03.01", "Информатика и вычислительная техника"),
        new Speciality("09.03.02", "Информационные системы и технологии"),
        new Speciality("09.03.03", "Прикладная информатика"),
        new Speciality("01.03.02", "Прикладная математика и информатика"),
        new Speciality("10.03.01", "Информационная безопасность"),
        new Speciality("38.03.05", "Бизнес-информатика"),
        new Speciality("09.04.04", "Программная инженерия (магистратура)"),
        new Speciality("09.04.01", "Информатика и вычислительная техника (магистратура)")
    );
    @GetMapping("/get-specialities-by-input")
    @ResponseBody
    public String getSpecialitiesByInput(@RequestParam("speciality") String input) {
        System.out.println("=== SPECIALITY SEARCH REQUEST: '" + input + "' ===");
        System.out.println("Input length: " + input.length());
        System.out.println("All specialities count: " + allSpecialities.size());
        
        List<Speciality> filteredSpecialities = allSpecialities.stream()
            .filter(speciality -> {
                String lowerInput = input.toLowerCase();
                boolean matchesCode = speciality.getCode().toLowerCase().contains(lowerInput);
                boolean matchesName = speciality.getName().toLowerCase().contains(lowerInput);
                boolean matchesStartName = speciality.getName().toLowerCase().startsWith(lowerInput);
                boolean matchesStartCode = speciality.getCode().startsWith(input);
                
                boolean result = matchesCode || matchesName || matchesStartName || matchesStartCode;
                if (result) {
                    System.out.println("Matched: " + speciality.getCode() + " - " + speciality.getName());
                }
                return result;
            })
            .limit(10)
            .collect(Collectors.toList());
        
        System.out.println("Found " + filteredSpecialities.size() + " specialities");
        
        StringBuilder html = new StringBuilder();
        if (!filteredSpecialities.isEmpty()) {
            html.append("<div class=\"suggestions-list\">");
            for (Speciality speciality : filteredSpecialities) {
                html.append("<button class=\"suggestion-item\" onclick=\"selectSpecialitySuggestion(this)\" type=\"button\" ")
                    .append("data-code=\"").append(speciality.getCode()).append("\" ")
                    .append("data-name=\"").append(speciality.getName()).append("\">")
                    .append("<span class=\"speciality-code\">").append(speciality.getCode()).append("</span>")
                    .append(" - ")
                    .append(speciality.getName())
                    .append("</button>");
            }
        } else {
            html.append("<div class=\"no-suggestions\">")
                .append("Специальность не найдена")
                .append("</div>");
        }
        html.append("</div>");
        
        return html.toString();
    }
}