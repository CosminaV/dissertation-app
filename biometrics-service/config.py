import os
from dotenv import load_dotenv

load_dotenv()

PROFILE_IMAGE_PRESIGNEDURL_URL = os.getenv("PROFILE_IMAGE_PRESIGNEDURL_URL")