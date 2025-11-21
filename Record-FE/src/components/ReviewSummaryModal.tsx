import React, { useRef, useEffect, useState } from 'react';
import {
  View,
  Text,
  Modal,
  TouchableOpacity,
  StyleSheet,
  Animated,
  Dimensions,
  TouchableWithoutFeedback,
  TextInput,
  ScrollView,
  Alert,
} from 'react-native';
import Clipboard from '@react-native-clipboard/clipboard';
import {
  Colors,
  Typography,
  Spacing,
  BorderRadius,
  Shadows,
} from '../styles/designSystem';

const { height } = Dimensions.get('window');

interface ReviewSummaryModalProps {
  visible: boolean;
  onClose: () => void;
  summaryText: string;
}

const ReviewSummaryModal: React.FC<ReviewSummaryModalProps> = ({
  visible,
  onClose,
  summaryText,
}) => {
  const slideAnim = useRef(new Animated.Value(height)).current;
  const [editedText, setEditedText] = useState(summaryText);

  useEffect(() => {
    setEditedText(summaryText);
  }, [summaryText]);

  useEffect(() => {
    if (visible) {
      Animated.spring(slideAnim, {
        toValue: 0,
        useNativeDriver: true,
        tension: 65,
        friction: 11,
      }).start();
    } else {
      Animated.timing(slideAnim, {
        toValue: height,
        duration: 250,
        useNativeDriver: true,
      }).start();
    }
  }, [visible]);

  /**
   * 클립보드에 요약된 텍스트를 복사합니다.
   * 
   * 이유: 사용자가 요약된 후기를 다른 곳에 붙여넣을 수 있도록
   *      클립보드에 복사하는 기능을 제공합니다.
   *      복사 성공 시 사용자에게 알림을 표시합니다.
   */
  const handleCopy = async () => {
    try {
      if (!editedText || editedText.trim().length === 0) {
        Alert.alert('알림', '복사할 내용이 없습니다.');
        return;
      }

      // 클립보드에 텍스트 복사
      await Clipboard.setString(editedText.trim());
      Alert.alert('완료', '요약된 후기가 클립보드에 복사되었습니다.');
    } catch (error) {
      console.error('클립보드 복사 오류:', error);
      Alert.alert('오류', '클립보드 복사에 실패했습니다.');
    }
  };

  return (
    <Modal
      visible={visible}
      transparent
      animationType="none"
      onRequestClose={onClose}
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay}>
          <TouchableWithoutFeedback>
            <Animated.View
              style={[
                styles.modalContainer,
                {
                  transform: [{ translateY: slideAnim }],
                },
              ]}
            >
              {/* Close Button */}
              <TouchableOpacity style={styles.closeButton} onPress={onClose}>
                <Text style={styles.closeButtonText}>✕</Text>
              </TouchableOpacity>

              {/* Title */}
              <Text style={styles.title}>요약완료!</Text>

              {/* Summary Content */}
              <ScrollView style={styles.contentContainer} showsVerticalScrollIndicator={false}>
                <TextInput
                  style={styles.summaryInput}
                  value={editedText}
                  onChangeText={setEditedText}
                  multiline
                  placeholder="요약된 내용을 수정할 수 있습니다..."
                  placeholderTextColor="#999"
                />
              </ScrollView>

              {/* Copy Button */}
              <TouchableOpacity style={styles.copyButton} onPress={handleCopy}>
                <Text style={styles.copyIcon}>📋</Text>
                <Text style={styles.copyButtonText}>요약된 후기를 복사해서 사용하세요</Text>
              </TouchableOpacity>
            </Animated.View>
          </TouchableWithoutFeedback>
        </View>
      </TouchableWithoutFeedback>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'flex-end',
  },
  modalContainer: {
    backgroundColor: '#fff',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingTop: 20,
    paddingHorizontal: 24,
    paddingBottom: 40,
    minHeight: height * 0.4,
    maxHeight: height * 0.8,
    ...Shadows.large,
  },
  closeButton: {
    position: 'absolute',
    top: 16,
    right: 16,
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 10,
  },
  closeButtonText: {
    fontSize: 20,
    color: '#999',
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#000',
    marginTop: 8,
    marginBottom: 20,
  },
  contentContainer: {
    flex: 1,
    marginBottom: 16,
  },
  summaryInput: {
    fontSize: 16,
    lineHeight: 24,
    color: '#333',
    backgroundColor: '#F8F9FA',
    borderRadius: 12,
    padding: 16,
    minHeight: 150,
    textAlignVertical: 'top',
  },
  copyButton: {
    backgroundColor: '#F5F5F5',
    borderRadius: 12,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 16,
  },
  copyIcon: {
    fontSize: 24,
    marginRight: 12,
  },
  copyButtonText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
});

export default ReviewSummaryModal;
