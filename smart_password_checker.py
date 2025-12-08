import sys
import pickle
import numpy as np
import re

class BalancedPasswordClassifier:
    def __init__(self):
        self.model = None
        self.scaler = None
        self.is_fitted = False
        self.weak_patterns = [
            'password', 'qwerty', 'admin', 'welcome', 'letmein',
            'monkey', 'dragon', 'baseball', 'football', 'mustang',
            'sunshine', 'princess', 'superman', 'batman', 'master',
            'hello', 'iloveyou', 'trustno1', 'shadow', 'ashley',
            'michael', 'jordan', 'charlie', 'donald', 'harley',
            'fuckyou', 'whatever', 'zaq1zaq1'
        ]
        self.common_weak_passwords = [
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
    
    def count_categories(self, password):
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
        leet_to_normal = {
            '@': 'a', '$': 's', '1': 'i', '0': 'o', '3': 'e',
            '!': 'i', '7': 't', '8': 'b', '9': 'g', '4': 'a',
            '5': 's', '2': 'z', '6': 'b'
        }
        normalized = ''.join(leet_to_normal.get(c, c) for c in password.lower())
        return word in normalized
    
    def follows_weak_pattern(self, password):
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
                if word_len > pwd_len * 0.7:
                    return True
        return False
    
    def rule_based_classification(self, password):
        password_lower = password.lower()
        if len(password) < 6:
            return 0
        if self.is_in_blacklist(password):
            return 0
        if self.follows_weak_pattern(password):
            return 0
        if password.isdigit() and len(password) < 10:
            return 0
        if password.isalpha() and password_lower == password and len(password) < 8:
            return 0
        return None
    
    def predict_single(self, password):
        rule_based = self.rule_based_classification(password)
        if rule_based is not None:
            return rule_based
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        features = self.extract_fast_features(password)
        features_scaled = self.scaler.transform([features])
        ml_prediction = self.model.predict(features_scaled)[0]
        if ml_prediction == 2:
            categories = self.count_categories(password)
            if len(password) < 12 and categories < 4:
                return 1
        return ml_prediction
    
    def predict_proba(self, password):
        rule_based = self.rule_based_classification(password)
        if rule_based is not None:
            if rule_based == 0:
                return np.array([0.90, 0.08, 0.02])
            elif rule_based == 1:
                return np.array([0.05, 0.85, 0.10])
            else:
                return np.array([0.33, 0.34, 0.33])
        if not self.is_fitted:
            raise ValueError("Модель не обучена!")
        features = self.extract_fast_features(password)
        features_scaled = self.scaler.transform([features])
        return self.model.predict_proba(features_scaled)[0]

def main():
    model_files = [
        'smart_password_classifier.pkl',
        'balanced_password_classifier.pkl',
        'strict_password_classifier.pkl'
    ]
    
    classifier = None
    loaded_model = None
    
    for model_file in model_files:
        try:
            with open(model_file, 'rb') as f:
                classifier = pickle.load(f)
            loaded_model = model_file
            break
        except FileNotFoundError:
            continue
        except Exception:
            continue
    
    if classifier is None:
        sys.exit(1)
    
    required_methods = ['predict_single', 'predict_proba']
    for method in required_methods:
        if not hasattr(classifier, method):
            sys.exit(1)
    
    if not hasattr(classifier, 'is_fitted') or not classifier.is_fitted:
        sys.exit(1)
    
    try:
        while True:
            line = sys.stdin.readline().strip()
            if not line:
                break
            password = line
            try:
                strength_num = classifier.predict_single(password)
                probabilities = classifier.predict_proba(password)
                strength_num = int(strength_num)
                if strength_num == 0:
                    strength = "weak"
                elif strength_num == 1:
                    strength = "medium"
                elif strength_num == 2:
                    strength = "strong"
                else:
                    strength = "medium"
                weak_prob = float(probabilities[0])
                medium_prob = float(probabilities[1])
                strong_prob = float(probabilities[2])
                confidence = max(weak_prob, medium_prob, strong_prob)
                if strength == "strong":
                    score = int(strong_prob * 100)
                elif strength == "medium":
                    score = int(medium_prob * 80)
                else:
                    score = int(weak_prob * 30)
                score = max(0, min(100, score))
                result = f"{strength},{confidence:.4f},{weak_prob:.4f},{medium_prob:.4f},{strong_prob:.4f},{score}"
                print(result)
                sys.stdout.flush()
            except Exception:
                print("medium,0.5000,0.3333,0.3333,0.3334,40")
    except KeyboardInterrupt:
        pass
    except Exception:
        sys.exit(1)

def test_classifier():
    test_passwords = [
        'password',
        'Password123',
        'P@ssw0rd!',
        'Admin2024!',
        '123456',
        'Aa1!',
        'sdfkjKJDJKSD123',
        'sadasd8305!df@sd',
        'A1b2C3d4!@#',
        'MySecurePass123!',
        'qwertyuiop',
        'J7#fK9$pL2&mN8@qR5',
        'CorrectHorseBatteryStaple',
    ]
    model_files = ['smart_password_classifier.pkl', 'balanced_password_classifier.pkl', 'strict_password_classifier.pkl']
    classifier = None
    for model_file in model_files:
        try:
            with open(model_file, 'rb') as f:
                classifier = pickle.load(f)
            break
        except:
            continue
    if classifier is None:
        return
    for pwd in test_passwords:
        try:
            strength_num = classifier.predict_single(pwd)
            strength = ['weak', 'medium', 'strong'][strength_num]
            probs = classifier.predict_proba(pwd)
            weak_prob = probs[0]
            medium_prob = probs[1]
            strong_prob = probs[2]
            if strength == "strong":
                score = int(strong_prob * 100)
            elif strength == "medium":
                score = int(medium_prob * 80)
            else:
                score = int(weak_prob * 30)
        except Exception:
            pass

def print_usage():
    pass

if __name__ == "__main__":
    if len(sys.argv) > 1:
        if sys.argv[1] == "--test":
            test_classifier()
        elif sys.argv[1] == "--help" or sys.argv[1] == "-h":
            print_usage()
        else:
            sys.exit(1)
    else:
        main()