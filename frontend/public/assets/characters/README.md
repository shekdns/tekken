# Character Assets

T8LAB character portraits should use this path pattern:

```text
/assets/characters/{assetKey}.webp
```

The `assetKey` comes from `GET /api/characters/options`.

Examples:

```text
/assets/characters/dragunov.webp
/assets/characters/kazuya.webp
/assets/characters/reina.webp
```

When an image file is missing, the frontend falls back to a text badge.

## Policy

Do not add official Tekken character images, fan wiki images, community images, or AI-generated character likenesses until the source and usage rights are reviewed.

For the project-level asset policy, see:

```text
docs/asset-policy.md
```

When production images are added, include `asset-manifest.json` in this directory with source URL, license/usage notes, checked date, and production approval status.
