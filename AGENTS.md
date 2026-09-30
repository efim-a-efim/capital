# Rules for AI agents working in this repository

## Every text change is made in all 15 languages

The app (`app/src/main/assets/i18n/<code>.json`) and the site (`docs/`, English at the root, the other languages under `docs/<code>/`, layout strings in `docs/_data/ui.yml`) exist in English, Chinese, Hindi, Spanish, Arabic, French, Bengali, Portuguese, Russian, Indonesian, Urdu, German, Japanese, Marathi and Vietnamese.

- A change to any user-facing text — app strings, site pages, manual, screen docs, Privacy Policy, Data safety or Financial features declarations, store texts — is not finished until the same change is applied to every other language. English is the source; translate from it.
- UI terms in the docs must match the app's own translation: take them from the app JSON file of that language.
- After translating, verify: `I18nTest` must pass (every app string in every language, same placeholders); for site pages, check that each translated file has the same Liquid tags and links as the English file, the front matter `lang`/`base` of its language, and the same anchors (`{#…}` ids) and table row counts. Fix what fails before committing.
- Screenshots on the site are per language (`docs/screenshots/<code>/`); a screen change needs new captures in every language.

The translation rules for agents are in `.tools/site-translate.md` (not tracked).
