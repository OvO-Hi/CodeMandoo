import React, { ReactNode } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Image,
  Animated,
} from 'react-native';
import { Colors, Typography, Spacing, BorderRadius, Shadows } from '../styles/designSystem';

export interface GNBProps {
  // 중앙 타이틀 (애니메이션 가능)
  centerTitle?: string;
  centerTitleOpacity?: Animated.AnimatedInterpolation<number>;
  centerTitleStyle?: any;
  
  // 오른쪽 컨텐츠 (커스텀 렌더링)
  rightContent?: ReactNode;
  
  // 헤더 스타일 커스터마이징
  headerStyle?: any;
  containerStyle?: any;
  
  // 상단 여백 (SafeArea insets)
  topInset?: number;
}

const GNB: React.FC<GNBProps> = ({
  centerTitle,
  centerTitleOpacity,
  centerTitleStyle,
  rightContent,
  headerStyle,
  containerStyle,
  topInset = 0,
}) => {
  return (
    <Animated.View
      style={[
        styles.header,
        { paddingTop: topInset },
        containerStyle,
        headerStyle,
      ]}
    >
      {/* 왼쪽 로고 */}
      <Image
        source={require('../assets/logo.png')}
        style={styles.headerLogo}
      />

      {/* 중앙 타이틀 (옵션) */}
      {centerTitle && (
        <Animated.View
          style={[
            styles.centerTitleContainer,
            { top: topInset + 10 },
            centerTitleOpacity && { opacity: centerTitleOpacity },
            centerTitleStyle,
          ]}
        >
          <Text style={styles.centerTitle}>{centerTitle}</Text>
        </Animated.View>
      )}

      {/* 오른쪽 컨텐츠 (옵션) */}
      {rightContent && (
        <View style={styles.rightContent}>
          {rightContent}
        </View>
      )}
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: Spacing.screenPadding,
    paddingVertical: Spacing.xl,
    height: 60,
    backgroundColor: Colors.systemBackground,
    zIndex: 10,
  },
  headerLogo: {
    width: 80,
    height: 22,
    transform: [{ translateY: 10 }],
    resizeMode: 'contain',
  },
  
  // 마이페이지 아이디 나오는 것
  centerTitleContainer: {
    position: 'absolute',
    left: 0,
    right: 0,
    alignItems: 'center',
    justifyContent: 'center',
    height: 36,
  },
  centerTitle: {
    ...Typography.callout,
    fontWeight: 'bold',
    color: Colors.label,
  },

  // 우측 요소들
  rightContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
});

export default GNB;
