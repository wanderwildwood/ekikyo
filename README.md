# I Ching

The I Ching, the Classic of Changes, for the [Mudita Kompakt](https://mudita.com/products/kompakt/):
a hexagram for the day, casts of your own with a question, a journal of readings, and all 64
hexagrams to read through. Made for the Kompakt's e-ink screen, in black and white. It will
install on any Android 12 device.

| | |
|---|---|
| ![A reading: the hexagram cast, the one it becomes, and Legge's text](screenshots/1-reading.png) | ![A cast in progress, line by line](screenshots/2-cast.png) |
| ![The journal of readings](screenshots/3-journal.png) | ![A hexagram on its own page, with its two trigrams](screenshots/4-hexagram.png) |

## Getting it, and keeping it

Download <https://github.com/wanderwildwood/ekikyo/releases/latest/download/ekikyo.apk>
and sideload it. That address always points at the newest release, and every release
publishes a `.sha256` beside the APK if you would rather check than trust.

For updates without doing this by hand, add this repository to
[Obtainium](https://github.com/ImranR98/Obtainium):

    https://github.com/wanderwildwood/ekikyo

## What it does

- **Today**: one hexagram for the day, cast once and kept until midnight.
- **Cast**: ask a question if you like, then cast the six lines from the bottom up, one press
  each or all at once. Three coins by default; yarrow-stalk odds in Settings. Changing lines are
  marked, and the hexagram they turn into is shown beside the first.
- **Journal**: every cast with its date, question, lines and a note of your own. Export and
  restore it as a JSON file.
- **All 64**: each hexagram with its two trigrams, judgment, image and all six lines.

The text is set in Noto Serif, line by line, so pages turn the way a book's do: each page
full, a paragraph running on over the turn.

## The text

James Legge's translation, *The Yî King*, from *The Sacred Books of the East*, volume 16
(Oxford, 1882), and Richard Wilhelm's German, *I Ging: Das Buch der Wandlungen* (Jena, 1924),
chosen in Settings and German by default on a German phone. Both are in the public domain and
given as their translators wrote them. The short names of the hexagrams are written for this
app. Thanks to [baud-9600](https://github.com/baud-9600) for bringing in the German text
([#1](https://github.com/wanderwildwood/ekikyo/pull/1)).

## Privacy

No permissions, no internet. See [PRIVACY.md](PRIVACY.md).

## License

GPL-3.0-only, copyright wander wildwood. See [LICENSE](LICENSE).
