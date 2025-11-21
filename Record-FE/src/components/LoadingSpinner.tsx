/**
 * 로딩 스피너 컴포넌트
 * API 호출 중 로딩 상태를 표시
 */

import React from 'react';
import {
  View,
  ActivityIndicator,
  Text,
  StyleSheet,
  Modal,
} from 'react-native';
import { Colors, Typography, Spacing } from '../styles/designSystem';

interface LoadingSpinnerProps {
  /** 로딩 상태 */
  loading: boolean;
  /** 로딩 메시지 */
  message?: string;
  /** 전체 화면 오버레이 여부 */
  overlay?: boolean;
  /** 크기 */
  size?: 'small' | 'large';
  /** 색상 */
  color?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  loading,
  message = '로딩 중...',
  overlay = false,
  size = 'large',
  color = Colors.systemBlue,
}) => {
  if (!loading) return null;

  const content = (
    <View style={[styles.container, overlay && styles.overlayContainer]}>
      <View style={styles.spinnerContainer}>
        <ActivityIndicator size={size} color={color} />
        {message && (
          <Text style={styles.message}>{message}</Text>
        )}
      </View>
    </View>
  );

  if (overlay) {
    return (
      <Modal
        transparent
        visible={loading}
        animationType="fade"
      >
        {content}
      </Modal>
    );
  }

  return content;
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: Spacing.xl,
  },
  overlayContainer: {
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  spinnerContainer: {
    backgroundColor: Colors.systemBackground,
    borderRadius: 12,
    padding: Spacing.xl,
    alignItems: 'center',
    minWidth: 120,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
  },
  message: {
    ...Typography.callout,
    color: Colors.label,
    marginTop: Spacing.md,
    textAlign: 'center',
  },
});

export default LoadingSpinner;
