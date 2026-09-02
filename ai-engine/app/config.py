from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    simulator_base_url: str = "http://127.0.0.1:8090"

    class Config:
        env_prefix = "AI_ENGINE_"
        env_file = ".env"


settings = Settings()
