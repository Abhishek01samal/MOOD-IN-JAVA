import cv2
import sys
import json
import os
import numpy as np
import mediapipe as mp
from mediapipe.tasks import python
from mediapipe.tasks.python import vision

# ── MOOD MAPPING ─────────────────────────────────────────────────────────────
# Based on MediaPipe's 52 Blendshapes for high accuracy
# Higher thresholds = more specific, Lower = more sensitive

def get_mood_from_blendshapes(blendshapes):
    # blendshapes is a list of Category objects (index, score, category_name)
    scores = {b.category_name: b.score for b in blendshapes}
    
    # 1. HAPPY (Smile)
    smile = (scores.get('mouthSmileLeft', 0) + scores.get('mouthSmileRight', 0)) / 2
    if smile > 0.35:
        return "happy"
        
    # 2. SURPRISED (Jaw open + Wide eyes)
    jaw_open = scores.get('jawOpen', 0)
    eye_wide = (scores.get('eyeWideLeft', 0) + scores.get('eyeWideRight', 0)) / 2
    if jaw_open > 0.4 and eye_wide > 0.3:
        return "surprised"
        
    # 3. ANGRY (Brow down + Pucker)
    brow_down = (scores.get('browDownLeft', 0) + scores.get('browDownRight', 0)) / 2
    if brow_down > 0.5:
        return "angry"
        
    # 4. SAD (Inner brow up + Lip shrug)
    brow_inner_up = scores.get('browInnerUp', 0)
    if brow_inner_up > 0.35:
        return "sad"
        
    # 5. DISGUSTED (Nose sneer)
    nose_sneer = (scores.get('noseSneerLeft', 0) + scores.get('noseSneerRight', 0)) / 2
    if nose_sneer > 0.35:
        return "disgusted"
        
    # 6. FEARFUL (Combined brow inner up + jaw + wide eyes)
    if brow_inner_up > 0.2 and eye_wide > 0.2 and jaw_open > 0.2:
        return "fearful"
        
    return "neutral"

def process_image(image_path):
    if not os.path.exists('face_landmarker.task'):
        return {"mood": "neutral", "error": "Model file missing"}

    # Initialize Face Landmarker
    base_options = python.BaseOptions(model_asset_path='face_landmarker.task')
    options = vision.FaceLandmarkerOptions(
        base_options=base_options,
        output_face_blendshapes=True,
        num_faces=1,
        min_face_detection_confidence=0.5,
        min_face_presence_confidence=0.5
    )
    
    with vision.FaceLandmarker.create_from_options(options) as landmarker:
        # Load image
        image = cv2.imread(image_path)
        if image is None: return {"mood": "neutral", "error": "Invalid image"}
        
        # Convert to RGB and MP format
        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb_image)
        
        # Detect
        detection_result = landmarker.detect(mp_image)
        
        if not detection_result.face_blendshapes:
            return {"mood": "neutral", "detected": False}
        
        # Mapping result
        mood = get_mood_from_blendshapes(detection_result.face_blendshapes[0])
        
        return {
            "mood": mood,
            "detected": True,
            "engine": "MediaPipe_Landmarker_Task"
        }

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image"}))
        sys.exit(1)
        
    output = process_image(sys.argv[1])
    print(json.dumps(output))
