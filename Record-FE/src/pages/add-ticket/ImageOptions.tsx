// === ImageOptions.tsx (UI 절대 수정 없음, 로직만 호환 수정) ===

import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  Platform,
  ActionSheetIOS,
  ScrollView,
  Alert,
} from 'react-native';
import {
  SafeAreaView,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import { useNavigation, useRoute } from '@react-navigation/native';
import {
  launchImageLibrary,
  launchCamera,
  ImageLibraryOptions,
  Asset,
} from 'react-native-image-picker';
import { useAtom } from 'jotai';
import { addTicketAtom, TicketStatus, basePromptAtom } from '../../atoms';
import { sttService } from '../../services/api/sttService';
import {
  ImageOptionsScreenNavigationProp,
  ImageOptionsRouteProp,
} from '../../types/reviewTypes';
import {
  Colors,
  Typography,
  Spacing,
  BorderRadius,
  Shadows,
  ComponentStyles,
  Layout,
} from '../../styles/designSystem';
import { Ticket, CreateTicketData } from '../../types/ticket';

const ImageOptions = () => {
  const navigation = useNavigation<ImageOptionsScreenNavigationProp>();
  const route = useRoute<ImageOptionsRouteProp>();
  const { ticketData, reviewData } = route.params;
  const [, addTicket] = useAtom(addTicketAtom);

  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  /**
   * 🎨 AI 이미지 생성
   * 1. /reviews/summarize 호출하여 5줄 영어 요약 생성
   * 2. basePrompt로 저장
   * 3. AIImageResults로 이동
   */
  const [, setBasePrompt] = useAtom(basePromptAtom);
  const [isGeneratingSummary, setIsGeneratingSummary] = React.useState(false);

  const handleAIImageSelect = async () => {
    const reviewText = reviewData.reviewText || reviewData.text || '';
    
    if (!reviewText.trim()) {
      Alert.alert('오류', '후기 내용이 없습니다.');
      return;
    }

    setIsGeneratingSummary(true);

    try {
      // /review/summarize 호출하여 한국어 요약 생성
      // 이유: sttService.summarizeReview는 Result<string>을 반환하므로,
      //      result.data가 직접 요약된 텍스트 문자열입니다.
      const result = await sttService.summarizeReview(reviewText);

      if (result.success && result.data) {
        // result.data는 이미 요약된 텍스트 문자열입니다 (summary 필드가 아님)
        const summary = result.data;
        
        if (summary && summary.trim().length > 0) {
          // basePrompt로 저장
          setBasePrompt(summary);
          console.log('✅ basePrompt 저장:', summary);

          const defaultSettings = {
            backgroundColor: '자동',
            includeText: true,
            imageStyle: '사실적',
            aspectRatio: '정사각형',
          };

          navigation.navigate('AIImageResults', {
            ticketData,
            reviewData: {
              reviewText: reviewText,
            },
            images: [],
            settings: defaultSettings,
          });
        } else {
          Alert.alert('오류', '요약 생성에 실패했습니다. (빈 요약)');
        }
      } else {
        Alert.alert('오류', result.error?.message || '요약 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('요약 생성 오류:', error);
      Alert.alert('오류', '요약 생성 중 문제가 발생했습니다.');
    } finally {
      setIsGeneratingSummary(false);
    }
  };

  /**
   * 📷 갤러리 선택
   */
  const handleGallerySelect = () => {
    const options: ImageLibraryOptions = {
      mediaType: 'photo',
      includeBase64: true,
      quality: 1.0,
      includeExtra: true,
      maxHeight: 2000,
      maxWidth: 2000,
      selectionLimit: 1,
    };

    launchImageLibrary(options, response => {
      if (response.didCancel) return;
      if (response.errorCode) {
        console.error(response.errorMessage);
        return;
      }

      const asset: Asset | undefined = response.assets?.[0];
      if (asset?.uri) {
        console.log('갤러리 선택:', asset.uri);
        setSelectedImage(asset.uri);

        navigation.navigate('TicketComplete', {
          ticketData,
          reviewData: {
            reviewText: reviewData.reviewText || reviewData.text || '',
          },
          images: [asset.uri],
        });
      }
    });
  };

  /**
   * 📸 카메라 or 갤러리 선택
   */
  const handleGalleryOrCameraSelect = () => {
    if (Platform.OS === 'ios') {
      ActionSheetIOS.showActionSheetWithOptions(
        {
          options: ['취소', '사진 찍기', '사진 보관함에서 선택'],
          cancelButtonIndex: 0,
        },
        buttonIndex => {
          if (buttonIndex === 1) {
            // Camera
            launchCamera(
              {
                mediaType: 'photo',
                maxHeight: 2000,
                maxWidth: 2000,
                quality: 0.8,
              },
              response => {
                if (response.didCancel) return;
                if (response.errorCode) {
                  console.error(response.errorMessage);
                  return;
                }
                const asset: Asset | undefined = response.assets?.[0];
                if (asset?.uri) {
                  console.log('카메라 촬영:', asset.uri);
                  setSelectedImage(asset.uri);

                  navigation.navigate('TicketComplete', {
                    ticketData,
                    reviewData: {
                      reviewText: reviewData.reviewText || reviewData.text || '',
                    },
                    images: [asset.uri],
                  });
                }
              },
            );
          } else if (buttonIndex === 2) {
            handleGallerySelect();
          }
        },
      );
    } else {
      handleGallerySelect();
    }
  };

  /**
   * 📌 이미지 없이 완료 (저장)
   */
  const handleSkipImages = () => {
    try {
      const ticketToSave = {
        ...ticketData,
        review: {
          reviewText: reviewData.reviewText || reviewData.text || '',
        },
        createdAt: new Date(),
        images: [],
      };

      addTicket(ticketToSave);

      Alert.alert('티켓 저장 완료', '티켓이 성공적으로 저장되었습니다.', [
        {
          text: '확인',
          onPress: () => {
            navigation.reset({
              index: 0,
              routes: [{ name: 'MainTabs' as never }],
            });
          },
        },
      ]);
    } catch (error) {
      Alert.alert('오류', '티켓 저장 중 오류가 발생했습니다.');
    }
  };

  return (
    <SafeAreaView style={styles.container} edges={['left', 'right']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>티켓 이미지 선택하기</Text>
        <View style={styles.placeholder} />
      </View>

      <ScrollView style={styles.scrollView}>
        <View style={styles.contextMessage}>
          <Text style={styles.contextSubtitle}>
            기억에 남는 장면을 이미지로 표현해보세요
          </Text>
        </View>

        <View style={styles.optionsContainer}>
          {/* AI 이미지 */}
          <TouchableOpacity
            style={[styles.generateButton, isGeneratingSummary && styles.generateButtonDisabled]}
            onPress={handleAIImageSelect}
            disabled={isGeneratingSummary}
          >
            <View style={styles.buttonContent}>
              <View style={styles.textContainer}>
                <Text style={styles.optionButtonText}>
                  {isGeneratingSummary ? '이미지 생성 중...' : 'AI 이미지'}
                </Text>
                <Text style={styles.optionButtonSubText}>
                  {isGeneratingSummary
                    ? '잠시만 기다려주세요...'
                    : 'AI가 만들어주는 나만의 티켓 이미지 ~'}
                </Text>
              </View>
              <Image
                source={require('../../assets/mic.png')}
                style={styles.buttonIcon}
              />
            </View>
          </TouchableOpacity>

          {/* 직접 선택하기 */}
          <TouchableOpacity
            style={[styles.optionButton]}
            onPress={handleGalleryOrCameraSelect}
          >
            <View style={styles.buttonContent}>
              <View style={styles.textContainer}>
                <Text style={[styles.optionButtonText, { color: '#000000' }]}>
                  직접 선택하기
                </Text>
                <Text
                  style={[styles.optionButtonSubText, { color: '#8E8E93' }]}
                >
                  사진 찍기 또는 사진 보관함에서 선택하세요.
                </Text>
              </View>
              <Image
                source={require('../../assets/mic.png')}
                style={styles.buttonIcon}
              />
            </View>
          </TouchableOpacity>
        </View>
      </ScrollView>

      {/* 이미지 스킵 */}
      <View style={styles.bottomButtonContainer}>
        <TouchableOpacity style={styles.skipButton} onPress={handleSkipImages}>
          <Text style={styles.skipButtonText}>이미지 없이 완료</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

// === 이하 UI — 절대 수정 없음 ===
const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F2F2F7' },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: Spacing.lg,
    backgroundColor: Colors.systemBackground,
    ...Shadows.small,
    zIndex: 1,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: BorderRadius.round,
    backgroundColor: Colors.secondarySystemBackground,
    justifyContent: 'center',
    alignItems: 'center',
    ...Shadows.small,
    zIndex: 2,
  },
  backButtonText: {
    ...Typography.title3,
    color: Colors.label,
    fontWeight: 'bold',
  },
  headerTitle: {
    ...Typography.headline,
    color: Colors.label,
    position: 'absolute',
    left: 0,
    right: 0,
    textAlign: 'center',
  },
  placeholder: { position: 'absolute', right: Spacing.lg, width: 44, height: 44 },
  scrollView: { flex: 1, paddingHorizontal: Spacing.screenPadding },
  contextMessage: {
    backgroundColor: Colors.secondarySystemBackground,
    paddingVertical: Spacing.lg,
    borderBottomWidth: 1,
    borderBottomColor: Colors.systemGray5,
  },
  contextSubtitle: {
    ...Typography.footnote,
    color: Colors.secondaryLabel,
    textAlign: 'left',
    lineHeight: 20,
  },
  optionsContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginVertical: 16,
  },
  optionButton: {
    flex: 1,
    borderRadius: 12,
    backgroundColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    marginHorizontal: 4,
  },
  generateButton: {
    flex: 1,
    alignItems: 'center',
    borderRadius: 12,
    backgroundColor: '#B11515',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    marginHorizontal: 4,
  },
  generateButtonDisabled: {
    opacity: 0.6,
  },
  optionButtonText: {
    fontSize: 17,
    fontWeight: '600',
    color: '#FFFFFF',
    marginTop: 16,
    marginBottom: 8,
  },
  optionButtonSubText: {
    fontSize: 15,
    fontWeight: '400',
    color: '#FFFFFF',
  },
  buttonContent: { alignItems: 'flex-end', paddingHorizontal: 16 },
  buttonIcon: { width: 50, height: 90, marginTop: 32, marginBottom: 16 },
  textContainer: { flexDirection: 'column' },
  bottomButtonContainer: { paddingHorizontal: 24, paddingVertical: 36 },
  skipButton: {
    backgroundColor: '#8E8E93',
    paddingVertical: 16,
    paddingHorizontal: 24,
    borderRadius: 12,
    alignItems: 'center',
  },
  skipButtonText: { color: '#FFFFFF', fontSize: 17, fontWeight: '600' },
});

export default ImageOptions;
