from fastapi import APIRouter, HTTPException, Request, Depends
import requests

from service.face_detection import detect_face
from service.face_analysis import analyze_face
from utils.image_utils import download_image_from_url, fetch_profile_image_url
from models.analysis_result import AnalysisResult
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

@router.get("/analyze-profile-image", response_model=AnalysisResult)
async def analyze_profile_image(request: Request):
    # Step 1: Forward Authorization header to Spring Boot
    auth_header = request.headers.get("Authorization")
    if not auth_header:
        raise HTTPException(status_code=401, detail="Token expired or invalid")

    try:
        image_url = fetch_profile_image_url(auth_header)
        logger.info(f"Fetched profile image URL: {image_url}")
    except requests.RequestException as e:
        logger.exception("Failed to fetch profile image URL")
        raise HTTPException(status_code=500, detail=f"Could not get profile image URL: {str(e)}")

    # Step 2: Download image
    try:
        image = download_image_from_url(image_url)
        logger.info("Image downloaded successfully")
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Failed to download image: {str(e)}")

    # Step 3: Run face detection
    try:
        logger.info("Running face detection")
        aligned_face = detect_face(image)
    except ValueError as ve:
        raise HTTPException(status_code=422, detail=f"Failed to detect face: {str(ve)}")

    # Step 4: Run analysis
    try:
        logger.info("Analyzing face")
        analysis_result = analyze_face(aligned_face)
        return analysis_result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Face analysis failed: {str(e)}")