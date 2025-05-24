from deepface import DeepFace
import numpy as np
from models.analysis_result import AnalysisResult


def analyze_face(aligned_face: np.ndarray) -> AnalysisResult:
    """
    Analyzes the aligned face and returns age, gender, and emotion.
    """
    try:
        results = DeepFace.analyze(
            img_path=aligned_face,
            actions=["age", "gender", "emotion"],
            detector_backend="skip",  # already detected/aligned the face in face_detection.py
            enforce_detection=False
        )

        result = results[0]
        gender = result.get("gender")
        if isinstance(gender, dict):
            gender = max(gender, key=gender.get)  # pick the most likely one

        return AnalysisResult(
            age=result.get("age"),
            gender=gender,
            dominant_emotion=result.get("dominant_emotion"),
            emotion_scores=result.get("emotion")
        )

    except Exception as e:
        raise ValueError(f"Face results failed: {str(e)}")