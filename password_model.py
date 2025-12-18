import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, confusion_matrix
from sklearn.preprocessing import StandardScaler
import pickle
import csv
import re

class BalancedPasswordClassifier:
    def __init__(self):
        self.model = RandomForestClassifier(
            n_estimators=50, 
            max_depth=10,
            min_samples_split=20,
            min_samples_leaf=10,
            max_features=0.7,
            random_state=42,
            n_jobs=-1  
        )
        self.scaler = StandardScaler()
        self.is_fitted = False
        self.weak_patterns = self.load_weak_patterns()
        self.common_weak_passwords = self.load_common_weak_passwords()
    
    def load_weak_patterns(self):
        """Загружает слабые паттерны"""
        return [
            'password', 'qwerty', 'admin', 'welcome', 'letmein',
            'monkey', 'dragon', 'baseball', 'football', 'mustang',
            'sunshine', 'princess', 'superman', 'batman', 'master',
            'hello', 'iloveyou', 'trustno1', 'shadow', 'ashley',
            'michael', 'jordan', 'charlie', 'donald', 'harley',
            'fuckyou', 'whatever', 'zaq1zaq1'
        ]
    
    def load_common_weak_passwords(self):
        """Загружает известные слабые пароли"""
        return [
            'password', '123456', '12345678', '123456789', '1234567890',
            'admin', 'administrator', 'qwerty', 'qwerty123', 'qwertyuiop',
            'letmein', 'welcome', 'monkey', 'dragon', 'baseball',
            'football', 'mustang', 'superman', 'batman', 'trustno1',
            'password123', 'password1', 'password1234', 'password12345',
            'admin123', 'admin1', 'admin1234', 'adminadmin',
            'welcome123', 'welcome1', 'letmein123', 'letmein1',
            '123123', '111111', '000000',
            'abc123', 'hello123', 'sunshine123'
        ]
    
    def extract_fast_features(self, password):
        """Сверхбыстрая экстракция признаков"""
        if not isinstance(password, str):
            password = str(password)
        
        length = len(password)
        features = np.zeros(12, dtype=np.float32)
        if length <= 6:
            features[0] = 0
        elif length <= 10:
            features[0] = 1
        else:
            features[0] = 2
        
        digit_count = upper_count = lower_count = special_count = 0
        for char in password:
            if char.isdigit():
                digit_count += 1
            elif char.isupper():
                upper_count += 1
            elif char.islower():
                lower_count += 1
            else:
                special_count += 1
        
        features[1] = digit_count
        features[2] = upper_count
        features[3] = lower_count
        features[4] = special_count
        
        total_chars = max(length, 1)
        features[5] = digit_count / total_chars
        features[6] = upper_count / total_chars
        features[7] = lower_count / total_chars
        features[8] = special_count / total_chars
        
        features[9] = 1.0 if (upper_count > 0 and lower_count > 0) else 0.0
        features[10] = 1.0 if ((upper_count > 0 or lower_count > 0) and digit_count > 0) else 0.0
        features[11] = 1.0 if special_count > 0 else 0.0
        
        return features
    
    def extract_features_batch(self, passwords):
        """Пакетная обработка для максимальной скорости"""
        features_list = np.zeros((len(passwords), 12), dtype=np.float32)
        
        for i, password in enumerate(passwords):
            features_list[i] = self.extract_fast_features(password)
        
        return features_list
    
    def train_fast(self, X, y):
        """Быстрое обучение"""
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y 
        )
        
        print(f"Обучаем на {len(X_train)} примерах...")
        X_train_scaled = self.scaler.fit_transform(X_train)
        X_test_scaled = self.scaler.transform(X_test)
        self.model.fit(X_train_scaled, y_train)
        self.is_fitted = True
        test_predictions = self.model.predict(X_test_scaled)
        test_accuracy = accuracy_score(y_test, test_predictions)
        
        print(f"Точность на тестовой выборке: {test_accuracy:.4f}")
        
        return test_accuracy
    
    def count_categories(self, password):
        """Считает количество категорий символов в пароле"""
        categories = 0
        
        if any(c.islower() for c in password):
            categories += 1  
        if any(c.isupper() for c in password):
            categories += 1  
        if any(c.isdigit() for c in password):
            categories += 1 
        if any(not c.isalnum() for c in password):
            categories += 1  
        
        return categories
    
    def is_in_blacklist(self, password):
        """Проверяет пароль по черному списку"""
        password_lower = password.lower()
        if password_lower in self.common_weak_passwords:
            return True
        
        for weak_pwd in self.common_weak_passwords:
            if (password_lower.startswith(weak_pwd) or 
                password_lower.endswith(weak_pwd)):
                if len(weak_pwd) >= len(password_lower) * 0.7:
                    return True
        
        return False
    
    def is_leet_variant(self, password, word):
        """Проверяет, является ли пароль leet-вариантом слова"""
        leet_to_normal = {
            '@': 'a', '$': 's', '1': 'i', '0': 'o', '3': 'e',
            '!': 'i', '7': 't', '8': 'b', '9': 'g', '4': 'a',
            '5': 's', '2': 'z', '6': 'b'
        }
        
        normalized = ''.join(leet_to_normal.get(c, c) for c in password.lower())
        return word in normalized
    
    def follows_weak_pattern(self, password):
        """Проверяет только явно слабые шаблоны"""
        password_lower = password.lower()
        
        if len(password) < 8:
            simple_patterns = [
                r'^[a-zA-Z]+[0-9]{1,3}$',          
                r'^[a-zA-Z]+[0-9]{1,3}[!@#$%^&*]?$',
                r'^[0-9]+[a-zA-Z]+$',             
            ]
            
            for pattern in simple_patterns:
                if re.match(pattern, password):
                    return True
        
        if len(password) < 10:
            leet_words = ['password', 'admin', 'test', 'root', 'login']
            for word in leet_words:
                if self.is_leet_variant(password, word):
                    return True
        
        for pattern in self.weak_patterns:
            if pattern in password_lower:
                word_len = len(pattern)
                pwd_len = len(password_lower)
                
                if word_len > pwd_len * 0.7:  # 70% или более
                    return True
        
        return False
    
    def rule_based_classification(self, password):
        """Классификация на основе ослабленных правил безопасности"""
        password_lower = password.lower()
        
        if len(password) < 6:
            return 0  # weak
        
        if self.is_in_blacklist(password):
            return 0  # weak
        
        if self.follows_weak_pattern(password):
            return 0  # weak
        
        if password.isdigit() and len(password) < 10:
            return 0  # weak
        
        if password.isalpha() and password_lower == password and len(password) < 8:
            return 0  # weak
        
        return None
    
    def predict_single(self, password):
        """Умный классификатор паролей с ослабленными правилами"""
        rule_based = self.rule_based_classification(password)
        if rule_based is not None:
            return rule_based
        
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_fast_features(password)
        features_scaled = self.scaler.transform([features])
        ml_prediction = self.model.predict(features_scaled)[0]
        
        if ml_prediction == 2:  # ML считает сильным
            categories = self.count_categories(password)
            if len(password) < 12 and categories < 4:
                return 1  
        
        return ml_prediction
    
    def predict_proba_single(self, password):
        """Вероятности классов (на основе ML или правил)"""
        rule_based = self.rule_based_classification(password)
        
        if rule_based is not None:
            if rule_based == 0:  # weak
                return np.array([0.90, 0.08, 0.02])  
            elif rule_based == 1:  # medium
                return np.array([0.05, 0.85, 0.10])  
            else:  
                return np.array([0.33, 0.34, 0.33])
        
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_fast_features(password)
        features_scaled = self.scaler.transform([features])
        return self.model.predict_proba(features_scaled)[0]
    
    def predict_batch(self, passwords):
        """Сверхбыстрое предсказание для батча паролей"""
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        predictions = np.zeros(len(passwords), dtype=int)
        
        for i, password in enumerate(passwords):
            rule_based = self.rule_based_classification(password)
            if rule_based is not None:
                predictions[i] = rule_based
            else:
                predictions[i] = -1
        
        ml_indices = np.where(predictions == -1)[0]
        if len(ml_indices) > 0:
            ml_passwords = [passwords[i] for i in ml_indices]
            features = self.extract_features_batch(ml_passwords)
            features_scaled = self.scaler.transform(features)
            ml_predictions = self.model.predict(features_scaled)
            
            for idx, ml_pred in zip(ml_indices, ml_predictions):
                if ml_pred == 2:  
                    categories = self.count_categories(passwords[idx])
                    if len(passwords[idx]) < 12 and categories < 4:
                        predictions[idx] = 1 
                    else:
                        predictions[idx] = 2
                else:
                    predictions[idx] = ml_pred
        
        return predictions
    
    def predict_proba_batch(self, passwords):
        """Быстрое предсказание вероятностей для батча"""
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_features_batch(passwords)
        features_scaled = self.scaler.transform(features)
        return self.model.predict_proba(features_scaled)


