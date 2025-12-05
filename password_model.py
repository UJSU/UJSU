import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, confusion_matrix
from sklearn.preprocessing import StandardScaler
import pickle
import csv

class BalancedPasswordClassifier:
    def __init__(self):
        # Упрощенная модель для скорости
        self.model = RandomForestClassifier(
            n_estimators=50,  # Меньше деревьев
            max_depth=10,
            min_samples_split=20,
            min_samples_leaf=10,
            max_features=0.7,
            random_state=42,
            n_jobs=-1  # Используем все ядра
        )
        self.scaler = StandardScaler()
        self.is_fitted = False
    
    def extract_fast_features(self, password):
        """Сверхбыстрая экстракция признаков"""
        if not isinstance(password, str):
            password = str(password)
        
        length = len(password)
        features = np.zeros(12, dtype=np.float32)  # Фиксированный размер
        
        # 1. Категория длины (быстро)
        if length <= 6:
            features[0] = 0
        elif length <= 10:
            features[0] = 1
        else:
            features[0] = 2
        
        # 2. Быстрый подсчет символов
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
        
        # 3. Быстрые пропорции
        total_chars = max(length, 1)
        features[5] = digit_count / total_chars
        features[6] = upper_count / total_chars
        features[7] = lower_count / total_chars
        features[8] = special_count / total_chars
        
        # 4. Ключевые флаги (самое важное для скорости)
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
            X, y, test_size=0.2, random_state=42, stratify=y  # Меньше тестовых данных
        )
        
        print(f"Обучаем на {len(X_train)} примерах...")
        
        # Масштабируем признаки
        X_train_scaled = self.scaler.fit_transform(X_train)
        X_test_scaled = self.scaler.transform(X_test)
        
        # Быстрое обучение
        self.model.fit(X_train_scaled, y_train)
        self.is_fitted = True
        
        # Быстрая валидация
        test_predictions = self.model.predict(X_test_scaled)
        test_accuracy = accuracy_score(y_test, test_predictions)
        
        print(f"Точность на тестовой выборке: {test_accuracy:.4f}")
        
        return test_accuracy
    
    def predict_batch(self, passwords):
        """Сверхбыстрое предсказание для батча паролей"""
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_features_batch(passwords)
        features_scaled = self.scaler.transform(features)
        return self.model.predict(features_scaled)
    
    def predict_single(self, password):
        """Быстрое предсказание для одного пароля"""
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_fast_features(password)
        features_scaled = self.scaler.transform([features])
        return self.model.predict(features_scaled)[0]
    
    def predict_proba_batch(self, passwords):
        """Быстрое предсказание вероятностей для батча"""
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        
        features = self.extract_features_batch(passwords)
        features_scaled = self.scaler.transform(features)
        return self.model.predict_proba(features_scaled)

def load_data_fast(filename):
    """Сверхбыстрая загрузка данных"""
    print(f"Быстрая загрузка {filename}...")
    
    # Используем pandas для максимальной скорости
    df = pd.read_csv(filename, encoding='utf-8', header=None, 
                    names=['password', 'strength'], quoting=csv.QUOTE_MINIMAL)
    
    # Быстрое преобразование типов
    df['strength'] = df['strength'].astype(np.int8)
    
    print(f"Загружено {len(df)} строк")
    return df

def add_label_noise_fast(df, noise_fraction=0.10):
    """Быстрое добавление шума"""
    print(f"Добавляем {noise_fraction:.0%} шума...")
    
    np.random.seed(42)
    noisy_df = df.copy()
    
    n_noise = int(len(df) * noise_fraction)
    noise_indices = np.random.choice(len(df), n_noise, replace=False)
    
    # Векторизованная операция
    current_labels = noisy_df.iloc[noise_indices]['strength'].values
    new_labels = np.random.randint(0, 3, size=len(current_labels))
    
    # Избегаем совпадений с исходными метками
    mask = new_labels == current_labels
    new_labels[mask] = (new_labels[mask] + 1) % 3
    
    noisy_df.iloc[noise_indices, noisy_df.columns.get_loc('strength')] = new_labels
    
    return noisy_df

def main_fast():
    print("=== СВЕРХБЫСТРАЯ МОДЕЛЬ КЛАССИФИКАЦИИ ПАРОЛЕЙ ===")
    
    # Быстрая загрузка
    df = load_data_fast('data.csv')
    
    print("\nРаспределение меток:")
    for strength, count in df['strength'].value_counts().items():
        print(f"  {strength}: {count}")
    
    # Добавляем шум
    noisy_df = add_label_noise_fast(df, noise_fraction=0.10)
    
    # Инициализация быстрого классификатора
    classifier = BalancedPasswordClassifier()
    
    # Быстрое извлечение признаков
    print("\nБыстрое извлечение признаков...")
    passwords_list = noisy_df['password'].tolist()
    X = classifier.extract_features_batch(passwords_list)
    y = noisy_df['strength'].values
    
    print(f"Признаки извлечены: {X.shape}")
    
    # Быстрое обучение
    print("\nБыстрое обучение...")
    accuracy = classifier.train_fast(X, y)
    
    # Сохранение модели
    with open('fast_password_classifier.pkl', 'wb') as f:
        pickle.dump(classifier, f)
    
    print(f"\nМодель сохранена как fast_password_classifier.pkl")
    
    # Тестирование скорости
    print("\n" + "="*50)
    print("ТЕСТ СКОРОСТИ ПРЕДСКАЗАНИЯ")
    print("="*50)
    
    test_passwords = [
        'password', 'Password123', 'P@ssw0rd!', '123456', 
        'Aa1!', 'aaaaaaaa', 'MySecurePass123!', 'qwerty'
    ] * 1000  # 8000 паролей для теста скорости
    
    print(f"Тестируем на {len(test_passwords)} паролях...")
    
    import time
    start_time = time.time()
    
    # Пакетное предсказание
    predictions = classifier.predict_batch(test_passwords)
    
    end_time = time.time()
    total_time = end_time - start_time
    
    print(f"Время предсказания: {total_time:.3f} секунд")
    print(f"Скорость: {len(test_passwords)/total_time:.0f} паролей/сек")
    
    # Демонстрация на нескольких примерах
    print(f"\n{'Пароль':<20} {'Предсказание':<12}")
    print("-" * 35)
    
    demo_passwords = ['password', 'Password123', 'P@ssw0rd!', 'Aa1!', '123456']
    for pwd in demo_passwords:
        pred = classifier.predict_single(pwd)
        class_name = ['weak', 'medium', 'strong'][pred]
        print(f"'{pwd:<18}' {class_name:<12}")
    
    print(f"\n✅ СВЕРХБЫСТРАЯ МОДЕЛЬ ГОТОВА!")
    print(f"🎯 Скорость: {len(test_passwords)/total_time:.0f} паролей/сек")

if __name__ == "__main__":
    main_fast()
