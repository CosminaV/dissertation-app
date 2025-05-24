import requests
import numpy as np
import cv2
from fastapi import HTTPException
from PIL import Image
from io import BytesIO
import logging
from config import PROFILE_IMAGE_PRESIGNEDURL_URL

logger = logging.getLogger(__name__)

def fetch_profile_image_url(auth_header: str) -> str:
    response = requests.get(
        PROFILE_IMAGE_PRESIGNEDURL_URL,
        headers={"Authorization": auth_header},
        verify=False
    )
    if response.status_code != 200:
        raise HTTPException(status_code=response.status_code, detail=response.text)

    profile_image_url = response.text.strip()
    return profile_image_url

def download_image_from_url(image_url: str) -> np.ndarray:
    try:
        response = requests.get(image_url)
        response.raise_for_status()

        image_bytes = BytesIO(response.content)
        pil_image = Image.open(image_bytes).convert("RGB")

        # Convert PIL image to OpenCV image
        cv_image = cv2.cvtColor(np.array(pil_image), cv2.COLOR_RGB2BGR)
        return cv_image

    except requests.exceptions.RequestException as e:
        raise RuntimeError(f"Failed to download image: {str(e)}")

    except Exception as e:
        raise RuntimeError(f"Failed to process image: {str(e)}")