import api from './api';

// 카드 감정 결과 타입
export interface CardEmotionResult {
  steps: {
    file_ext_check: string;
    size_brightness_check: string;
    card_verify: {
      front: {
        target: string;
        best_conf: number;
        best_area_frac: number;
      };
      back: {
        target: string;
        best_conf: number;
        best_area_frac: number;
      };
    };
    bending: {
      curvatures_percent: {
        image_side_1: number;
        image_side_2: number;
        image_side_3: number;
        image_side_4: number;
      };
      per_image_penalties: {
        image_side_1: number;
        image_side_2: number;
        image_side_3: number;
        image_side_4: number;
      };
      bend_penalty_total: number;
    };
    other_defects: {
      front: {
        detections: any[];
      };
      back: {
        detections: any[];
      };
      other_penalties_total: number;
    };
  };
  score: number;
  grade: string;
}

// 카드 감정하기 API 호출
export const analyzeCardEmotion = async (files: File[]): Promise<CardEmotionResult> => {
  const formData = new FormData();

  // 파일들을 FormData에 추가
  files.forEach((file, index) => {
    formData.append(`file_${index}`, file);
  });

  try {
    const response = await api.post('/v1/card/emotion', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  } catch (error) {
    console.error('카드 감정 분석 실패:', error);
    throw error;
  }
};

// 카드 감정 결과를 기반으로 등급 해시 생성 (임시)
export const generateGradeHash = (result: CardEmotionResult): string => {
  // 실제로는 서버에서 제공하는 해시를 사용해야 하지만, 
  // 현재는 점수와 등급을 기반으로 임시 해시 생성
  return `grade_${result.score}_${result.grade}_${Date.now()}`;
};
