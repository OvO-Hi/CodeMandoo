declare module 'react-native-document-picker' {
  export type DocumentPickerResponse = {
    uri?: string | null;
    fileCopyUri?: string | null;
    name?: string | null;
    type?: string | null;
    size?: number | null;
    [key: string]: any;
  };

  export const types: {
    allFiles: string;
    audio: string;
    images: string;
    plainText: string;
    pdf: string;
    video: string;
    [key: string]: string;
  };

  export default class DocumentPicker {
    static pick(options?: any): Promise<DocumentPickerResponse[]>;
    static pickSingle(options?: any): Promise<DocumentPickerResponse>;
    static isCancel(err: unknown): boolean;
  }
}

