# Python Patterns

## Environment and Secrets
- Always use `.venv` (virtual environment) for project dependencies. Never install packages globally.
- Store credentials and secrets in `.env` files. Never hardcode them in source code.
- Add `.env` and `.venv/` to `.gitignore`. Never commit secrets.
- Use `python-dotenv` or equivalent to load `.env` at runtime.

## Project Structure
- Keep analysis outputs and generated data in a dedicated folder (e.g., `analisis/`, `output/`, `results/`), always separated from source code.
- Never mix generated artifacts with source files.
- Each script or module should have a single responsibility. Avoid monolithic files.

## Code Style
- Prefer explicit over implicit. Avoid magic values — use named constants or config.
- Validate inputs at the entry point of each function or module (fail fast).
- Prefer `pathlib.Path` over raw string paths for file operations.
- Use type hints on function signatures to improve readability.

## Testing
- Write tests for non-trivial logic. Prefer pytest.
- Do not test implementation details — test observable behavior.

## AI API Calls (Gemini, OpenAI, Anthropic, Vertex AI)
- Always wrap AI API calls with retry logic with exponential backoff. Never call the API bare.
- Detect retryable errors by checking for `429`, `RESOURCE_EXHAUSTED`, `503`, `500`, `502`, `504` in the exception message or `status_code` attribute.
- Use `delay * (attempt + 1)` as the wait formula (linear-exponential backoff). Default: 8 retries, 20s base.
- Add a configurable `call_delay_seconds` fixed pre-call delay to throttle sustained request rates. Default: 2s for Gemini/Vertex.
- Never hardcode retry counts or delays — expose them as config constants overridable via env vars and CLI args.
- Keep retry logic in one place (e.g. `_invoke_with_retry`). Invoker functions must not retry themselves.
- When parallelizing API calls, always bound concurrency with `max_workers`. Never use unbounded `ThreadPoolExecutor`.
- Always print `[retry]` messages with attempt number, error type, and wait time so the user knows what is happening.

```python
# Pattern: exponential backoff wrapper
def _invoke_with_retry(invoker, ..., max_retries=8, retry_delay_seconds=20.0, call_delay_seconds=2.0):
    if call_delay_seconds > 0:
        time.sleep(call_delay_seconds)
    for attempt in range(max_retries):
        try:
            return invoker(...)
        except Exception as exc:
            if not _is_retryable(exc) or attempt == max_retries - 1:
                raise
            wait = retry_delay_seconds * (attempt + 1)
            print(f"  [retry] {type(exc).__name__} (attempt {attempt+1}/{max_retries}). Waiting {wait:.1f}s…")
            time.sleep(wait)
```
