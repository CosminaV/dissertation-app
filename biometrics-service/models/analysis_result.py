from pydantic import BaseModel
from typing import Dict, Optional

class AnalysisResult(BaseModel):
    age: Optional[int]
    gender: Optional[str]
    dominant_emotion: Optional[str]
    emotion_scores: Optional[Dict[str, float]]