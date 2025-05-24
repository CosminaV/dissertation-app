import math
from typing import Any

import cv2
import mediapipe as mp
import numpy as np

mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh(min_detection_confidence=0.5,
                                  min_tracking_confidence=0.5)

# 3D model coordinates (real-world reference points)
face_coordination_points = np.array([
    [285, 528, 200],
    [285, 371, 152],
    [197, 574, 128],
    [173, 425, 108],
    [360, 574, 128],
    [391, 425, 108]
], dtype=np.float64)

# Corresponding landmark indices from MediaPipe
landmark_ids = [1, 9, 57, 130, 287, 359]

def rotation_matrix_to_angles(rotation_matrix):
    x = math.atan2(rotation_matrix[2, 1], rotation_matrix[2, 2])
    y = math.atan2(-rotation_matrix[2, 0], math.sqrt(rotation_matrix[0, 0]*2 + rotation_matrix[1, 0]*2))
    z = math.atan2(rotation_matrix[1, 0], rotation_matrix[0, 0])
    return np.array([x, y, z]) * 180. / math.pi


def get_head_direction(frame: np.ndarray, calibrated_angles=None) -> str | tuple[Any, Any, Any]:
    img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = face_mesh.process(img_rgb)

    if not results.multi_face_landmarks:
        return "No face detected"

    h, w, _ = frame.shape
    face_landmarks = results.multi_face_landmarks[0]

    image_points = []
    for idx in landmark_ids:
        lm = face_landmarks.landmark[idx]
        x, y = int(lm.x * w), int(lm.y * h)
        image_points.append([x, y])

    image_points = np.array(image_points, dtype=np.float64)

    cam_matrix = np.array([
        [w, 0, w / 2],
        [0, w, h / 2],
        [0, 0, 1]
    ], dtype=np.float64)
    dist_matrix = np.zeros((4, 1), dtype=np.float64)

    success, rotation_vec, _ = cv2.solvePnP(face_coordination_points, image_points, cam_matrix, dist_matrix)
    if not success:
        return "Pose estimation failed"

    rotation_matrix, _ = cv2.Rodrigues(rotation_vec)
    pitch, yaw, roll = rotation_matrix_to_angles(rotation_matrix)

    if calibrated_angles is None:
        # Return angles so you can calibrate
        return pitch, yaw, roll

    pitch_offset, yaw_offset, roll_offset = calibrated_angles
    PITCH_THRESHOLD = 8
    YAW_THRESHOLD = 12
    ROLL_THRESHOLD = 5

    if abs(yaw - yaw_offset) <= YAW_THRESHOLD and abs(pitch - pitch_offset) <= PITCH_THRESHOLD and abs(roll - roll_offset) <= ROLL_THRESHOLD:
        return "Looking at Screen"
    elif yaw < yaw_offset - 15:
        return "Looking Right"
    elif yaw > yaw_offset + 15:
        return "Looking Left"
    elif pitch > pitch_offset + 10:
        return "Looking Up"
    elif pitch < pitch_offset - 10:
        return "Looking Down"
    elif abs(roll - roll_offset) > 7:
        return "Tilted"
    else:
        return "Looking at Screen"

# def get_head_direction(frame: np.ndarray) -> str:
#     img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
#     results = face_mesh.process(img_rgb)
#
#     if not results.multi_face_landmarks:
#         return "No face detected"
#
#     h, w, _ = frame.shape
#     face_landmarks = results.multi_face_landmarks[0]
#
#     image_points = []
#     for idx in landmark_ids:
#         lm = face_landmarks.landmark[idx]
#         x, y = int(lm.x * w), int(lm.y * h)
#         image_points.append([x, y])
#
#     image_points = np.array(image_points, dtype=np.float64)
#
#     cam_matrix = np.array([
#         [w, 0, w / 2],
#         [0, w, h / 2],
#         [0, 0, 1]
#     ], dtype=np.float64)
#     dist_matrix = np.zeros((4, 1), dtype=np.float64)
#
#     success, rotation_vec, _ = cv2.solvePnP(
#         model_points, image_points, cam_matrix, dist_matrix)
#
#     if not success:
#         return "Pose estimation failed"
#
#     rotation_matrix, _ = cv2.Rodrigues(rotation_vec)
#     pitch, yaw, roll = rotation_matrix_to_angles(rotation_matrix)
#
#     # Determine direction
#     if abs(yaw) < 20 and abs(pitch) < 15:
#         return "Looking at Screen"
#     elif yaw <= -20:
#         return "Looking Right"
#     elif yaw >= 20:
#         return "Looking Left"
#     elif pitch >= 15:
#         return "Looking Up"
#     elif pitch <= -15:
#         return "Looking Down"
#     else:
#         return "Looking at Screen"