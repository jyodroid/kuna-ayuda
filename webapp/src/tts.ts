import { useCallback, useEffect, useState } from "react";
import type { Lang } from "./i18n";

// Text-to-speech via the browser's Web Speech API (SpeechSynthesis) — the web equivalent of the app's
// ui/platform Speaker. Free, offline (OS voices), no library. Must be started by a user gesture (a tap).

export const ttsAvailable = typeof window !== "undefined" && "speechSynthesis" in window;

// Mirrors core/domain util tipSpeechText: title, then body, with a pause between.
export function tipSpeech(title: string, body: string): string {
  const t = title.trim();
  const b = body.trim();
  if (!t) return b;
  if (!b) return t;
  const sep = ".!?:".includes(t[t.length - 1]) ? " " : ". ";
  return t + sep + b;
}

// Pick a voice matching the language, if the browser exposes one.
function pickVoice(lang: Lang): SpeechSynthesisVoice | undefined {
  const want = lang === "es" ? "es" : "en";
  return window.speechSynthesis.getVoices().find((v) => v.lang.toLowerCase().startsWith(want));
}

export function useSpeech() {
  const [speakingId, setSpeakingId] = useState<string | null>(null);

  // getVoices() is populated asynchronously in some browsers.
  useEffect(() => {
    if (!ttsAvailable) return;
    const warm = () => window.speechSynthesis.getVoices();
    warm();
    window.speechSynthesis.addEventListener("voiceschanged", warm);
    return () => window.speechSynthesis.removeEventListener("voiceschanged", warm);
  }, []);

  // Stop any speech when the component using the hook unmounts.
  useEffect(
    () => () => {
      if (ttsAvailable) window.speechSynthesis.cancel();
    },
    [],
  );

  const stop = useCallback(() => {
    if (ttsAvailable) window.speechSynthesis.cancel();
    setSpeakingId(null);
  }, []);

  const speak = useCallback(
    (id: string, text: string, lang: Lang) => {
      if (!ttsAvailable) return;
      window.speechSynthesis.cancel();
      const u = new SpeechSynthesisUtterance(text);
      u.lang = lang === "es" ? "es-ES" : "en-US";
      const v = pickVoice(lang);
      if (v) u.voice = v;
      u.onend = () => setSpeakingId((cur) => (cur === id ? null : cur));
      u.onerror = () => setSpeakingId((cur) => (cur === id ? null : cur));
      setSpeakingId(id);
      window.speechSynthesis.speak(u);
    },
    [],
  );

  return { available: ttsAvailable, speakingId, speak, stop };
}
