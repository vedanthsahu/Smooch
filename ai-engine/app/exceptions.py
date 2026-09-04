class ModelUnavailableError(RuntimeError):
    """Raised when a requested model tier has no usable provider -- e.g. the
    external tier before an OpenRouter API key is configured. Callers should
    treat this as a routing signal (fall back or escalate to human), never
    silently swallow it. See docs/09-ai-engine-architecture.md."""
