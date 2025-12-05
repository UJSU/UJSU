# import pandas as pd
# import numpy as np
# from sklearn.ensemble import RandomForestClassifier
# from sklearn.model_selection import train_test_split
# from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
# from sklearn.preprocessing import StandardScaler
# import pickle
# import csv

# class BalancedPasswordClassifier:
#     def __init__(self):
#         self.model = RandomForestClassifier(
#             n_estimators=80,
#             max_depth=12,
#             min_samples_split=30,
#             min_samples_leaf=15,
#             max_features=0.6,
#             random_state=42,
#             n_jobs=-1
#         )
#         self.scaler = StandardScaler()
#         self.is_fitted = False
    
#     def extract_balanced_features(self, password):
#         """Признаки с уменьшенным влиянием длины и усиленным влиянием разнообразия"""
#         if not isinstance(password, str):
#             password = str(password)
        
#         features = []
        
#         # 1. Длина (сильно ограниченное влияние)
#         length = len(password)
#         # Вместо прямой длины используем категории длины
#         if length <= 6:
#             length_category = 0
#         elif length <= 10:
#             length_category = 1
#         else:
#             length_category = 2
#         features.append(length_category)
        
#         # 2. Состав символов (усиленное влияние)
#         digit_count = sum(1 for c in password if c.isdigit())
#         upper_count = sum(1 for c in password if c.isupper())
#         lower_count = sum(1 for c in password if c.islower())
#         special_count = sum(1 for c in password if not c.isalnum())
        
#         # Абсолютные счетчики
#         features.extend([digit_count, upper_count, lower_count, special_count])
        
#         # 3. Пропорции (более важные чем длина)
#         total_chars = max(length, 1)
#         digit_ratio = digit_count / total_chars
#         upper_ratio = upper_count / total_chars
#         lower_ratio = lower_count / total_chars
#         special_ratio = special_count / total_chars
        
#         features.extend([digit_ratio, upper_ratio, lower_ratio, special_ratio])
        
#         # 4. Ключевые комбинации символов (самые важные!)
#         has_upper_lower = int(upper_count > 0 and lower_count > 0)
#         has_letter_digit = int((upper_count > 0 or lower_count > 0) and digit_count > 0)
#         has_special = int(special_count > 0)
#         has_all_three = int(has_upper_lower and has_letter_digit and has_special)
        
#         features.extend([has_upper_lower, has_letter_digit, has_special, has_all_three])
        
#         # 5. Меры разнообразия (усиленные)
#         unique_chars = len(set(password))
#         unique_ratio = unique_chars / total_chars if total_chars > 0 else 0
        
#         # Энтропия Шеннона (мера разнообразия)
#         char_counts = {}
#         for char in password:
#             char_counts[char] = char_counts.get(char, 0) + 1
        
#         entropy = 0
#         for count in char_counts.values():
#             p = count / total_chars
#             if p > 0:
#                 entropy -= p * np.log2(p)
        
#         features.extend([unique_ratio, entropy])
        
#         # 6. Качество пароля (комбинированные метрики)
#         # Счетчик сложности: сумма разных типов символов
#         complexity_score = min(has_upper_lower + has_letter_digit + has_special + int(length >= 8), 4)
#         features.append(complexity_score)
        
#         # Сбалансированность символов
#         balance_score = 1 - (max(digit_ratio, upper_ratio, lower_ratio, special_ratio) - 
#                            min(digit_ratio, upper_ratio, lower_ratio, special_ratio))
#         features.append(balance_score)
        
#         return features
    
#     def train(self, X, y):
#         """Обучение модели"""
#         X_train, X_test, y_train, y_test = train_test_split(
#             X, y, test_size=0.3, random_state=42, stratify=y
#         )
        
#         print(f"Размер обучающей выборки: {len(X_train)}")
#         print(f"Размер тестовой выборки: {len(X_test)}")
        
#         # Масштабируем признаки
#         X_train_scaled = self.scaler.fit_transform(X_train)
#         X_test_scaled = self.scaler.transform(X_test)
        
#         # Обучаем модель
#         self.model.fit(X_train_scaled, y_train)
#         self.is_fitted = True
        
#         # Проверяем на разных выборках
#         train_predictions = self.model.predict(X_train_scaled)
#         train_accuracy = accuracy_score(y_train, train_predictions)
        
