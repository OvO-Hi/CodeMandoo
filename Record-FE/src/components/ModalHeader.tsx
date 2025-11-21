import React, { ReactNode } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import { Colors, Typography, Spacing } from '../styles/designSystem';

export interface ModalHeaderProps {
  // 중앙 타이틀
  title: string;
  
  // 뒤로가기 버튼
  onBack: () => void;
  backButtonText?: string;
  
  // 오른쪽 액션 (옵션)
  rightAction?: {
    text: string;
    onPress: () => void;
    disabled?: boolean;
  };
  
  // 커스텀 오른쪽 컨텐츠 (rightAction 대신 사용)
  rightContent?: ReactNode;
  
  // 스타일 커스터마이징
  containerStyle?: any;
}

const ModalHeader: React.FC<ModalHeaderProps> = ({
  title,
  onBack,
  backButtonText = '←',
  rightAction,
  rightContent,
  containerStyle,
}) => {
  return (
    <View style={[styles.header, containerStyle]}>
      {/* 뒤로가기 버튼 */}
      <TouchableOpacity
        style={styles.backButton}
        onPress={onBack}
      >
        <Text style={styles.backButtonText}>{backButtonText}</Text>
      </TouchableOpacity>

      {/* 중앙 타이틀 */}
      <Text style={styles.headerTitle}>{title}</Text>

      {/* 오른쪽 액션 또는 플레이스홀더 */}
      {rightContent ? (
        <View style={styles.rightContent}>{rightContent}</View>
      ) : rightAction ? (
        <TouchableOpacity
          onPress={rightAction.onPress}
          disabled={rightAction.disabled}
        >
          <Text
            style={[
              styles.rightActionText,
              rightAction.disabled && styles.rightActionTextDisabled,
            ]}
          >
            {rightAction.text}
          </Text>
        </TouchableOpacity>
      ) : (
        <View style={styles.placeholder} />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: Spacing.screenPadding,
    paddingVertical: Spacing.lg,
    backgroundColor: Colors.systemBackground,
    borderBottomWidth: 0.5,
    borderBottomColor: Colors.systemGray5,
  },
  backButton: {
    width: 44,
    height: 44,
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
  backButtonText: {
    fontSize: 28,
    color: Colors.label,
    fontWeight: '300',
  },
  headerTitle: {
    ...Typography.headline,
    fontWeight: '600',
    color: Colors.label,
    flex: 1,
    textAlign: 'center',
  },
  rightContent: {
    width: 44,
    alignItems: 'flex-end',
  },
  rightActionText: {
    ...Typography.body,
    color: Colors.primary,
    fontWeight: '600',
  },
  rightActionTextDisabled: {
    color: Colors.tertiaryLabel,
  },
  placeholder: {
    width: 44,
  },
});

export default ModalHeader;