def load_data_fast(filename):
    """Сверхбыстрая загрузка данных с обработкой всех случаев"""
    print(f"Быстрая загрузка {filename}...")
    
    try:
        df = pd.read_csv(
            filename, 
            encoding='utf-8',
            on_bad_lines='skip',
            dtype={'strength': 'int8'}
        )
        
        if 'strength' not in df.columns or 'password' not in df.columns:
            print("Колонки не найдены, пробуем без заголовка...")
            df = pd.read_csv(
                filename,
                encoding='utf-8',
                header=None,
                names=['password', 'strength'],
                on_bad_lines='skip',
                dtype={'strength': 'int8'}
            )
    
    except Exception as e:
        print(f"Ошибка при загрузке: {e}")
        print("Пробуем альтернативный метод...")
        passwords = []
        strengths = []
        
        with open(filename, 'r', encoding='utf-8', errors='ignore') as f:
            for line_num, line in enumerate(f, 1):
                line = line.strip()
                if not line:
                    continue
                    
                parts = line.split(',')
                if line_num == 1 and ('password' in line.lower() or 'strength' in line.lower()):
                    continue
                if len(parts) >= 2:
                    try:
                        password = parts[0].strip()
                        strength = None
                        for part in parts[1:]:
                            part = part.strip()
                            if part in ['0', '1', '2']:
                                strength = int(part)
                                break
                        
                        if strength is not None:
                            passwords.append(password)
                            strengths.append(strength)
                    except:
                        continue
        
        df = pd.DataFrame({
            'password': passwords,
            'strength': strengths
        })
    df = df[df['strength'].isin([0, 1, 2])].copy()
    
    print(f"Загружено {len(df)} строк")
    print(f"Пример данных:")
    print(df.head())
    
    return df