#         test_predictions = self.model.predict(X_test_scaled)
#         test_accuracy = accuracy_score(y_test, test_predictions)
        
#         print(f"Точность на обучающей выборке: {train_accuracy:.4f}")
#         print(f"Точность на тестовой выборке: {test_accuracy:.4f}")
        
#         # Анализ
#         cm = confusion_matrix(y_test, test_predictions)
#         print("\nМатрица ошибок:")
#         print(cm)
        
#         total_errors = np.sum(cm) - np.trace(cm)
#         print(f"Ошибок: {total_errors}/{len(y_test)} ({total_errors/len(y_test):.2%})")
        
#         # Анализ важности признаков
#         print("\nТоп-10 важных признаков:")
#         feature_names = [
#             'length_category', 'digit_count', 'upper_count', 'lower_count', 'special_count',
#             'digit_ratio', 'upper_ratio', 'lower_ratio', 'special_ratio',
#             'has_upper_lower', 'has_letter_digit', 'has_special', 'has_all_three',
#             'unique_ratio', 'entropy', 'complexity_score', 'balance_score'
#         ]
        
#         importances = self.model.feature_importances_
#         feature_importance = list(zip(feature_names, importances))
#         feature_importance.sort(key=lambda x: x[1], reverse=True)
        
#         for name, importance in feature_importance[:10]:
#             print(f"  {name}: {importance:.4f}")
        
#         return test_accuracy
    
#     def predict(self, password):
#         if not self.is_fitted:
#             raise ValueError("Модель не обучена!")
        
#         features = self.extract_balanced_features(password)
#         features_scaled = self.scaler.transform([features])
#         return self.model.predict(features_scaled)[0]
    
#     def predict_proba(self, password):
#         if not self.is_fitted:
#             raise ValueError("Модель не обучена!")
        
#         features = self.extract_balanced_features(password)
#         features_scaled = self.scaler.transform([features])
#         return self.model.predict_proba(features_scaled)[0]

# def fast_add_label_noise(df, noise_fraction=0.10):
#     """Быстрое добавление шума в метки"""
#     print(f"Добавляем {noise_fraction:.0%} шума в метки...")
    
#     np.random.seed(42)
#     noisy_df = df.copy()
    
#     n_noise = int(len(df) * noise_fraction)
#     noise_indices = np.random.choice(len(df), n_noise, replace=False)
    
#     current_labels = noisy_df.iloc[noise_indices]['strength'].values
#     new_labels = np.array([np.random.choice([x for x in [0, 1, 2] if x != label]) 
#                           for label in current_labels])
    
#     noisy_df.iloc[noise_indices, noisy_df.columns.get_loc('strength')] = new_labels
    
#     print(f"Изменено {len(noise_indices)} меток")
#     return noisy_df

# def load_csv_with_commas(filename):
#     """Загрузка CSV с обработкой запятых"""
#     data = []
    
#     with open(filename, 'r', encoding='utf-8') as file:
#         reader = csv.reader(file)
#         headers = next(reader, None)
        
#         for i, row in enumerate(reader):
#             if len(row) >= 2:
#                 password = ','.join(row[:-1])
#                 strength = row[-1]
#                 data.append({'password': password, 'strength': strength})
            
#             if i % 100000 == 0 and i > 0:
#                 print(f"Обработано {i} строк...")
    
#     return pd.DataFrame(data)

# def main():
#     print("=== БАЛАНСИРОВАННАЯ МОДЕЛЬ ===")
#     print("=== (Меньше зависимость от длины, больше от разнообразия) ===\n")
    
#     # Загрузка данных
#     print("Загружаем data.csv...")
#     try:
#         df = load_csv_with_commas('data.csv')
#         print(f"Успешно загружено {len(df)} строк")
#     except Exception as e:
#         print(f"Ошибка загрузки: {e}")
#         return
    
#     # Преобразуем метки
#     if isinstance(df['strength'].iloc[0], str):
#         df['strength'] = df['strength'].astype(int)
    
#     print("\nИсходное распределение меток:")
#     for strength, count in df['strength'].value_counts().items():
#         print(f"  {strength}: {count}")
    
#     # Добавляем шум
#     noisy_df = fast_add_label_noise(df, noise_fraction=0.10)
    
