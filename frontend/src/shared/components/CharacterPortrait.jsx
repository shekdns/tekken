import { useEffect, useMemo, useState } from 'react';
import { characterAccentStyle, characterAssetPath, characterFallbackLabel } from '../utils/characters';

export function CharacterPortrait({ character, label, size = 'md' }) {
  const [failed, setFailed] = useState(false);
  const imageUrl = useMemo(() => characterAssetPath(character), [character]);
  const fallbackSource = label || character?.displayName || character?.name;
  const fallback = characterFallbackLabel(fallbackSource);
  const accentStyle = characterAccentStyle(character?.assetKey || fallbackSource);
  const hasImage = imageUrl && !failed;

  useEffect(() => {
    setFailed(false);
  }, [imageUrl]);

  return (
    <span
      className={`character-portrait ${size} ${hasImage ? 'has-image' : 'fallback'}`}
      style={accentStyle}
      aria-hidden="true"
    >
      {hasImage ? (
        <img src={imageUrl} alt="" onError={() => setFailed(true)} />
      ) : (
        <>
          <span className="portrait-grid" />
          <span className="portrait-mark" />
          <span className="portrait-label">{fallback}</span>
        </>
      )}
    </span>
  );
}
