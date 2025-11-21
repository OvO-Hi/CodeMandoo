//check
import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  Image,
  TextInput,
  Modal,
  Platform,
} from 'react-native';
import {
  SafeAreaView,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';

import { useAtom } from 'jotai';
import { userProfileAtom, resetUserDataAtom } from '../../atoms/userAtoms';
import { ticketsAtom, basePromptAtom } from '../../atoms/ticketAtoms';
import { logoutAtom, deleteAccountAtom } from '../../atoms/userAtomsApi';

import { isPlaceholderTicket } from '../../utils/isPlaceholder';
import { resolveImageUrl } from '../../utils/resolveImageUrl';

import {
  Colors,
  Typography,
  Spacing,
  BorderRadius,
  Shadows,
  ComponentStyles,
} from '../../styles/designSystem';

import ModalHeader from '../../components/ModalHeader';
import { useUserProfileData } from '../../hooks/useApiData';
import { UserProfile } from '../../types/user';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback } from 'react';
import { fetchMyProfileAtom } from '../../atoms/userAtomsApi';

interface SettingsPageProps {
  navigation: any;
}

const SettingsPage: React.FC<SettingsPageProps> = ({ navigation }) => {
  const insets = useSafeAreaInsets();

  // 백엔드 프로필 불러오기
  const { data: profile } = useUserProfileData({ autoFetch: true });
  const [, fetchMyProfile] = useAtom(fetchMyProfileAtom);

  // 화면 포커스 시 프로필 새로고침
  useFocusEffect(
    useCallback(() => {
      fetchMyProfile(true);
    }, [fetchMyProfile])
  );

  // local atom 데이터
  const [localProfile] = useAtom(userProfileAtom);
  const [tickets] = useAtom(ticketsAtom);

  // 최종 프로필 결정 (백엔드 → 로컬 atom → fallback)
  const actualProfile: UserProfile =
    profile ??
    localProfile ?? {
      id: '',
      nickname: '사용자',
      email: '',
      profileImage: null,
      createdAt: null,
      updatedAt: null,
      isAccountPrivate: false,
    };

  // 프로필 이미지 URL 변환
  const resolvedImageUrl = resolveImageUrl(actualProfile.profileImage);

  // 티켓 계산
  const realTickets = tickets.filter((t) => !isPlaceholderTicket(t));

  // atoms
  const [, logout] = useAtom(logoutAtom);
  const [, resetUserData] = useAtom(resetUserDataAtom);
  const [, deleteAccount] = useAtom(deleteAccountAtom);
  const [, setBasePrompt] = useAtom(basePromptAtom);

  // 회원탈퇴 모달 상태
  const [deleteAccountModalVisible, setDeleteAccountModalVisible] = useState(false);
  const [deleteAccountPassword, setDeleteAccountPassword] = useState('');

  // 로그아웃
  const handleLogout = async () => {
    Alert.alert('로그아웃', '정말 로그아웃 하시겠습니까?', [
      { text: '취소', style: 'cancel' },
      {
        text: '로그아웃',
        style: 'destructive',
        onPress: async () => {
          try {
            await logout();
            resetUserData();
            setBasePrompt(null); // 로그아웃 시 basePrompt 초기화

            navigation.reset({
              index: 0,
              routes: [{ name: 'Login' as never }],
            });
          } catch (error) {
            Alert.alert('오류', '로그아웃 중 문제가 발생했습니다.');
          }
        },
      },
    ]);
  };

  // 회원탈퇴 alert (iOS = prompt 사용)
  const handleDeleteAccount = () => {
    if (Platform.OS === 'ios') {
      Alert.prompt(
        '회원 탈퇴',
        '정말 탈퇴하시겠습니까?\n되돌릴 수 없습니다.\n\n비밀번호를 입력하세요.',
        [
          { text: '취소', style: 'cancel' },
          {
            text: '탈퇴',
            style: 'destructive',
            onPress: async (password) => {
              if (!password) return;
              executeDeleteAccount(password);
            },
          },
        ],
        'secure-text'
      );
    } else {
      setDeleteAccountModalVisible(true);
    }
  };

  // 실제 탈퇴 실행
  const executeDeleteAccount = async (password: string) => {
    try {
      const result = await deleteAccount(password);

      if (result.success) {
        resetUserData();
        setDeleteAccountModalVisible(false);
        setDeleteAccountPassword('');

        navigation.reset({
          index: 0,
          routes: [{ name: 'Login' as never }],
        });
      } else {
        Alert.alert('오류', result.error?.message || '탈퇴 실패');
      }
    } catch {
      Alert.alert('오류', '회원탈퇴 중 문제가 발생했습니다.');
    }
  };

  // Android 모달 확인
  const handleDeleteAccountConfirm = () => {
    if (!deleteAccountPassword.trim()) {
      Alert.alert('오류', '비밀번호를 입력해주세요.');
      return;
    }
    executeDeleteAccount(deleteAccountPassword.trim());
  };

  // 설정 리스트
  const settingsOptions = [
    {
      id: 1,
      title: '개인정보 수정',
      icon: '👤',
      onPress: () => navigation.navigate('PersonalInfoEdit'),
      showArrow: true,
    },
    {
      id: 2,
      title: '히스토리',
      icon: '📋',
      onPress: () => navigation.navigate('History'),
      showArrow: true,
    },
    {
      id: 3,
      title: '로그아웃',
      icon: '🚪',
      onPress: handleLogout,
      showArrow: false,
      textColor: '#FF6B6B',
    },
    {
      id: 4,
      title: '회원 탈퇴',
      icon: '⚠️',
      onPress: handleDeleteAccount,
      showArrow: false,
      textColor: '#FF3B30',
    },
  ];

  return (
    <SafeAreaView style={styles.container}>
      <ModalHeader title="설정" onBack={() => navigation.goBack()} />

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {/* 유저 섹션 */}
        <View style={styles.userSection}>
          <TouchableOpacity
            style={styles.avatarContainer}
            onPress={() => navigation.navigate('PersonalInfoEdit')}
          >
            {resolvedImageUrl ? (
              <Image source={{ uri: resolvedImageUrl }} style={styles.avatarImage} />
            ) : (
              <View style={[styles.avatarImage, styles.defaultAvatar]}>
                <Text style={styles.defaultAvatarText}>👤</Text>
              </View>
            )}
          </TouchableOpacity>

          <Text style={styles.username}>{actualProfile.nickname}</Text>
        </View>

        {/* 옵션 리스트 */}
        <View style={styles.optionsContainer}>
          {settingsOptions.map((option) => (
            <TouchableOpacity
              key={option.id}
              style={styles.optionItem}
              onPress={option.onPress}
            >
              <View style={styles.optionLeft}>
                <Text style={styles.optionIcon}>{option.icon}</Text>
                <Text
                  style={[
                    styles.optionTitle,
                    option.textColor && { color: option.textColor },
                  ]}
                >
                  {option.title}
                </Text>
              </View>

              {option.showArrow && <Text style={styles.optionArrow}>→</Text>}
            </TouchableOpacity>
          ))}
        </View>

        {/* 버전 정보 */}
        <View style={styles.versionContainer}>
          <Text style={styles.versionText}>버전 1.0.0</Text>
        </View>
      </ScrollView>

      {/* Android 탈퇴 모달 */}
      <Modal
        visible={deleteAccountModalVisible}
        transparent
        animationType="fade"
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>회원 탈퇴</Text>
            <Text style={styles.modalMessage}>
              정말 탈퇴하시겠습니까?{'\n'}
              되돌릴 수 없습니다.{'\n\n'}
              비밀번호를 입력해주세요.
            </Text>

            <TextInput
              style={styles.modalInput}
              placeholder="비밀번호"
              placeholderTextColor={Colors.tertiaryLabel}
              value={deleteAccountPassword}
              onChangeText={setDeleteAccountPassword}
              secureTextEntry
            />

            <View style={styles.modalButtons}>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalButtonCancel]}
                onPress={() => {
                  setDeleteAccountModalVisible(false);
                  setDeleteAccountPassword('');
                }}
              >
                <Text style={styles.modalButtonCancelText}>취소</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.modalButton, styles.modalButtonDelete]}
                onPress={handleDeleteAccountConfirm}
              >
                <Text style={styles.modalButtonDeleteText}>탈퇴</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
};

