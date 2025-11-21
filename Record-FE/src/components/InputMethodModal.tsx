import React, { useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Modal,
  Alert,
  Animated,
  Dimensions,
  Pressable,
} from 'react-native';
import {
  Colors,
  Typography,
  Shadows,
} from '../styles/designSystem';

interface InputMethodModalProps {
  visible: boolean;
  onClose: () => void; // 페이지 뒤로 가기 등
  onSelectManual: () => void; // 직접 입력 모드
  onSelectOCR: () => void; // OCR 모드
}

const { height } = Dimensions.get('window');

const InputMethodModal: React.FC<InputMethodModalProps> = ({
  visible,
  onClose,
  onSelectManual,
  onSelectOCR,
}) => {
  const slideAnim = useRef(new Animated.Value(height)).current;

  useEffect(() => {
    if (visible) {
      // 모달 열기 애니메이션
      Animated.spring(slideAnim, {
        toValue: 0,
        damping: 25,
        stiffness: 120,
        useNativeDriver: true,
      }).start();
    } else {
      // 모달 닫기 애니메이션
      Animated.timing(slideAnim, {
        toValue: height,
        duration: 250,
        useNativeDriver: true,
      }).start();
    }
  }, [visible]);

  const handleInputMethodSelect = (method: 'ocr' | 'booking' | 'manual') => {
    if (method === 'manual') {
      onSelectManual(); // 직접 입력 모드로 전환
    } else if (method === 'ocr') {
      onSelectOCR(); // OCR 페이지로 이동
    } else if (method === 'booking') {
      Alert.alert('준비 중', '예매내역 불러오기 기능은 준비 중입니다.');
    }
  };

  // 딤드 클릭 시 모달만 닫고 직접 입력 모드로
  const handleOverlayPress = () => {
    handleInputMethodSelect('manual');
  };

  return (
    <Modal
      transparent
      visible={visible}
      animationType="fade"
      onRequestClose={onClose} // 뒤로가기 버튼
    >
      <View style={styles.container}>
        {/* 딤드 레이어 */}
        <Pressable style={styles.overlay} onPress={handleOverlayPress} />

        {/* 모달 콘텐츠 */}
        <Animated.View
          style={[
            styles.modalContent,
            { transform: [{ translateY: slideAnim }] },
          ]}
        >
          <Text style={styles.modalTitle}>입력 방법을 선택하세요</Text>

          <View style={styles.buttonContainer}>
            <TouchableOpacity
              style={styles.methodButton}
              onPress={() => handleInputMethodSelect('booking')}
            >
              <Text style={styles.methodButtonText}>
                예매내역{'\n'}불러오기
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.methodButton}
              onPress={() => handleInputMethodSelect('ocr')}
            >
              <Text style={styles.methodButtonText}>OCR</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.methodButton}
              onPress={() => handleInputMethodSelect('manual')}
            >
              <Text style={styles.methodButtonText}>직접{'\n'}입력</Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity style={styles.cancelButton} onPress={onClose}>
            <Text style={styles.cancelButtonText}>취소</Text>
          </TouchableOpacity>
        </Animated.View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1 },
  overlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.5)',
  },
  modalContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: Colors.systemBackground,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    padding: 24,
    paddingBottom: 40,
  },
  modalTitle: {
    ...Typography.title3,
    fontWeight: '600',
    color: Colors.label,
    textAlign: 'center',
    marginBottom: 24,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    marginBottom: 16,
  },
  methodButton: {
    flex: 1,
    backgroundColor: Colors.systemBackground,
    borderWidth: 1,
    borderColor: Colors.systemGray4,
    borderRadius: 16,
    padding: 24,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 120,
    ...Shadows.medium,
  },
  methodButtonText: {
    ...Typography.body,
    fontWeight: '600',
    color: Colors.label,
    textAlign: 'center',
    lineHeight: 22,
  },
  cancelButton: {
    backgroundColor: Colors.secondarySystemBackground,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
  },
  cancelButtonText: {
    ...Typography.body,
    color: Colors.secondaryLabel,
    fontWeight: '600',
  },
});

export default InputMethodModal;
