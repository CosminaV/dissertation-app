from fastapi import APIRouter, File, UploadFile, HTTPException, Request
from utils.image_utils import download_image_from_url, fetch_profile_image_url
from deepface import DeepFace
import numpy as np
import cv2
from PIL import Image
from io import BytesIO
import requests
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

@router.post("/verify-face")
async def verify_face(request: Request, file: UploadFile = File(...)):
    auth_header = request.headers.get("Authorization")
    if not auth_header:
        raise HTTPException(status_code=401, detail="Token expired or invalid")

    try:
        image_url = fetch_profile_image_url(auth_header)
        logger.info(f"Fetched profile image URL: {image_url}")
    except requests.RequestException as e:
        logger.exception("Failed to fetch profile image URL")
        raise HTTPException(status_code=500, detail=f"Could not get profile image URL: {str(e)}")

    try:
        # Read uploaded image
        uploaded_bytes = await file.read()
        uploaded_image = Image.open(BytesIO(uploaded_bytes)).convert("RGB")
        img2_np = cv2.cvtColor(np.array(uploaded_image), cv2.COLOR_RGB2BGR)

        # Download reference image
        img1_np = download_image_from_url(image_url)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Image handling error: {str(e)}")

    try:
        result = DeepFace.verify(
            img1_path=img1_np,
            img2_path=img2_np,
            detector_backend="opencv",
            model_name="ArcFace"
        )
        return {
            "verified": result.get("verified", False),
            "distance": result.get("distance"),
            "similarity_score": round((1 - result.get("distance", 1)) * 100, 2)
        }
    except Exception as e:
        logger.exception("Face verification failed")
        raise HTTPException(status_code=500, detail=f"Face verification failed: {str(e)}")