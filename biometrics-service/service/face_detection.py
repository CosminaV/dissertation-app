from deepface import DeepFace
import numpy as np
import logging

logger = logging.getLogger(__name__)

def detect_face(image: np.ndarray):
    """
    Uses DeepFace to detect and align a single face.
    Returns:
        - aligned_face if exactly one face is found.
        - Raises ValueError if no face or multiple faces found.
    """
    try:
        faces = DeepFace.extract_faces(
            img_path=image,
            detector_backend="opencv",
            enforce_detection=True
        )

        if len(faces) > 1:
            raise ValueError("Multiple faces detected. Please use an image with only one face.")

        # Return the aligned and resized face
        aligned_face = faces[0]["face"]
        return aligned_face

    except Exception as e:
        logger.error(f"Face detection error: {str(e)}")
        raise ValueError(str(e))