// 스타일 정의
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.secondarySystemBackground,
  },
  content: {
    flex: 1,
  },
  userSection: {
    backgroundColor: Colors.systemBackground,
    alignItems: 'center',
    paddingVertical: Spacing.xxxl,
    borderBottomColor: Colors.systemGray5,
    borderBottomWidth: 0.5,
    marginBottom: Spacing.sectionSpacing,
  },
  avatarContainer: {},
  avatarImage: {
    width: 120,
    height: 120,
    borderRadius: 60,
    backgroundColor: Colors.systemGray5,
  },
  defaultAvatar: {
    backgroundColor: Colors.systemGray5,
    justifyContent: 'center',
    alignItems: 'center',
  },
  defaultAvatarText: {
    fontSize: 48,
    color: Colors.secondaryLabel,
  },
  username: {
    ...Typography.title1,
    fontWeight: 'bold',
    color: Colors.label,
    paddingVertical: 12,
  },
  optionsContainer: {
    ...ComponentStyles.card,
    marginHorizontal: Spacing.lg,
    borderRadius: BorderRadius.lg,
    overflow: 'hidden',
  },
  optionItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.lg,
    borderBottomWidth: 0.5,
    borderBottomColor: Colors.separator,
  },
  optionLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  optionIcon: {
    fontSize: 20,
    marginRight: 16,
  },
  optionTitle: {
    ...Typography.callout,
    fontWeight: '500',
    color: Colors.label,
  },
  optionArrow: {
    ...Typography.callout,
    color: Colors.systemGray2,
  },
  versionContainer: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  versionText: {
    ...Typography.footnote,
    color: Colors.tertiaryLabel,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    padding: Spacing.xl,
  },
  modalContent: {
    backgroundColor: Colors.systemBackground,
    borderRadius: BorderRadius.xl,
    padding: Spacing.xl,
    ...Shadows.large,
  },
  modalTitle: {
    ...Typography.title2,
    fontWeight: '600',
    color: Colors.label,
    marginBottom: Spacing.md,
  },
  modalMessage: {
    ...Typography.body,
    color: Colors.secondaryLabel,
    marginBottom: Spacing.lg,
    lineHeight: 20,
  },
  modalInput: {
    ...ComponentStyles.input,
    marginBottom: Spacing.lg,
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: Spacing.md,
  },
  modalButton: {
    paddingHorizontal: Spacing.xl,
    paddingVertical: Spacing.md,
    borderRadius: BorderRadius.md,
  },
  modalButtonCancel: {
    backgroundColor: Colors.systemGray5,
  },
  modalButtonDelete: {
    backgroundColor: '#FF3B30',
  },
  modalButtonCancelText: {
    ...Typography.callout,
    fontWeight: '600',
    color: Colors.label,
  },
  modalButtonDeleteText: {
    ...Typography.callout,
    fontWeight: '600',
    color: Colors.white,
  },
});

export default SettingsPage;
