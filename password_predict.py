import sys
import pickle
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler

# ТОЧНАЯ КОПИЯ ВАШЕГО КЛАССА из password_model.py
class BalancedPasswordClassifier:
    def __init__(self):
        self.model = RandomForestClassifier(
            n_estimators=80,
            max_depth=12,
            min_samples_split=30,
            min_samples_leaf=15,
            max_features=0.6,
            random_state=42,
            n_jobs=-1
        )
        self.scaler = StandardScaler()
        self.is_fitted = False
    
    def extract_balanced_features(self, password):
        """ТОЧНАЯ КОПИЯ МЕТОДА из password_model.py"""
        if not isinstance(password, str):
            password = str(password)
        
        features = []
        
        # 1. Длина (категории)
        length = len(password)
        if length <= 6:
            length_category = 0
        elif length <= 10:
            length_category = 1
        else:
            length_category = 2
        features.append(length_category)
        
        # 2. Состав символов
        digit_count = sum(1 for c in password if c.isdigit())
        upper_count = sum(1 for c in password if c.isupper())
        lower_count = sum(1 for c in password if c.islower())
        special_count = sum(1 for c in password if not c.isalnum())
        
        features.extend([digit_count, upper_count, lower_count, special_count])
        
        # 3. Пропорции
        total_chars = max(length, 1)
        digit_ratio = digit_count / total_chars
        upper_ratio = upper_count / total_chars
        lower_ratio = lower_count / total_chars
        special_ratio = special_count / total_chars
        
        features.extend([digit_ratio, upper_ratio, lower_ratio, special_ratio])
        
        # 4. Комбинации символов
        has_upper_lower = int(upper_count > 0 and lower_count > 0)
        has_letter_digit = int((upper_count > 0 or lower_count > 0) and digit_count > 0)
        has_special = int(special_count > 0)
        has_all_three = int(has_upper_lower and has_letter_digit and has_special)
        
        features.extend([has_upper_lower, has_letter_digit, has_special, has_all_three])
        
        # 5. Разнообразие
        unique_chars = len(set(password))
        unique_ratio = unique_chars / total_chars if total_chars > 0 else 0
        
        # Энтропия
        char_counts = {}
        for char in password:
            char_counts[char] = char_counts.get(char, 0) + 1
        
        entropy = 0
        for count in char_counts.values():
            p = count / total_chars
            if p > 0:
                entropy -= p * np.log2(p)
        
        features.extend([unique_ratio, entropy])
        
        # 6. Сложность
        complexity_score = min(has_upper_lower + has_letter_digit + has_special + int(length >= 8), 4)
        features.append(complexity_score)
        
        balance_score = 1 - (max(digit_ratio, upper_ratio, lower_ratio, special_ratio) - 
                           min(digit_ratio, upper_ratio, lower_ratio, special_ratio))
        features.append(balance_score)
        
        return features
    
    def predict(self, password):
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_balanced_features(password)
        features_scaled = self.scaler.transform([features])
        return self.model.predict(features_scaled)[0]
    
    def predict_proba(self, password):
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_balanced_features(password)
        features_scaled = self.scaler.transform([features])
        return self.model.predict_proba(features_scaled)[0]

# Основной код предсказания
def main():
    try:
        # Загружаем обученную модель
        with open('balanced_password_classifier.pkl', 'rb') as f:
            classifier = pickle.load(f)
        print("DEBUG: Model loaded successfully", file=sys.stderr)
    except Exception as e:
        print(f"DEBUG: Error loading model: {e}", file=sys.stderr)
        sys.exit(1)
    
    print("DEBUG: Ready for predictions. Send passwords via stdin...", file=sys.stderr)
    
    try:
        while True:
            # Читаем пароль из stdin
            line = sys.stdin.readline().strip()
            if not line:
                break
            
            # Получаем предсказание
            prediction = classifier.predict(line)
            probabilities = classifier.predict_proba(line)
            
            strength_map = {0: "weak", 1: "medium", 2: "strong"}
            strength = strength_map.get(prediction, "medium")
            confidence = float(np.max(probabilities))
            
            # Форматируем ответ для Java
            result = f"{strength},{confidence:.4f},{probabilities[0]:.4f},{probabilities[1]:.4f},{probabilities[2]:.4f}"
            print(result)
            sys.stdout.flush()
            
    except Exception as e:
        print(f"DEBUG: Error in main loop: {e}", file=sys.stderr)
    finally:
        print("DEBUG: Python predictor finished", file=sys.stderr)

if __name__ == "__main__":
    main()