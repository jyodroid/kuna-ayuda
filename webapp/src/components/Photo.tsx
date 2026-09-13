import { useEffect, useState, type ReactNode } from "react";
import { photoObjectUrl } from "../api";

// Loads a /api/photos/{id} image with the X-App-Key header (an <img src> can't send headers), as a
// blob object URL, and revokes it on unmount. On any failure — network, 404, or an invalid/empty image
// that won't decode (onError) — it renders `fallback` instead of a broken-image icon.
export function Photo({ id, alt, className, fallback }: { id: number; alt: string; className?: string; fallback?: ReactNode }) {
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let objectUrl: string | null = null;
    let alive = true;
    setFailed(false);
    setUrl(null);
    photoObjectUrl(id)
      .then((u) => {
        objectUrl = u;
        if (alive) setUrl(u);
        else URL.revokeObjectURL(u);
      })
      .catch(() => alive && setFailed(true));
    return () => {
      alive = false;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [id]);

  if (failed) return <>{fallback ?? null}</>;
  if (!url) return <div className={`animate-pulse bg-neutral-200 ${className ?? ""}`} aria-hidden />;
  return <img src={url} alt={alt} loading="lazy" className={className} onError={() => setFailed(true)} />;
}
