/**
 * 에러 바운더리 컴포넌트
 * 앱 전체의 에러를 캐치하고 사용자 친화적인 에러 화면을 표시
 */

import React, { Component, ReactNode } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';
import { Colors, Typography, Spacing, BorderRadius } from '../styles/designSystem';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: any) => void;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: any;
}

class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): State {
    return {
      hasError: true,
      error,
      errorInfo: null,
    };
  }

  componentDidCatch(error: Error, errorInfo: any) {
    this.setState({
      error,
      errorInfo,
    });

    // 에러 로깅
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    
    // 외부 에러 핸들러 호출
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // 실제 앱에서는 에러 리포팅 서비스에 전송
    // 예: Sentry, Crashlytics 등
  }

  handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  render() {
    if (this.state.hasError) {
      // 커스텀 fallback이 제공된 경우
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // 기본 에러 화면
      return (
        <View style={styles.container}>
          <View style={styles.errorContainer}>
            <Text style={styles.errorEmoji}>😵</Text>
            <Text style={styles.errorTitle}>앗, 문제가 발생했어요!</Text>
            <Text style={styles.errorMessage}>
              예상치 못한 오류가 발생했습니다.{'\n'}
              잠시 후 다시 시도해주세요.
            </Text>
            
            <TouchableOpacity
              style={styles.retryButton}
              onPress={this.handleRetry}
            >
              <Text style={styles.retryButtonText}>다시 시도</Text>
            </TouchableOpacity>

            {__DEV__ && (
              <ScrollView style={styles.debugContainer}>
                <Text style={styles.debugTitle}>디버그 정보:</Text>
                <Text style={styles.debugText}>
                  {this.state.error?.toString()}
                </Text>
                {this.state.errorInfo && (
                  <Text style={styles.debugText}>
                    {this.state.errorInfo.componentStack}
                  </Text>
                )}
              </ScrollView>
            )}
          </View>
        </View>
      );
    }

    return this.props.children;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.systemBackground,
    justifyContent: 'center',
    alignItems: 'center',
    padding: Spacing.xl,
  },
  errorContainer: {
    alignItems: 'center',
    maxWidth: 300,
  },
  errorEmoji: {
    fontSize: 64,
    marginBottom: Spacing.lg,
  },
  errorTitle: {
    ...Typography.title1,
    color: Colors.label,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  errorMessage: {
    ...Typography.body,
    color: Colors.secondaryLabel,
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: Spacing.xl,
  },
  retryButton: {
    backgroundColor: Colors.systemBlue,
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.lg,
    marginBottom: Spacing.lg,
  },
  retryButtonText: {
    ...Typography.headline,
    color: Colors.white,
    fontWeight: '600',
  },
  debugContainer: {
    maxHeight: 200,
    width: '100%',
    backgroundColor: Colors.secondarySystemBackground,
    borderRadius: BorderRadius.md,
    padding: Spacing.md,
  },
  debugTitle: {
    ...Typography.caption1,
    color: Colors.label,
    fontWeight: '600',
    marginBottom: Spacing.xs,
  },
  debugText: {
    ...Typography.caption2,
    color: Colors.secondaryLabel,
    fontFamily: 'Courier',
  },
});

export default ErrorBoundary;