#     # Инициализация классификатора
#     classifier = BalancedPasswordClassifier()
    
#     # Извлечение признаков
#     print("\nИзвлекаем сбалансированные признаки...")
#     X = []
#     y = noisy_df['strength'].values
    
#     total_passwords = len(noisy_df)
#     for i, password in enumerate(noisy_df['password']):
#         features = classifier.extract_balanced_features(password)
#         X.append(features)
        
#         if i % 100000 == 0 and i > 0:
#             print(f"Обработано {i}/{total_passwords} паролей...")
    
#     X = np.array(X)
#     print(f"Готово! Размерность признаков: {X.shape}")
    
#     # Обучение модели
#     print("\nОбучаем сбалансированную модель...")
#     accuracy = classifier.train(X, y)
    
#     # Сохранение модели
#     with open('balanced_password_classifier.pkl', 'wb') as f:
#         pickle.dump(classifier, f)
    
#     print(f"\nМодель сохранена как balanced_password_classifier.pkl")
    
#     # Тестирование на критических случаях
#     print("\n" + "="*60)
#     print("ТЕСТИРОВАНИЕ ВЛИЯНИЯ ДЛИНЫ И РАЗНООБРАЗИЯ")
#     print("="*60)
    
#     test_cases = [
#         # Короткие но сложные
#         ('Aa1!', 'Короткий сложный'),
#         ('P@1s', 'Очень короткий сложный'),
#         ('A1b2!', 'Короткий с разными символами'),
        
#         # Длинные но простые
#         ('aaaaaaaaaaaaaaaa', 'Длинный одинаковые буквы'),
#         ('1234567890123456', 'Длинный одинаковые цифры'),
#         ('AAAAAAAAAAAAAAAA', 'Длинный одинаковые заглавные'),
        
#         # Сбалансированные
#         ('Password123!', 'Средний сложный'),
#         ('pass123', 'Короткий средний'),
#         ('MyPass123!', 'Средний очень сложный'),
        
#         # Граничные
#         ('Aa1!Bb2@', 'Короткий очень сложный'),
#         ('simplelongpassword123', 'Длинный простой'),
#         ('C0mpl3x!', 'Короткий сложный'),
#     ]
    
#     print(f"\n{'Пароль':<25} {'Описание':<30} {'Предсказание':<8} {'Уверенность':<10} {'Длина':<6} {'Разнообр.'}")
#     print("-" * 95)
    
#     for password, description in test_cases:
#         prediction = classifier.predict(password)
#         probs = classifier.predict_proba(password)
#         confidence = max(probs)
#         class_name = ['weak', 'medium', 'strong'][prediction]
        
#         # Анализ характеристик
#         length = len(password)
#         unique_ratio = len(set(password)) / len(password) if password else 0
#         diversity = f"{unique_ratio:.2f}"
        
#         print(f"'{password:<23}' {description:<30} {class_name:<8} {confidence:.3f}     {length:<6} {diversity:<8}")
    
#     # Анализ влияния признаков
#     print("\n" + "="*60)
#     print("АНАЛИЗ ПОВЕДЕНИЯ МОДЕЛИ:")
#     print("="*60)
    
#     print("Короткие сложные пароли:")
#     short_complex = ['Aa1!', 'P@1s', 'A1b2!', 'C0mpl3x!']
#     for pwd in short_complex:
#         pred = classifier.predict(pwd)
#         probs = classifier.predict_proba(pwd)
#         print(f"  '{pwd}' -> {['weak','medium','strong'][pred]} (0={probs[0]:.3f}, 1={probs[1]:.3f}, 2={probs[2]:.3f})")
    
#     print("\nДлинные простые пароли:")
#     long_simple = ['aaaaaaaaaaaaaaaa', '1234567890123456', 'simplelongpassword123']
#     for pwd in long_simple:
#         pred = classifier.predict(pwd)
#         probs = classifier.predict_proba(pwd)
#         print(f"  '{pwd}' -> {['weak','medium','strong'][pred]} (0={probs[0]:.3f}, 1={probs[1]:.3f}, 2={probs[2]:.3f})")
    
#     print(f"\n✅ БАЛАНСИРОВАННАЯ МОДЕЛЬ ГОТОВА!")
#     print("📊 Меньше зависимость от длины, больше от разнообразия символов")

# if __name__ == "__main__":
#     main()