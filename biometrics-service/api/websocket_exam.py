from fastapi import APIRouter, WebSocket, WebSocketDisconnect
import logging
import base64
import json
import numpy as np
import time
from PIL import Image
from io import BytesIO
from service.head_pose_estimation import get_head_direction
from service.eye_gaze_tracking import get_eye_direction

router = APIRouter()
logger = logging.getLogger(__name__)

@router.websocket("/ws/exam")
async def exam_websocket(websocket: WebSocket):
    await websocket.accept()
    logger.info("WebSocket connection accepted.")

    # calibration for head pose
    calibrated_angles = None
    start_time = time.time()
    calibration_data = []

    try:
        while True:
            data = await websocket.receive_text()
            #logger.info(f"Received WebSocket message: {data}")

            payload = json.loads(data)
            base64_image = payload.get("image")
            token = payload.get("token")

            if not base64_image or not token:
                await websocket.send_json({"message": "Missing image or token"})
                continue

            try:
                header, encoded = base64_image.split(",", 1)
                img_bytes = base64.b64decode(encoded)
                pil_image = Image.open(BytesIO(img_bytes))
                if pil_image.mode != "RGB":
                    pil_image = pil_image.convert("RGB")
                frame = np.array(pil_image)

                current_time = time.time()

                if calibrated_angles is None and current_time - start_time <= 5:
                    angles = get_head_direction(frame, None)
                    if isinstance(angles, tuple):
                        calibration_data.append(angles)
                    await websocket.send_json({"message": "Calibrating... Please look straight"})
                    continue

                elif calibrated_angles is None and calibration_data:
                    pitches, yaws, rolls = zip(*calibration_data)
                    calibrated_angles = (
                        sum(pitches) / len(pitches),
                        sum(yaws) / len(yaws),
                        sum(rolls) / len(rolls),
                    )
                    await websocket.send_json({"message": "Calibration complete"})

                else:
                    head_direction = get_head_direction(frame, calibrated_angles)
                    eye_direction = get_eye_direction(frame)
                    await websocket.send_json({
                        "head_direction": head_direction,
                        "eye_direction": eye_direction,
                    })

            except Exception as e:
                await websocket.send_json({"message": f"Image decoding failed: {str(e)}"})

    except WebSocketDisconnect:
        logger.info("WebSocket disconnected.")