def add_label_noise_fast(df, noise_fraction=0.10):
    """Быстрое добавление шума"""
    print(f"Добавляем {noise_fraction:.0%} шума...")
    
    np.random.seed(42)
    noisy_df = df.copy()
    
    n_noise = int(len(df) * noise_fraction)
    noise_indices = np.random.choice(len(df), n_noise, replace=False)
    current_labels = noisy_df.iloc[noise_indices]['strength'].values
    new_labels = np.random.randint(0, 3, size=len(current_labels))
    mask = new_labels == current_labels
    new_labels[mask] = (new_labels[mask] + 1) % 3
    
    noisy_df.iloc[noise_indices, noisy_df.columns.get_loc('strength')] = new_labels
    
    return noisy_df


def test_password_examples(classifier):
    """Тестируем классификатор на различных паролях"""
    print("\n" + "="*60)
    print("ТЕСТИРОВАНИЕ КЛАССИФИКАТОРА НА ПРИМЕРАХ")
    print("="*60)
    
    test_cases = [
        ('password', 0, 'Слишком простое слово'),
        ('Password123', 0, 'Слово+цифры - слабый паттерн (короткий)'),
        ('P@ssw0rd!', 0, 'Leet вариация password (короткая)'),
        ('Admin2024!', 1, 'Слово+год+спецсимвол - теперь средний'),
        ('123456', 0, 'Только цифры, короткий'),
        ('Aa1!', 0, 'Слишком короткий'),
        ('qwertyuiop', 0, 'Клавиатурная последовательность'),
        ('MyDogName2024', 1, 'Словосочетание+год - теперь средний'),
        ('CorrectHorseBatteryStaple', 1, 'Длинная фраза - средний'),
        ('Tr0ub4dor&3', 1, 'Сложный, но короткий - средний'),
        ('A1b2C3d4!@#', 2, 'Случайные символы - сильный'),
        ('Xk8&gP2#qL9$mN5', 2, 'Очень сложный - сильный'),
        ('SecurePass2024!Long', 1, 'Длинный, но с годом - средний'),
        ('J7#fK9$pL2&mN8@qR5', 2, 'Случайный, длинный, сложный - сильный'),
        ('sdfkjKJDJKSD123', 1, 'Длинный, с заглавными и цифрами - средний'),
        ('sadasd8305!df@sd', 1, 'Длинный, со спецсимволами, без заглавных - средний'),
    ]
    
    print(f"{'Пароль':<25} {'Предсказание':<12} {'Ожидалось':<10} {'Статус':<10}")
    print("-" * 60)
    
    results = []
    for password, expected, description in test_cases:
        try:
            prediction = classifier.predict_single(password)
            predicted_label = ['weak', 'medium', 'strong'][prediction]
            expected_label = ['weak', 'medium', 'strong'][expected]
            status = "✓" if prediction == expected else "✗"
            
            results.append({
                'password': password,
                'predicted': predicted_label,
                'expected': expected_label,
                'status': status
            })
            
            print(f"'{password:<23}' {predicted_label:<12} {expected_label:<10} {status:<10}")
        except Exception as e:
            print(f"'{password:<23}' ОШИБКА: {e}")
            results.append({
                'password': password,
                'predicted': 'ERROR',
                'expected': expected_label,
                'status': '✗'
            })
    correct = sum(1 for r in results if r['status'] == '✓')
    total = len(results)
    print(f"\nТочность на тестовых примерах: {correct}/{total} ({correct/total*100:.1f}%)")


