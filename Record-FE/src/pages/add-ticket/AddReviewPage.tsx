import React, { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Switch,
  Platform,
  Alert,
  Animated,
  Dimensions,
  PanResponder,
  Image,
  KeyboardAvoidingView,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Colors, Typography, Spacing, Shadows, BorderRadius } from '../../styles/designSystem';
import { sttService } from '../../services/api/sttService';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../types/reviewTypes';
import ReviewSummaryModal from '../../components/ReviewSummaryModal';
import { apiClient } from '../../services/api/client';
import DocumentPicker, { types as DocumentPickerTypes, DocumentPickerResponse } from 'react-native-document-picker';

type AddReviewPageProps = NativeStackScreenProps<RootStackParamList, 'AddReview'>;

const { width } = Dimensions.get('window');

const AddReviewPage = ({ navigation, route }: AddReviewPageProps) => {
  const { ticketData } = route.params;

  const [reviewText, setReviewText] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [showSummaryModal, setShowSummaryModal] = useState(false);
  const [summaryText, setSummaryText] = useState(''); // 요약된 텍스트를 저장할 state
  const [isProcessingSTT, setIsProcessingSTT] = useState(false);
  const [selectedAudio, setSelectedAudio] = useState<DocumentPickerResponse | null>(null);
  const [questions, setQuestions] = useState<string[]>([
    '이 공연을 보게 된 계기는?',
    '가장 인상 깊었던 순간은?',
    '다시 본다면 어떤 점이 기대되나요?',
  ]);
  const [isLoadingQuestions, setIsLoadingQuestions] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isCardVisible, setIsCardVisible] = useState(true);

  const scrollX = useRef(new Animated.Value(0)).current;
  const pan = useRef(new Animated.ValueXY()).current;
  const opacity = useRef(new Animated.Value(1)).current;
  const cardScale = useRef(new Animated.Value(1)).current;
  const cardHeight = useRef(new Animated.Value(1)).current;
  const reviewTranslateY = useRef(new Animated.Value(0)).current;
  const currentIndexRef = useRef(0);

  useEffect(() => {
    currentIndexRef.current = currentIndex;
  }, [currentIndex]);

  // 후기 작성 화면 진입 시 질문 가져오기
  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        setIsLoadingQuestions(true);
        
        // 장르 매핑 (프론트엔드 → 백엔드)
        // 백엔드의 mapGenre 메서드가 "밴드", "연극/뮤지컬" 등을 받아서 "band", "musical", "common"으로 매핑합니다.
        const mapGenreForBackend = (frontendGenre: string): string => {
          if (!frontendGenre) {
            console.warn('장르가 없어서 COMMON으로 설정');
            return 'COMMON';
          }
          const genre = frontendGenre.trim();
          if (genre.includes('밴드') || genre === '밴드') {
            return '밴드';  // 백엔드에서 "band"로 매핑됨
          } else if (genre.includes('뮤지컬') || genre.includes('연극')) {
            return '연극/뮤지컬';  // 백엔드에서 "musical"로 매핑됨
          }
          return 'COMMON';  // 백엔드에서 "common"으로 매핑됨
        };

        const genre = mapGenreForBackend(ticketData.genre || '');
        console.log('=== 질문 가져오기 시작 ===');
        console.log('원본 장르:', ticketData.genre);
        console.log('매핑된 장르:', genre);
        console.log('API 요청 URL:', `/review-questions?genre=${encodeURIComponent(genre)}`);
        
        const result = await apiClient.get<string[]>(`/review-questions?genre=${encodeURIComponent(genre)}`);
        
        console.log('API 응답 전체:', JSON.stringify(result, null, 2));
        console.log('응답 success:', result.success);
        console.log('응답 data:', result.data);
        console.log('응답 data 길이:', result.data?.length);
        
        if (result.success && result.data && result.data.length > 0) {
          console.log('✅ 질문 가져오기 성공! 가져온 질문:', result.data);
          setQuestions(result.data);
        } else {
          // API 호출 실패 또는 빈 리스트 시 기본 질문 사용
          console.warn('⚠️ 질문 가져오기 실패 또는 빈 리스트');
          console.warn('응답 상세:', {
            success: result.success,
            data: result.data,
            dataLength: result.data?.length,
            error: result.error,
          });
          console.warn('기본 질문 사용');
        }
      } catch (error) {
        console.error('❌ 질문 가져오기 오류:', error);
        console.error('오류 상세:', error instanceof Error ? error.message : String(error));
        // 오류 발생 시 기본 질문 사용
      } finally {
        setIsLoadingQuestions(false);
        console.log('=== 질문 가져오기 완료 ===');
      }
    };

    fetchQuestions();
  }, [ticketData.genre]);

  const resetCardPosition = () => {
    Animated.parallel([
      Animated.spring(pan, {
        toValue: { x: 0, y: 0 },
        useNativeDriver: false,
        tension: 100,
        friction: 8,
      }),
      Animated.spring(opacity, { toValue: 1, useNativeDriver: false }),
    ]).start();
  };

  const createBounceEffect = (direction: 'left' | 'right') => {
    const bounceDistance = direction === 'left' ? -30 : 30;
    Animated.sequence([
      Animated.timing(pan, {
        toValue: { x: bounceDistance, y: 0 },
        duration: 150,
        useNativeDriver: false,
      }),
      Animated.spring(pan, {
        toValue: { x: 0, y: 0 },
        tension: 300,
        friction: 8,
        useNativeDriver: false,
      }),
    ]).start();
  };

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: (_, gestureState) =>
        Math.abs(gestureState.dx) > 10,
      onPanResponderMove: (_, gestureState) =>
        pan.setValue({ x: gestureState.dx, y: 0 }),
      onPanResponderRelease: (_, gestureState) => {
        const swipeThreshold = 80;
        const velocityThreshold = 0.3;
        const totalCards = questions.length;
        const currentIdx = currentIndexRef.current;

        const shouldSwipeRight =
          gestureState.dx > swipeThreshold ||
          (gestureState.dx > 30 && gestureState.vx > velocityThreshold);
        const shouldSwipeLeft =
          gestureState.dx < -swipeThreshold ||
          (gestureState.dx < -30 && gestureState.vx < -velocityThreshold);

        if (shouldSwipeRight) {
          if (currentIdx === 0) createBounceEffect('left');
          else {
            const newIndex = currentIdx - 1;
            setCurrentIndex(newIndex);
            Animated.timing(scrollX, {
              toValue: newIndex * width,
              duration: 200,
              useNativeDriver: false,
            }).start();
            resetCardPosition();
          }
        } else if (shouldSwipeLeft) {
          if (currentIdx === totalCards - 1) createBounceEffect('right');
          else {
            const newIndex = currentIdx + 1;
            setCurrentIndex(newIndex);
            Animated.timing(scrollX, {
              toValue: newIndex * width,
              duration: 200,
              useNativeDriver: false,
            }).start();
            resetCardPosition();
          }
        } else resetCardPosition();
      },
    }),
  ).current;

  const handleSelectAudioFile = async () => {
    try {
      const file = await DocumentPicker.pickSingle({
        type: [DocumentPickerTypes.audio],
        presentationStyle: 'fullScreen',
        copyTo: 'documentDirectory',
      });

      const uri = file.fileCopyUri || file.uri;
      if (!uri) {
        Alert.alert('오류', '선택한 파일의 경로를 확인할 수 없습니다.');
        return;
      }

      setSelectedAudio({
        ...file,
        uri,
      });
      Alert.alert('선택 완료', `${file.name || '오디오 파일'}을 선택했습니다.`);
    } catch (err) {
      if (!DocumentPicker.isCancel(err)) {
        console.error('Audio pick error:', err);
        Alert.alert('오류', '오디오 파일을 선택하는 중 문제가 발생했습니다.');
      }
    }
  };

  /**
   * STT 변환 실행
   * 
   * 흐름:
   * 1. 음성 파일을 STT로 변환하여 DB에 저장
   * 2. 변환된 텍스트를 후기 작성 텍스트 창에 표시
   * 
   * 이유: 사용자가 "STT 변환 실행" 버튼을 누르면 음성만 텍스트로 변환하고,
   *      변환된 텍스트가 후기 작성 창에 표시됩니다. 요약은 별도의 "후기 요약하기" 버튼을 통해 실행됩니다.
   */
  const handleUploadAudioFile = async () => {
    if (!selectedAudio?.uri) {
      Alert.alert('알림', '먼저 업로드할 오디오 파일을 선택해주세요.');
      return;
    }

    try {
      setIsProcessingSTT(true);
      Alert.alert('처리중', 'STT 변환 중입니다...');

      // STT 변환 및 DB 저장
      const sttResult = await sttService.transcribeAndSave(
        selectedAudio.uri,
        selectedAudio.name || 'recording.m4a',
        selectedAudio.type || 'audio/m4a'
      );

      setIsProcessingSTT(false);

      if (sttResult.success && sttResult.data) {
        // STT 변환 성공: 변환된 텍스트를 reviewText에 추가
        // 백엔드 Transcription 엔티티는 resultText 필드를 사용하므로, 
        // transcript(하위 호환성)와 resultText 둘 다 확인합니다.
        const transcribedText = sttResult.data.resultText || sttResult.data.transcript || '';
        if (transcribedText) {
          setReviewText(prev => (prev ? `${prev}\n${transcribedText}` : transcribedText));
          Alert.alert('완료', 'STT 변환이 완료되었습니다.');
        } else {
          Alert.alert('알림', 'STT 변환은 완료되었지만 텍스트가 비어있습니다.');
        }
        // 선택된 오디오 파일 초기화 (다음 파일 선택을 위해)
        setSelectedAudio(null);
      } else {
        Alert.alert('오류', sttResult.error?.message || 'STT 변환에 실패했습니다.');
      }
    } catch (error: any) {
      console.error('STT upload error:', error);
      setIsProcessingSTT(false);
      Alert.alert('오류', 'STT 변환 중 오류가 발생했습니다.');
    }
  };

  const handleSubmit = () => {
    navigation.navigate('ImageOptions', {
      ticketData,
      reviewData: { reviewText },
    });
  };

  /**
   * 후기 요약하기
   * 
   * 흐름:
   * 1. 후기 작성 창의 텍스트를 요약 API에 전달
   * 2. 요약된 결과를 ReviewSummaryModal에 표시
   * 
   * 이유: 사용자가 "후기 요약하기" 버튼을 누르면 현재 작성된 후기 텍스트를
   *      OpenAI를 통해 요약하고, 요약 결과를 모달로 표시합니다.
   */
  const handleSummary = async () => {
    if (!reviewText || reviewText.trim().length === 0) {
      Alert.alert('알림', '요약할 후기 내용을 먼저 작성해주세요.');
      return;
    }

    try {
      Alert.alert('처리중', '후기를 요약하는 중입니다...');
      
      const result = await sttService.summarizeReview(reviewText);
      
      if (result.success && result.data) {
        // 요약 성공: 요약된 텍스트를 state에 저장하고 모달 표시
        setSummaryText(result.data);
        setShowSummaryModal(true);
      } else {
        Alert.alert('오류', result.error?.message || '요약 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('Summary error:', error);
      Alert.alert('오류', '요약 생성 중 오류가 발생했습니다.');
    }
  };

  const handleCloseCard = () => {
    Animated.parallel([
      Animated.timing(cardScale, {
        toValue: 0,
        duration: 300,
        useNativeDriver: false,
      }),
      Animated.timing(opacity, {
        toValue: 0,
        duration: 300,
        useNativeDriver: false,
      }),
      Animated.timing(cardHeight, {
        toValue: 0,
        duration: 300,
        useNativeDriver: false,
      }),
      Animated.timing(reviewTranslateY, {
        toValue: 44,
        duration: 300,
        useNativeDriver: false,
      }),
    ]).start(() => {
      setIsCardVisible(false);
    });
  };

  return (
    <SafeAreaView style={styles.container} edges={['left', 'right']}>
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>후기 작성하기</Text>
        <TouchableOpacity onPress={handleSubmit}>
          <Text style={styles.nextButtonText}>다음</Text>
        </TouchableOpacity>
      </View>

      {/* 질문 카드 스와이프 */}
      {isCardVisible && (
        <Animated.View
          style={[
            styles.questionSection,
            {
              height: cardHeight.interpolate({
                inputRange: [0, 1],
                outputRange: [0, 150],
              }),
              opacity,
            },
          ]}
        >
          {/* Animated 점 인디케이터 */}
          <View style={styles.dots}>
            {questions.map((_, i) => {
              const inputRange = [
                (i - 1) * width,
                i * width,
                (i + 1) * width,
              ];
              const dotWidth = scrollX.interpolate({
                inputRange,
                outputRange: [6, 12, 6],
                extrapolate: 'clamp',
              });
              const dotColor = scrollX.interpolate({
                inputRange,
                outputRange: ['#BDC3C7', '#2C3E50', '#BDC3C7'],
                extrapolate: 'clamp',
              });
              return (
                <Animated.View
                  key={i}
                  style={[
                    styles.dot,
                    { width: dotWidth, backgroundColor: dotColor },
                  ]}
                />
              );
            })}
          </View>

          {/* Animated 질문 카드 */}
          <Animated.View
            style={[
              styles.animatedCard,
              {
                transform: [
                  ...pan.getTranslateTransform(),
                  { scale: cardScale },
                ],
                opacity,
              },
            ]}
            {...panResponder.panHandlers}
          >
            <View style={styles.questionCard}>
              <View style={styles.questionHeaderRow}>
                {/* 이미지 */}
                <Image
                  source={require('../../assets/cat.png')}
                  style={styles.catImage}
                />

                {/* 오른쪽 텍스트 영역 */}
                <View style={styles.textContainer}>
                  <View style={styles.questionLabelRow}>
                    <Text style={styles.questionLabel}>
                      질문 {currentIndex + 1}
                    </Text>
                  </View>
                  <Text style={styles.questionText}>
                    {questions[currentIndex]}
                  </Text>
                </View>
                <TouchableOpacity
                  style={styles.closeButton}
                  onPress={handleCloseCard}
                >
                  <Text style={styles.closeButtonText}>✕</Text>
                </TouchableOpacity>
              </View>
            </View>
          </Animated.View>
        </Animated.View>
      )}

      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        {/* 후기 입력 영역 */}
        <Animated.View
          style={[
            styles.reviewContainer,
            { transform: [{ translateY: reviewTranslateY }] },
          ]}
        >
          <TextInput
            style={styles.reviewInput}
            placeholder="후기를 입력하세요..."
            placeholderTextColor="#BDC3C7"
            multiline
            numberOfLines={8}
            maxLength={1000}
            value={reviewText}
            onChangeText={setReviewText}
            textBreakStrategy="simple" // Android에서 텍스트 줄바꿈 전략 설정
          />

          {/* 후기 요약하기 버튼 */}
          <TouchableOpacity
            style={styles.reviewListButton}
            onPress={handleSummary}
          >
            <Text style={styles.reviewListButtonIcon}>🎫</Text>
            <Text style={styles.reviewListButtonText}>후기 요약하기</Text>
          </TouchableOpacity>
        </Animated.View>

        {/* 오디오 업로드 섹션 */}
        <View style={styles.audioSection}>
          <TouchableOpacity
            style={styles.audioButton}
            onPress={handleSelectAudioFile}
            disabled={isProcessingSTT}
          >
            <Text style={styles.audioButtonText}>🎵 오디오 파일 선택</Text>
          </TouchableOpacity>

          {selectedAudio?.name && (
            <Text style={styles.audioFileName}>선택된 파일: {selectedAudio.name}</Text>
          )}

          <TouchableOpacity
            style={[
              styles.audioUploadButton,
              (isProcessingSTT || !selectedAudio) && styles.audioUploadButtonDisabled,
            ]}
            onPress={handleUploadAudioFile}
            disabled={isProcessingSTT || !selectedAudio}
          >
            <Text style={styles.audioUploadButtonText}>
              {isProcessingSTT ? '⏳ 변환 중...' : 'STT 변환 실행'}
            </Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
        
      {/* 후기 요약 모달 */}
      <ReviewSummaryModal
        visible={showSummaryModal}
        onClose={() => setShowSummaryModal(false)}
        summaryText={summaryText || "이곳에 요약된 결과가 나옵니다."}
      />
    </SafeAreaView>
  );
};

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
  },
  backButtonText: {
    ...Typography.title3,
    color: Colors.label,
    fontWeight: '500',
  },
  headerTitle: { ...Typography.headline, color: Colors.label },
  nextButtonText: { ...Typography.body, color: '#B11515' },

  questionSection: {
    marginTop: 16,
    marginHorizontal: 20,
  },
  
  // 인디케이터
  dots: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  dot: { height: 6, borderRadius: 3, marginHorizontal: Spacing.xs },

  animatedCard: { width: '100%' },
  questionCard: {
    width: '100%',
    backgroundColor: '#eaeaea',
    borderRadius: 12,
    padding: 8,
    ...Shadows.small,
  },

  catImage: {
    width: 60,
    height: 50,
    margin: 12,
  },

  questionLabelRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  questionHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  textContainer: {
    flex: 1,
  },
  questionLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#000',
    marginBottom: 4,
  },

  // 닫기 버튼
  closeButton: {
    width: 24,
    height: 24,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'absolute',
    top: 8,
    right: 8,
  },
  closeButtonText: {
    fontSize: 18,
    color: '#000000ff',
  },
  questionText: {
    fontSize: 18,
    color: '#000',
    fontWeight: '500',
    lineHeight: 24,
  },

  // 후기 작성
  reviewContainer: {
    flex: 1,
    marginHorizontal: 20,
  },
  reviewInput: {
    borderRadius: 12,
    padding: 20,
    backgroundColor: '#eaeaea',
    minHeight: 450,
    textAlignVertical: 'top',
    fontSize: 16,
    color: '#000',
    marginTop: -20,
    // ⚠️ iOS에서 TextInput의 lineHeight 제한사항:
    // React Native의 TextInput에서 iOS에서는 lineHeight가 제대로 작동하지 않습니다.
    // 이는 React Native의 알려진 제한사항이며, iOS 네이티브 UITextView의 동작 방식 때문입니다.
    // 
    // 해결 방법:
    // 1. Android에서는 lineHeight가 작동하므로 설정합니다.
    // 2. iOS에서는 lineHeight가 작동하지 않으므로, 
    //    실제로 행간을 늘리려면 커스텀 네이티브 모듈을 사용하거나
    //    TextInput 대신 다른 컴포넌트를 사용해야 합니다.
    // 
    // 현재는 Android에서만 lineHeight를 적용하고,
    // iOS에서는 fontSize와 padding을 조정하여 시각적 효과를 냅니다.
    // 하지만 이것도 실제 행간을 늘리는 것은 아닙니다.
    ...(Platform.OS === 'android' && {
      lineHeight: 22, // fontSize 16의 약 1.375배로 행간을 약간 늘림
      includeFontPadding: false, // Android에서 폰트 패딩 제거하여 정확한 행간 적용
    }),
    // iOS에서는 lineHeight가 작동하지 않으므로,
    // fontSize를 약간 줄이고 paddingVertical을 조정하여 시각적 효과를 냅니다.
    // 하지만 이것도 실제 행간을 늘리는 것은 아닙니다.
    // iOS에서 실제 행간을 늘리려면 커스텀 네이티브 모듈이 필요합니다.
    ...(Platform.OS === 'ios' && {
      fontSize: 15.5, // 약간 줄여서 행간이 늘어난 것처럼 보이게
      paddingVertical: 22, // 상하 패딩을 약간 늘려 행간 효과
    }),
  },

  // 후기 요약하기 버튼
  reviewListButton: {
    marginTop: -60,
    alignSelf: 'center',
    width: 140,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 25,
    ...Shadows.medium,
  },

  reviewListButtonIcon: {
    fontSize: 18,
    marginRight: 6,
  },
  reviewListButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#000',
  },

  // 녹음 버튼
  audioSection: {
    paddingHorizontal: Spacing.sectionSpacing,
    paddingVertical: Spacing.lg,
    gap: Spacing.md,
  },
  audioButton: {
    backgroundColor: Colors.secondarySystemBackground,
    borderRadius: BorderRadius.lg,
    paddingVertical: Spacing.lg,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadows.small,
  },
  audioButtonText: {
    ...Typography.body,
    fontWeight: '600',
    color: Colors.label,
  },
  audioFileName: {
    ...Typography.footnote,
    color: Colors.secondaryLabel,
    textAlign: 'center',
  },
  audioUploadButton: {
    backgroundColor: Colors.primary,
    borderRadius: BorderRadius.lg,
    paddingVertical: Spacing.lg,
    alignItems: 'center',
    justifyContent: 'center',
    ...Shadows.button,
  },
  audioUploadButtonDisabled: {
    opacity: 0.5,
  },
  audioUploadButtonText: {
    ...Typography.headline,
    color: Colors.white,
    fontWeight: '600',
  },
});

export default AddReviewPage;
