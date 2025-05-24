from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from api import profile_image, verify_face, websocket_exam
import logging

app = FastAPI()

# CORS settings
app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://localhost:3000"],  # Update if needed
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(name)s - %(message)s"
)

# Register routers
app.include_router(profile_image.router, prefix="/api/profile-image", tags=["Profile Image"])
app.include_router(verify_face.router, prefix="/api", tags=["Verify face"])
app.include_router(websocket_exam.router, tags=["Exam"])

@app.get("/")
def root():
    return {"message": "Biometrics microservice is up and running"}
