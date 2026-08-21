# Storyboard illustrations — provenance & license

Wordless step-by-step illustrations for the Guide safety tips, shown when a tip is opened. Flat teal
line-art of a consistent character on a white rounded-card background (theme-safe), captions added by
the app (`story_*` string resources) in all 4 locales — never baked into the image.

## Source & license

**Custom illustrations generated for this project** with Google **Gemini ("nano banana")** image
generation, from a locked style prompt (flat vector look, brand teal `#0F5E66`, amber/red only for
hazard elements, no text). **Project-owned** — not third-party stock; free to use, modify, and ship in
the app and store listings. Delivered as 1024×1024, downscaled to 384×384 PNG for the bundle (1024px
masters kept in `~/Documents/kuna-ayuda/storyboard/`).

## Files → step depicted

### During an earthquake — attached to the "During" tip (`tip_during_1`)
| File | Step |
|------|------|
| `story_during_1.png` | Drop / crouch down low (on hands and knees) |
| `story_during_2.png` | Cover your head under a sturdy table |
| `story_during_3.png` | Hold on to the table leg until the shaking stops |

### Before / preparedness — attached to the "Before" tip (`tip_before_1`)
| File | Step |
|------|------|
| `story_before_1.png` | Build an emergency kit (water, food, flashlight, first-aid, radio) |
| `story_before_2.png` | Make a family plan (checklist together) |
| `story_before_3.png` | Agree on a meeting point (family at an outdoor marker) |

### After the quake — attached to the "After" tip (`tip_after_1`)
| File | Step |
|------|------|
| `story_after_1.png` | Check for injuries (first aid) |
| `story_after_2.png` | Avoid hazards (cracked building, gas flame, downed power line) |
| `story_after_3.png` | Evacuate calmly to an open assembly area |

### Pets — attached to the Animals "evacuate with pets" tip (`tip_animals_1`)
| File | Step |
|------|------|
| `story_pets_1.png` | Pet emergency kit (carrier, leash, bowls, food, ID tag) |
| `story_pets_2.png` | Keep your pet close and calm during shaking — don't chase it |
| `story_pets_3.png` | Leash / crate it before going out, so it can't bolt |

### Calming techniques — attached to the Mental-health "calming techniques" tip (`tip_mental_3`)
| File | Step |
|------|------|
| `story_calm_1.png` | Breathe in slowly (seated, hand on belly) |
| `story_calm_2.png` | Breathe out slowly (shoulders dropping) |
| `story_calm_3.png` | Feel your feet on the ground (hand on heart, grounded) |

### Supporting children — attached to the Mental-health "supporting children" tip (`tip_mental_4`)
| File | Step |
|------|------|
| `story_children_1.png` | Comfort and reassure (hug at the child's eye level) |
| `story_children_2.png` | Listen calmly (let the child talk or draw feelings) |
| `story_children_3.png` | Keep a routine together (reading, comfort toy) |

## Notes
- Displayed via `Image` (full color, no tint) in `ui/tips/SafetyTipsScreen.kt` `StoryStepRow`.
- To add a storyboard to another tip: drop `story_<name>_N.png` here, add `story_<name>_N` captions to
  the 4 `strings.xml`, and reference them in a `StoryStep` list wired to that `Tip`.
