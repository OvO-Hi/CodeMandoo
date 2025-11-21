// === AIImageResults.tsx (UI 미변경, API 로직만 완전 수정) ===

import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  Image,
  ActivityIndicator,
  Dimensions,
  TextInput,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import {
  imageGenerationService,
  ImageGenerationRequest,
  ImageGenerationResponse,
} from '../../services/api';
import { Result } from '../../utils/result';
import { useAtom } from 'jotai';
import { basePromptAtom } from '../../atoms';
import {
  Colors,
  Typography,
  Spacing,
  BorderRadius,
  Shadows,
} from '../../styles/designSystem';

interface AIImageResultsProps {
  navigation: any;
  route?: {
    params?: {
      ticketData?: any;
      reviewData?: {
        rating: number;
        reviewText: string;
      };
      images?: string[];
      settings?: {
        backgroundColor: string;
        includeText: boolean;
        imageStyle: string;
        aspectRatio: string;
      };
    };
  };
}

const { width } = Dimensions.get('window');
const cardWidth = width - 48;
const cardHeight = (cardWidth * 5) / 4;

const AIImageResults: React.FC<AIImageResultsProps> = ({ navigation, route }) => {
  const [isGenerating, setIsGenerating] = useState(true);
  const [generatedImage, setGeneratedImage] = useState<string | null>(null);
  const [generationHistory, setGenerationHistory] = useState<string[]>([]);
  const [regenerationRequest, setRegenerationRequest] = useState<string>('');
  const [currentPrompt, setCurrentPrompt] = useState<string | null>(null);
  const [basePrompt] = useAtom(basePromptAtom);

  const ticketData = route?.params?.ticketData;
  const reviewData = route?.params?.reviewData;
  const settings = route?.params?.settings;

  useEffect(() => {
    handleGenerateAIImage();
  }, []);

  /** 🎨 장르 매핑 */
  const mapGenreForBackend = (frontendGenre: string): string => {
    if (frontendGenre?.includes('뮤지컬') || frontendGenre?.includes('연극'))
      return '뮤지컬';
    if (frontendGenre?.includes('밴드')) return '밴드';
    return '뮤지컬';
  };

  /** 🖼 이미지 최초 생성 */
  const handleGenerateAIImage = async () => {
    setIsGenerating(true);

    try {
      if (!ticketData?.title || !reviewData?.reviewText) {
        Alert.alert('오류', '티켓 정보 또는 후기 정보가 없습니다.');
        setIsGenerating(false);
        return;
      }

      // performedAt이 Date라면 문자열로 변환
      const dateValue =
        ticketData?.performedAt instanceof Date
          ? ticketData.performedAt.toISOString()
          : ticketData?.performedAt ?? '';

      const requestData: ImageGenerationRequest = {
        title: ticketData.title,
        review: reviewData.reviewText,
        genre: mapGenreForBackend(ticketData.genre || ''),
        location: ticketData.venue || '', // place → venue로 변경
        date: dateValue,
        cast: [],
        basePrompt: basePrompt || undefined, // basePrompt 추가
      };

      console.log('🔍 이미지 생성 요청 데이터:', requestData);
      console.log('📋 basePrompt:', basePrompt);

      const result: Result<ImageGenerationResponse> =
        await imageGenerationService.generateImage(requestData);

      if (result.success && result.data) {
        const imageData = result.data;

        setGeneratedImage(imageData.imageUrl);
        setGenerationHistory(prev => [imageData.imageUrl, ...prev]);

        if (imageData.prompt) setCurrentPrompt(imageData.prompt);
      } else {
        Alert.alert('오류', result.error?.message || 'AI 이미지 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('❌ 이미지 생성 중 오류:', error);
      Alert.alert('오류', 'AI 이미지 생성 중 문제가 발생했습니다.');
    } finally {
      setIsGenerating(false);
    }
  };

  /** 🖌 이미지 재생성 */
  const handleRegenerateImage = async () => {
    if (!generatedImage) {
      Alert.alert('오류', '생성된 이미지가 없습니다.');
      return;
    }

    setIsGenerating(true);
    setGeneratedImage(null);

    try {
      const dateValue =
        ticketData?.performedAt instanceof Date
          ? ticketData.performedAt.toISOString()
          : ticketData?.performedAt ?? '';

      const requestData: ImageGenerationRequest = {
        title: ticketData.title,
        review: reviewData?.reviewText || '', // reviewData가 undefined일 수 있으므로 옵셔널 체이닝 사용
        genre: mapGenreForBackend(ticketData.genre || ''),
        location: ticketData.venue || '', // place → venue로 변경
        date: dateValue,
        cast: [],
        basePrompt: basePrompt || undefined, // basePrompt 추가
        imageRequest: regenerationRequest.trim() || undefined, // 사용자 요구사항 추가
      };

      console.log('🔄 재생성 요청:', requestData);
      console.log('📝 사용자 요구사항:', regenerationRequest);
      console.log('📋 basePrompt:', basePrompt);

      const result: Result<ImageGenerationResponse> =
        await imageGenerationService.generateImage(requestData);

      if (result.success && result.data) {
        const imageData = result.data;

        setGeneratedImage(imageData.imageUrl);
        setGenerationHistory(prev => [imageData.imageUrl, ...prev]);

        if (imageData.prompt) setCurrentPrompt(imageData.prompt);

        setRegenerationRequest('');
      } else {
        Alert.alert('오류', result.error?.message || '이미지 재생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('❌ 재생성 오류:', error);
      Alert.alert('오류', '이미지 재생성 중 문제가 발생했습니다.');
    } finally {
      setIsGenerating(false);
    }
  };

  /** 선택 버튼 */
  const handleSelectImage = () => {
    if (generatedImage) {
      navigation.navigate('TicketComplete', {
        ticketData,
        reviewData,
        images: [generatedImage],
      });
    }
  };

  /** 히스토리 이미지 선택 */
  const handleSelectFromHistory = (imageUrl: string) => {
    setGeneratedImage(imageUrl);
  };

  return (
    <SafeAreaView style={styles.container} edges={['left', 'right']}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.backButton} onPress={() => navigation.goBack()}>
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>티켓 이미지 생성</Text>

        {generatedImage && (
          <TouchableOpacity style={styles.nextButton} onPress={handleSelectImage}>
            <Text style={styles.nextButtonText}>다음</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* 로딩 화면 */}
      {isGenerating ? (
        <View style={styles.loadingFullScreen}>
          <ActivityIndicator size="large" color="#b11515" />
          <Text style={styles.generatingTitle}>AI 이미지 생성 중...</Text>
        </View>
      ) : (
        <ScrollView style={styles.content}>
          {generatedImage && (
            <>
              {/* 메시지 */}
              <View style={styles.successMessageContainer}>
                <Text style={styles.successMessage}>이미지가 생성되었어요!</Text>
              </View>

              {/* 생성 이미지 */}
              <View style={styles.generatedImageContainer}>
                <Image
                  source={{ uri: generatedImage }}
                  style={styles.generatedImage}
                  resizeMode="cover"
                />
              </View>

              {/* 재생성 UI */}
              <View style={styles.regenerationSection}>
                <Text style={styles.regenerationTitle}>이렇게 바꿔주세요</Text>

                <View style={styles.hintBubble}>
                  <Text style={styles.hintText}>
                    생성된 티켓이 마음에 들지 않나요?{'\n'}
                    원하는 스타일을 알려주세요!
                  </Text>
                </View>

                <TextInput
                  style={styles.regenerationInput}
                  placeholder="요구사항을 입력하세요..."
                  placeholderTextColor={Colors.tertiaryLabel}
                  value={regenerationRequest}
                  onChangeText={setRegenerationRequest}
                  multiline
                  textAlignVertical="top"
                />

                <TouchableOpacity
                  style={[styles.regenerateButton, isGenerating && styles.regenerateButtonDisabled]}
                  disabled={isGenerating}
                  onPress={handleRegenerateImage}
                >
                  <Text style={styles.regenerateButtonText}>다시 생성하기</Text>
                </TouchableOpacity>
              </View>
            </>
          )}

          {/* 히스토리 */}
          {generationHistory.length > 1 && (
            <View style={styles.sectionContainer}>
              <Text style={styles.sectionTitle}>생성 히스토리</Text>

              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.historyContainer}>
                {generationHistory.slice(1).map((imageUrl, index) => (
                  <TouchableOpacity
                    key={index}
                    style={styles.historyImageWrapper}
                    onPress={() => handleSelectFromHistory(imageUrl)}
                  >
                    <Image
                      source={{ uri: imageUrl }}
                      style={[
                        styles.historyImage,
                        generatedImage === imageUrl && styles.selectedHistoryImage,
                      ]}
                    />
                    {generatedImage === imageUrl && (
                      <View style={styles.selectedOverlay}>
                        <Text style={styles.selectedText}>✓</Text>
                      </View>
                    )}
                  </TouchableOpacity>
                ))}
              </ScrollView>
            </View>
          )}
        </ScrollView>
      )}
    </SafeAreaView>
  );
};

// === 아래는 UI 스타일 — 절대 수정 없음 ===
const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F8F9FA' },
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
  nextButton: { paddingHorizontal: Spacing.md, paddingVertical: Spacing.sm },
  nextButtonText: { ...Typography.callout, color: '#b11515', fontWeight: '600' },

  content: { flex: 1 },

  loadingFullScreen: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F8F9FA',
  },
  generatingTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2C3E50',
    marginTop: 8,
  },

  successMessageContainer: {
    marginHorizontal: Spacing.xl,
    marginTop: Spacing.xl,
    marginBottom: Spacing.md,
    alignItems: 'center',
  },
  successMessage: { ...Typography.title2, fontWeight: '600', color: Colors.label },

  generatedImageContainer: {
    marginHorizontal: Spacing.xl,
    marginTop: Spacing.lg,
    alignItems: 'center',
  },

  generatedImage: {
    width: cardWidth,
    height: cardHeight,
    borderRadius: BorderRadius.lg,
    backgroundColor: Colors.systemGray5,
  },

  regenerationSection: {
    marginHorizontal: Spacing.xl,
    marginTop: Spacing.xxxl,
    marginBottom: Spacing.xxxl,
  },
  regenerationTitle: {
    ...Typography.title3,
    fontWeight: '600',
    color: Colors.label,
    marginBottom: Spacing.md,
  },

  hintBubble: {
    backgroundColor: '#FFF5F5',
    borderRadius: BorderRadius.md,
    padding: Spacing.md,
    marginBottom: Spacing.md,
    borderWidth: 1,
    borderColor: '#FFE5E5',
  },
  hintText: { ...Typography.caption1, color: '#8B4513', lineHeight: 18 },

  regenerationInput: {
    backgroundColor: Colors.systemBackground,
    borderWidth: 1,
    borderColor: Colors.systemGray5,
    borderRadius: BorderRadius.md,
    padding: Spacing.md,
    minHeight: 100,
    ...Typography.body,
    color: Colors.label,
    marginBottom: Spacing.lg,
    ...Shadows.small,
  },

  regenerateButton: {
    backgroundColor: '#FF6B6B',
    borderRadius: BorderRadius.lg,
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.xl,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadows.button,
  },
  regenerateButtonDisabled: { opacity: 0.6 },

  regenerateButtonText: {
    ...Typography.headline,
    color: Colors.white,
    fontWeight: '600',
  },

  sectionContainer: {
    backgroundColor: '#FFFFFF',
    marginHorizontal: 20,
    marginTop: 20,
    marginBottom: 20,
    padding: 20,
    borderRadius: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#2C3E50',
    marginBottom: 12,
  },
  historyContainer: { marginTop: 12 },
  historyImageWrapper: { position: 'relative', marginRight: 12 },
  historyImage: {
    width: 80,
    height: 100,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  selectedHistoryImage: { borderColor: '#b11515' },
  selectedOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  selectedText: { fontSize: 24, color: '#b11515', fontWeight: 'bold' },
});

export default AIImageResults;
