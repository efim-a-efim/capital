---
layout: default
title: Data safety declaration
class: doc
---
# Data safety declaration

<p class="meta">Answers for the Google Play Console form (Policy and programs → App content → Data safety), with the reasoning behind each one. Reviewed against version 2.2.1 on 30 September 2026. The <a href="{{ '/privacy' | relative_url }}">Privacy Policy</a> is the user-facing statement of the same facts.</p>

## How the app handles data

Capital has no backend. Everything the user enters stays in a folder on the device. The only data that ever leaves the device is what the app sends, on the user's instruction, to the third-party data operators the user selects in Settings: public wallet addresses, token contract ids, currency codes and any API key the user entered for that operator. The operators answer the request; the app stores the returned balances and prices locally and keeps no copy of the request. No SDK in the app phones home: dependencies are AndroidX, Kotlin, OkHttp and Bouncy Castle only.

Google Play counts data as *collected* when it is transmitted off the device, even when no developer server is involved and the processing is ephemeral, so the declaration is not "collects nothing". It is a single ephemeral, optional data type.

## Form answers

### Overview

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** |
| Is all of the user data collected by your app encrypted in transit? | **Yes** — HTTPS only; cleartext traffic is disabled in the manifest |
| Do you provide a way for users to request that their data is deleted? | **Yes** — nothing is retained after the request completes, which satisfies the "deleted within 90 days of collection" rule for the badge. Users delete on-device data by deleting the folder and uninstalling; see the Privacy Policy. |

### Data types

Select exactly one type.

| Category | Data type | Collected | Shared | Ephemeral | Required or optional | Purposes |
|---|---|---|---|---|---|---|
| Financial info | Other financial info | Yes | No | **Yes** | **Optional** | App functionality |

What the type covers: public blockchain addresses the user tracks, the token contracts found on them, and the currency codes of the user's holdings. They are transmitted to the data operator the user selected so that balances and prices can be fetched, held in memory for the request, and discarded.

Why **not shared**: the transfer goes directly from the device to the operator the user chose, on a refresh the user started, after the app told the user in Settings which operator will be queried and that the request discloses the address and IP to that operator. This is the "user-initiated action where the user reasonably expects the data to be shared" exemption. The developer receives nothing and has no service providers.

Why **optional**: the app is fully usable with manual holdings only. Addresses and API keys are entered by choice.

API keys entered by the user are sent only to the operator that issued them. They are the user's credentials for that operator's own service and are not declared as a separate user data type; if a reviewer asks, describe them as above.

### Types that are **not** collected

Every other category is "No": no location, no personal info, no contacts, no messages, no photos or media, no files and docs, no app activity, no web browsing, no app info and performance (no crash logs, no diagnostics), no device or other identifiers. IP addresses reach the operators as part of any HTTPS request and are not used by the app for any purpose.

The user's financial records (holdings, goals, plans) are processed only on the device and are out of scope for the form.

### Security practices

| Item | Answer |
|---|---|
| Independent security review (MASA) | No |
| Committed to follow the Families policy | No (not a children's app) |

## Related declarations on the App content page

| Declaration | Answer |
|---|---|
| Privacy policy URL | `{{ site.url }}{{ site.baseurl }}/privacy` |
| Ads | No, the app contains no ads |
| App access | All functionality is available without special access. No login. Provider API keys are optional; every provider has a keyless default. |
| Content rating (IARC) | Utility / productivity questionnaire; no violence, sexual content, gambling, controlled substances, user interaction or location sharing. Expected result: Everyone / PEGI 3. |
| Target audience and content | 18 and over (personal finance tool; not designed for children) |
| News app | No |
| COVID-19 contact tracing and status | No |
| Data safety | As above |
| Government app | No |
| Financial features | See the [Financial features declaration]({{ '/financial-features' | relative_url }}) |
| Health apps | No health features |

## What to update when the app changes

Re-check this page when a release adds analytics, crash reporting, accounts, a developer-operated server, a new SDK with network access, or on-device sharing with another app. Any of these changes the form.
