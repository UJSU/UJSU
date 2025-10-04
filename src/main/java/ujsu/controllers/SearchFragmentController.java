package ujsu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ujsu.entities.University;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/fragments")
public class SearchFragmentController {

    @GetMapping("/get-universities-by-input")
    @ResponseBody
    public String getUniversitiesByInput(@RequestParam("university") String input) {
        System.out.println("=== SEARCH REQUEST: '" + input + "' ===");
        
        // Временные тестовые данные
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
        
        // Фильтруем по введенному тексту
        List<String> filteredUniversities = allUniversities.stream()
            .filter(univ -> univ.toLowerCase().contains(input.toLowerCase()))
            .collect(Collectors.toList());
        
        System.out.println("Found " + filteredUniversities.size() + " universities");
        
        // Генерируем HTML вручную
        StringBuilder html = new StringBuilder();
        if (!filteredUniversities.isEmpty()) {
            html.append("<div class=\"suggestions-list\">");
            for (String university : filteredUniversities) {
                html.append("<button class=\"suggestion-item\" onclick=\"selectSuggestion(this, 'universityInput')\" type=\"button\">")
                    .append(university)
                    .append("</button>");
            }
            html.append("</div>");
        }
        
        return html.toString();
    }
}