def main_fast():
    print("=== УМНЫЙ КЛАССИФИКАТОР ПАРОЛЕЙ С ОСЛАБЛЕННЫМИ ПРАВИЛАМИ ===")
    
    df = load_data_fast('data.csv')
    
    print("\nРаспределение меток:")
    for strength, count in df['strength'].value_counts().items():
        print(f"  {strength}: {count}")
    noisy_df = add_label_noise_fast(df, noise_fraction=0.10)
    classifier = BalancedPasswordClassifier()
    
    print("\nБыстрое извлечение признаков...")
    passwords_list = noisy_df['password'].tolist()
    X = classifier.extract_features_batch(passwords_list)
    y = noisy_df['strength'].values
    
    print(f"Признаки извлечены: {X.shape}")
    
    print("\nБыстрое обучение...")
    accuracy = classifier.train_fast(X, y)
    
    test_password_examples(classifier)
    
    with open('smart_password_classifier.pkl', 'wb') as f:
        pickle.dump(classifier, f)
    
    print(f"\n✅ Модель сохранена как smart_password_classifier.pkl")
    
    # Тестирование скорости
    print("\n" + "="*60)
    print("ТЕСТ СКОРОСТИ ПРЕДСКАЗАНИЯ")
    print("="*60)
    
    test_passwords = [
        'password', 'Password123', 'P@ssw0rd!', '123456', 
        'Aa1!', 'aaaaaaaa', 'MySecurePass123!', 'qwerty',
        'sdfkjKJDJKSD123', 'sadasd8305!df@sd', 'A1b2C3d4!@#'
    ] * 500  # 5500 паролей для теста скорости
    
    print(f"Тестируем на {len(test_passwords)} паролях...")
    
    import time
    start_time = time.time()
    predictions = classifier.predict_batch(test_passwords)
    
    end_time = time.time()
    total_time = end_time - start_time
    
    print(f"Время предсказания: {total_time:.3f} секунд")
    print(f"Скорость: {len(test_passwords)/total_time:.0f} паролей/сек")
    print(f"\nРаспределение предсказаний:")
    unique, counts = np.unique(predictions, return_counts=True)
    for pred, count in zip(unique, counts):
        label = ['weak', 'medium', 'strong'][pred]
        percentage = (count / len(predictions)) * 100
        print(f"  {label}: {count} ({percentage:.1f}%)")
    
    print(f"\n✅ УМНАЯ МОДЕЛЬ КЛАССИФИКАЦИИ ГОТОВА!")
    print(f"🎯 Скорость: {len(test_passwords)/total_time:.0f} паролей/сек")


if __name__ == "__main__":
    main_fast()