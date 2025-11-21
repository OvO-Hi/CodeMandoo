import { Platform } from 'react-native';

class VoiceManager {
  private isAvailable: boolean | null = null;
  private isInitialized: boolean = false;
  private Voice: any = null;
  private isCheckingAvailability: boolean = false;

  constructor() {
    // Don't initialize immediately to avoid NativeEventEmitter errors
  }

  private async checkAvailability() {
    if (this.isAvailable !== null) {
      return this.isAvailable;
    }

    if (this.isCheckingAvailability) {
      // Wait for ongoing check to complete
      while (this.isCheckingAvailability) {
        await new Promise<void>(resolve => setTimeout(resolve, 50));
      }
      return this.isAvailable;
    }

    this.isCheckingAvailability = true;

    try {
      // Only proceed if we're on a supported platform
      if (Platform.OS === 'web') {
        this.isAvailable = false;
        return false;
      }

      // For now, disable Voice module completely to avoid NativeEventEmitter errors
      // This is a temporary fix until the native module issue is resolved
      console.warn('Voice module temporarily disabled to prevent NativeEventEmitter errors');
      this.isAvailable = false;
      
      // TODO: Re-enable when native module is properly configured
      /*
      // Wrap the require in a timeout to prevent hanging
      const loadVoiceModule = () => {
        return new Promise((resolve, reject) => {
          const timeout = setTimeout(() => {
            reject(new Error('Voice module loading timeout'));
          }, 3000);

          try {
            const VoiceModule = require('@react-native-voice/voice');
            clearTimeout(timeout);
            resolve(VoiceModule.default || VoiceModule);
          } catch (error) {
            clearTimeout(timeout);
            reject(error);
          }
        });
      };

      this.Voice = await loadVoiceModule();
      
      // Minimal check without calling native methods that might trigger NativeEventEmitter
      if (this.Voice && typeof this.Voice.start === 'function') {
        this.isAvailable = true;
      } else {
        this.isAvailable = false;
      }
      */
    } catch (error) {
      console.warn('Voice module is not available or not properly linked:', error);
      this.isAvailable = false;
    } finally {
      this.isCheckingAvailability = false;
    }

    return this.isAvailable;
  }

  async initialize() {
    await this.checkAvailability();
    
    if (this.isInitialized || !this.isAvailable) {
      return this.isAvailable;
    }

    try {
      // Ensure Voice is properly initialized
      if (this.Voice && this.Voice.destroy) {
        await this.Voice.destroy();
      }
      this.isInitialized = true;
      return true;
    } catch (error) {
      console.warn('Failed to initialize Voice module:', error);
      this.isAvailable = false;
      return false;
    }
  }

  async isVoiceAvailable(): Promise<boolean> {
    await this.checkAvailability();
    return this.isAvailable === true && Platform.OS !== 'web';
  }

  async startRecording(locale: string = 'ko-KR'): Promise<boolean> {
    if (!(await this.isVoiceAvailable())) {
      throw new Error('음성 인식 기능을 사용할 수 없습니다.');
    }

    try {
      await this.initialize();
      await this.Voice.start(locale);
      return true;
    } catch (error) {
      console.warn('Failed to start recording:', error);
      throw error;
    }
  }

  async stopRecording(): Promise<void> {
    if (!(await this.isVoiceAvailable())) {
      return;
    }

    try {
      await this.Voice.stop();
    } catch (error) {
      console.warn('Failed to stop recording:', error);
    }
  }

  async destroy(): Promise<void> {
    if (!(await this.isVoiceAvailable())) {
      return;
    }

    try {
      if (this.Voice && this.Voice.destroy && this.Voice.removeAllListeners) {
        await this.Voice.destroy();
        this.Voice.removeAllListeners();
      }
    } catch (error) {
      console.warn('Failed to destroy Voice module:', error);
    }
  }

  async setListeners(callbacks: {
    onSpeechResults?: (event: any) => void;
    onSpeechError?: (error: any) => void;
    onSpeechEnd?: () => void;
  }) {
    if (!(await this.isVoiceAvailable())) {
      return;
    }

    try {
      if (callbacks.onSpeechResults && this.Voice.onSpeechResults !== undefined) {
        this.Voice.onSpeechResults = callbacks.onSpeechResults;
      }
      if (callbacks.onSpeechError && this.Voice.onSpeechError !== undefined) {
        this.Voice.onSpeechError = callbacks.onSpeechError;
      }
      if (callbacks.onSpeechEnd && this.Voice.onSpeechEnd !== undefined) {
        this.Voice.onSpeechEnd = callbacks.onSpeechEnd;
      }
    } catch (error) {
      console.warn('Failed to set Voice listeners:', error);
    }
  }
}

// Create a singleton instance only when needed
let voiceManagerInstance: VoiceManager | null = null;

export const getVoiceManager = (): VoiceManager => {
  if (!voiceManagerInstance) {
    voiceManagerInstance = new VoiceManager();
  }
  return voiceManagerInstance;
};

// For backward compatibility
export const voiceManager = {
  async isVoiceAvailable(): Promise<boolean> {
    try {
      return await getVoiceManager().isVoiceAvailable();
    } catch (error) {
      console.warn('Voice availability check failed:', error);
      return false;
    }
  },
  
  async startRecording(locale: string = 'ko-KR'): Promise<boolean> {
    try {
      return await getVoiceManager().startRecording(locale);
    } catch (error) {
      console.warn('Start recording failed:', error);
      throw error;
    }
  },
  
  async stopRecording(): Promise<void> {
    try {
      await getVoiceManager().stopRecording();
    } catch (error) {
      console.warn('Stop recording failed:', error);
    }
  },
  
  async setListeners(callbacks: {
    onSpeechResults?: (event: any) => void;
    onSpeechError?: (error: any) => void;
    onSpeechEnd?: () => void;
  }): Promise<void> {
    try {
      await getVoiceManager().setListeners(callbacks);
    } catch (error) {
      console.warn('Set listeners failed:', error);
    }
  },
  
  async destroy(): Promise<void> {
    try {
      await getVoiceManager().destroy();
    } catch (error) {
      console.warn('Destroy failed:', error);
    }
  }
};
