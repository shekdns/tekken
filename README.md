# tekken
tekkenInfo

## EWGF API integration test

EWGF API key must not be committed. Keep it in an environment variable or a local `.env` file.

```bash
export EWGF_API_KEY=replace_with_your_ewgf_api_key
export EWGF_SAMPLE_TEKKEN_ID=27tB-4yhF-mfNE
sh gradlew test --tests EwgfApiIntegrationTest
```

If `EWGF_API_KEY` is not set, the external API integration test class is skipped.

## EWGF proxy API

Frontend clients should call this backend instead of calling EWGF directly.

```bash
export EWGF_API_KEY=replace_with_your_ewgf_api_key
sh gradlew bootRun
```

Available endpoints:

```http
GET /api/ewgf/battles/{tekkenId}
GET /api/ewgf/profile/{tekkenId}
POST /api/ewgf/profile
```

`POST /api/ewgf/profile` body:

```json
["27tB-4yhF-mfNE"]
```
