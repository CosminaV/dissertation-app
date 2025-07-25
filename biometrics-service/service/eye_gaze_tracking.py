import cv2
import dlib
import numpy as np

# Load dlib’s face detector and 68 landmarks model
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor("model/shape_predictor_68_face_landmarks.dat")

def detect_pupil(eye_region):
    gray_eye = cv2.cvtColor(eye_region, cv2.COLOR_BGR2GRAY)
    blurred_eye = cv2.GaussianBlur(gray_eye, (7, 7), 0)
    _, threshold_eye = cv2.threshold(blurred_eye, 50, 255, cv2.THRESH_BINARY_INV)
    contours, _ = cv2.findContours(threshold_eye, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if contours:
        pupil_contour = max(contours, key=cv2.contourArea)
        px, py, pw, ph = cv2.boundingRect(pupil_contour)
        return (px + pw // 2, py + ph // 2)
    return None


def get_eye_direction(frame: np.ndarray) -> str:
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = detector(gray)

    if not faces:
        return "Face not aligned to detect the eyes properly"

    for face in faces:
        landmarks = predictor(gray, face)

        # Extract left and right eye landmarks
        left_eye_points = np.array([(landmarks.part(n).x, landmarks.part(n).y) for n in range(36, 42)])
        right_eye_points = np.array([(landmarks.part(n).x, landmarks.part(n).y) for n in range(42, 48)])

        # Get bounding rectangles for the eyes
        left_eye_rect = cv2.boundingRect(left_eye_points)
        right_eye_rect = cv2.boundingRect(right_eye_points)

        # Extract eye regions
        left_eye = frame[left_eye_rect[1]:left_eye_rect[1] + left_eye_rect[3],
                   left_eye_rect[0]:left_eye_rect[0] + left_eye_rect[2]]
        right_eye = frame[right_eye_rect[1]:right_eye_rect[1] + right_eye_rect[3],
                    right_eye_rect[0]:right_eye_rect[0] + right_eye_rect[2]]

        # Detect pupils
        left_pupil = detect_pupil(left_eye)
        right_pupil = detect_pupil(right_eye)

        if left_pupil and right_pupil:
            lx, ly = left_pupil
            rx, ry = right_pupil

            eye_width = left_eye_rect[2]
            eye_height = left_eye_rect[3]
            norm_ly, norm_ry = ly / eye_height, ry / eye_height

            if lx < eye_width // 3 and rx < eye_width // 3:
                return "Looking Right"
            elif lx > 2 * eye_width // 3 and rx > 2 * eye_width // 3:
                return "Looking Left"
            elif norm_ly < 0.3 and norm_ry < 0.3:
                return "Looking Up"
            elif norm_ly > 0.45 and norm_ry > 0.45:
                return "Looking Down"
            else:
                return "Looking Center"

    return "Face not aligned to detect the eyes properly"