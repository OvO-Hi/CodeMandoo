//check
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  SafeAreaView,
  ActivityIndicator,
  Alert,
  TextInput,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { Colors, Typography, Spacing, BorderRadius, Shadows } from '../../styles/designSystem';
import { authService } from '../../services/auth/authService';
import { useNavigation } from '@react-navigation/native';

const SignupPage = () => {
  const navigation = useNavigation();
  const [isLoading, setIsLoading] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [isVerified, setIsVerified] = useState(false);
  const [isSendingCode, setIsSendingCode] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0);

  // 타이머 useEffect
  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null;
    
    if (timeLeft > 0) {
      interval = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            setIsCodeSent(false);
            setVerificationCode('');
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [timeLeft]);

  // 타이머 포맷팅 함수 (mm:ss)
  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const handleSendVerificationCode = async () => {
    // 이메일 빈 값 검증
    if (!email.trim()) {
      Alert.alert('입력 오류', '이메일을 입력해주세요.', [{ text: '확인' }]);
      return;
    }

    // 이메일 형식 검증
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      Alert.alert('입력 오류', '올바른 이메일 형식이 아닙니다.', [{ text: '확인' }]);
      return;
    }

    // 이메일 코드 발송
    setIsSendingCode(true);
    try {
      const result = await authService.sendSignupVerificationCode(email);
      
      if (result.success) {
        // 성공 시
        setIsCodeSent(true);
        setTimeLeft(600);
        Alert.alert('인증 코드 발송', '인증 코드가 이메일로 발송되었습니다.', [{ text: '확인' }]);
      } else {
        // 에러 메시지가 메일 인증 관련인 경우 더 친화적인 메시지 표시
        let errorMessage = result.error?.message || '인증 코드 발송 중 오류가 발생했습니다.';
        if (errorMessage.includes('메일 서비스') || errorMessage.includes('Authentication failed') || errorMessage.includes('인증에 실패')) {
          errorMessage = '메일 서비스 설정에 문제가 있습니다.\n\n백엔드 관리자에게 다음을 확인해달라고 요청해주세요:\n• Gmail 앱 비밀번호 설정\n• MAIL_USERNAME, MAIL_PASSWORD 환경변수 확인\n• 백엔드 서버 재시작';
        }
        
        Alert.alert(
          '인증 코드 발송 실패',
          errorMessage,
          [{ text: '확인' }]
        );
      }
    } catch (error) {
      Alert.alert('인증 코드 발송 실패', '인증 코드 발송 중 오류가 발생했습니다.', [{ text: '확인' }]);
    } finally {
      setIsSendingCode(false);
    }
  };

  // 인증 코드 검증

  const handleVerifyCode = async () => {
    // 인증 코드 빈 값 검증
    if (!verificationCode.trim()) {
      Alert.alert('입력 오류', '인증 코드를 입력해주세요.', [{ text: '확인' }]);
      return;
    }

    // 6자리 검증
    if (verificationCode.length !== 6 || !/^\d+$/.test(verificationCode)) {
      Alert.alert('입력 오류', '6자리 숫자 코드를 입력해주세요.', [{ text: '확인' }]);
      return;
    }

    setIsLoading(true);
    try {
      const result = await authService.verifySignupCode(email, verificationCode);
      
      if (result.success) {
        // 성공 시
        setIsVerified(true);
        setTimeLeft(0);
        Alert.alert('인증 완료', '이메일 인증이 완료되었습니다.', [{ text: '확인' }]);
      } else {
        Alert.alert(
          '인증 실패',
          result.error?.message || '인증 코드가 올바르지 않습니다.',
          [{ text: '확인' }]
        );
      }
    } catch (error) {
      Alert.alert('인증 실패', '인증 코드 검증 중 오류가 발생했습니다.', [{ text: '확인' }]);
    } finally {
      setIsLoading(false);
    }
  };

  // 유효값 검증
  const validateInputs = () => {
    if (!username.trim()) {
      Alert.alert('입력 오류', '아이디를 입력해주세요.', [{ text: '확인' }]);
      return false;
    }
    if (username.length < 3 || username.length > 15) {
      Alert.alert('입력 오류', '아이디는 3자 이상, 15자 이하여야 합니다.', [{ text: '확인' }]);
      return false;
    }
    if (!password.trim()) {
      Alert.alert('입력 오류', '비밀번호를 입력해주세요.', [{ text: '확인' }]);
      return false;
    }
    if (password.length < 8) {
      Alert.alert('입력 오류', '비밀번호는 8자 이상이어야 합니다.', [{ text: '확인' }]);
      return false;
    }
    if (password !== confirmPassword) {
      Alert.alert('입력 오류', '비밀번호가 일치하지 않습니다.', [{ text: '확인' }]);
      return false;
    }
    if (!email.trim()) {
      Alert.alert('입력 오류', '이메일을 입력해주세요.', [{ text: '확인' }]);
      return false;
    }
    // Simple email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      Alert.alert('입력 오류', '올바른 이메일 형식이 아닙니다.', [{ text: '확인' }]);
      return false;
    }
    if (!nickname.trim()) {
      Alert.alert('입력 오류', '닉네임을 입력해주세요.', [{ text: '확인' }]);
      return false;
    }
    if (nickname.length > 30) {
      Alert.alert('입력 오류', '닉네임은 30자 이하여야 합니다.', [{ text: '확인' }]);
      return false;
    }
    if (!isVerified) {
      Alert.alert('인증 필요', '이메일 인증을 완료해주세요.', [{ text: '확인' }]);
      return false;
    }
    return true;
  };

  const handleSignup = async () => {
    if (!validateInputs()) {
      return;
    }

    setIsLoading(true);
    try {
      const result = await authService.signUp(username, password, email, nickname);
      
      if (result.success) {
        Alert.alert(
          '회원가입 완료',
          '회원가입이 완료되었습니다. 로그인 화면으로 돌아갑니다.',
          [
            {
              text: '확인',
              onPress: () => {
                // 회원가입 완료 후 로그인 화면으로 돌아가기
                navigation.goBack();
              },
            },
          ]
        );
      } else {
        Alert.alert(
          '회원가입 실패',
          result.error?.message || '회원가입 중 오류가 발생했습니다.',
          [{ text: '확인' }]
        );
      }
    } catch (error) {
      Alert.alert(
        '회원가입 실패',
        '예상치 못한 오류가 발생했습니다.',
        [{ text: '확인' }]
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoToLogin = () => {
    navigation.goBack();
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          {/* Logo Section */}
          <View style={styles.logoSection}>
            <Text style={styles.appTitle}>Record</Text>
            <Text style={styles.appSubtitle}>새로운 계정을 만들어보세요</Text>
          </View>

          {/* Signup Form Section */}
          <View style={styles.formSection}>
            {/* Username Input */}
            <View style={styles.inputContainer}>
              <View style={styles.labelRow}>
                <Text style={styles.inputLabel}>아이디</Text>
                <Text style={styles.conditionLabel}>*3자 이상, 15자 이하</Text>
              </View>
              <TextInput
                style={styles.input}
                placeholder="아이디를 입력하세요"
                placeholderTextColor={Colors.placeholderText}
                value={username}
                onChangeText={setUsername}
                autoCapitalize="none"
                autoCorrect={false}
                editable={!isLoading}
              />
            </View>

            {/* Password Input */}
            <View style={styles.inputContainer}>
              <View style={styles.labelRow}>
                <Text style={styles.inputLabel}>비밀번호</Text>
                <Text style={styles.conditionLabel}>*8자 이상</Text>
              </View>
              <TextInput
                style={styles.input}
                placeholder="비밀번호를 입력하세요"
                placeholderTextColor={Colors.placeholderText}
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                autoCapitalize="none"
                autoCorrect={false}
                editable={!isLoading}
              />
            </View>

            {/* Confirm Password Input */}
            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>비밀번호 확인</Text>
              <TextInput
                style={styles.input}
                placeholder="비밀번호를 다시 입력하세요"
                placeholderTextColor={Colors.placeholderText}
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                secureTextEntry
                autoCapitalize="none"
                autoCorrect={false}
                editable={!isLoading}
              />
            </View>

            {/* Email Input */}
            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>이메일</Text>
              <View style={styles.emailInputRow}>
                <TextInput
                  style={styles.emailInput}
                  placeholder="이메일을 입력하세요"
                  placeholderTextColor={Colors.placeholderText}
                  value={email}
                  onChangeText={setEmail}
                  keyboardType="email-address"
                  autoCapitalize="none"
                  autoCorrect={false}
                  editable={!isLoading && !isVerified}
                />
                <TouchableOpacity
                  style={[
                    styles.verifyButton,
                    (isLoading || isSendingCode || isVerified) && styles.verifyButtonDisabled,
                  ]}
                  onPress={handleSendVerificationCode}
                  disabled={isLoading || isSendingCode || isVerified}
                  activeOpacity={0.8}
                >
                  {isSendingCode ? (
                    <ActivityIndicator color={Colors.white} size="small" />
                  ) : (
                    <Text style={styles.verifyButtonText}>
                      {isVerified ? '인증완료' : '인증하기'}
                    </Text>
                  )}
                </TouchableOpacity>
              </View>
            </View>

            {/* Verification Code Input */}
            {isCodeSent && !isVerified && (
              <View style={styles.inputContainer}>
                <View style={styles.codeLabelRow}>
                  <Text style={styles.inputLabel}>인증 코드</Text>
                  {timeLeft > 0 && (
                    <Text style={styles.timerText}>{formatTime(timeLeft)}</Text>
                  )}
                </View>
                <View style={styles.emailInputRow}>
                  <TextInput
                    style={styles.emailInput}
                    placeholder="6자리 코드 입력"
                    placeholderTextColor={Colors.placeholderText}
                    value={verificationCode}
                    onChangeText={(text) => {
                      // 숫자만 입력 허용, 최대 6자리
                      const numericText = text.replace(/[^0-9]/g, '').slice(0, 6);
                      setVerificationCode(numericText);
                    }}
                    keyboardType="number-pad"
                    maxLength={6}
                    autoCapitalize="none"
                    autoCorrect={false}
                    editable={!isLoading && timeLeft > 0}
                  />
                  <TouchableOpacity
                    style={[
                      styles.verifyButton,
                      isLoading && styles.verifyButtonDisabled,
                    ]}
                    onPress={handleVerifyCode}
                    disabled={isLoading || timeLeft === 0}
                    activeOpacity={0.8}
                  >
                    {isLoading ? (
                      <ActivityIndicator color={Colors.white} size="small" />
                    ) : (
                      <Text style={styles.verifyButtonText}>확인</Text>
                    )}
                  </TouchableOpacity>
                </View>
              </View>
            )}

            {/* Nickname Input */}
            <View style={styles.inputContainer}>
              <View style={styles.labelRow}>
                <Text style={styles.inputLabel}>닉네임</Text>
                <Text style={styles.conditionLabel}>*30자 이하</Text>
              </View>
              <TextInput
                style={styles.input}
                placeholder="닉네임을 입력하세요"
                placeholderTextColor={Colors.placeholderText}
                value={nickname}
                onChangeText={setNickname}
                autoCorrect={false}
                editable={!isLoading}
              />
            </View>

            {/* Signup Button */}
            <TouchableOpacity
              style={[
                styles.signupButton,
                isLoading && styles.signupButtonDisabled,
              ]}
              onPress={handleSignup}
              disabled={isLoading}
              activeOpacity={0.8}
            >
              {isLoading ? (
                <ActivityIndicator color={Colors.white} />
              ) : (
                <Text style={styles.signupButtonText}>회원가입</Text>
              )}
            </TouchableOpacity>

            {/* Login Link */}
            <View style={styles.loginContainer}>
              <Text style={styles.loginText}>이미 계정이 있으신가요? </Text>
              <TouchableOpacity onPress={handleGoToLogin}>
                <Text style={styles.loginLink}>로그인</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Footer */}
          <View style={styles.footer}>
            <Text style={styles.footerText}>
              © 2025 Record. All rights reserved.
            </Text>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.systemBackground,
  },
  keyboardView: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    paddingHorizontal: Spacing.screenPadding,
    justifyContent: 'space-between',
  },
  logoSection: {
    alignItems: 'flex-start',
    paddingTop: 40,
    paddingBottom: 40,
  },

  appTitle: {
    ...Typography.title1,
    fontWeight: '600',
    color: Colors.label,
    marginBottom: Spacing.xs,
  },
  appSubtitle: {
    ...Typography.body,
    color: Colors.secondaryLabel,
    textAlign: 'center',
  },
  formSection: {
    flex: 1,
  },

  inputContainer: {
    marginBottom: Spacing.xl,
  },
  labelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.xs,
  },
  conditionLabel: {
    ...Typography.footnote,
    color: Colors.secondaryLabel,
    textAlign: 'right',
    marginBottom: Spacing.xs,
  },
  inputLabel: {
    ...Typography.subheadline,
    fontWeight: '600',
    color: Colors.label,
    marginBottom: Spacing.xs,
  },
  input: {
    backgroundColor: Colors.systemBackground,
    borderWidth: 1,
    borderColor: Colors.systemGray5,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.lg,
    ...Typography.body,
    color: Colors.label,
    ...Shadows.small,
  },
  signupButton: {
    backgroundColor: Colors.primary,
    borderRadius: BorderRadius.lg,
    paddingVertical: Spacing.lg,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: Spacing.lg,
    marginBottom: Spacing.lg,
    ...Shadows.button,
  },
  signupButtonDisabled: {
    opacity: 0.6,
  },
  signupButtonText: {
    ...Typography.headline,
    color: Colors.white,
    fontWeight: '600',
  },
  loginContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loginText: {
    ...Typography.subheadline,
    color: Colors.secondaryLabel,
  },
  loginLink: {
    ...Typography.subheadline,
    color: Colors.primary,
    fontWeight: '600',
  },
  footer: {
    paddingVertical: Spacing.lg,
    alignItems: 'center',
  },
  footerText: {
    ...Typography.caption2,
    color: Colors.quaternaryLabel,
  },
  emailInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  emailInput: {
    flex: 1,
    backgroundColor: Colors.systemBackground,
    borderWidth: 1,
    borderColor: Colors.systemGray5,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.lg,
    ...Typography.body,
    color: Colors.label,
    ...Shadows.small,
    marginBottom: 0,
  },
  verifyButton: {
    backgroundColor: Colors.primary,
    borderRadius: BorderRadius.md,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.lg,
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 80,
    height: 48,
    ...Shadows.small,
  },
  verifyButtonDisabled: {
    opacity: 0.6,
  },
  verifyButtonText: {
    ...Typography.subheadline,
    color: Colors.white,
    fontWeight: '600',
  },
  codeLabelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.sm,
  },
  timerText: {
    ...Typography.subheadline,
    color: Colors.systemRed,
    fontWeight: '600',
  },
});

export default SignupPage;
