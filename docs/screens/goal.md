---
layout: screen
lang: en
base: ""
key: "screens/goal"
screen: goal
title: Goal
---
# Goal

**What it is.** One goal with its funding. Reached by tapping a card on the [Goals]({{ page.base }}/screens/goals) tab; **← All goals** goes back.

**Header.** Name and badge, funded / target, due date, the projection line, and **Still needed**: target minus what is funded today.

**Buttons.** **Edit goal** changes name, currency, target and due date. **Archive** keeps the goal without counting it; an archived goal shows **Activate** instead. **Delete** removes the goal and its connections after a confirmation.

**Funding sources.** The buckets that may fund this goal. **Connect bucket** adds one with a contribution limit:

- **Auto — up to remaining need**: the bucket gives whatever the goal still needs, after earlier goals took their share.
- **Fixed amount in goal currency**.
- **% of bucket**: at most that share of the bucket's value.
- **% of goal**: at most that share of the target.

The preview in the editor shows what the connection would contribute today. Limits are ceilings: goal order, available savings and other connections can reduce the contribution. Each listed source shows what it gives now and why it gives no more; **Edit connection** changes the limit, **Disconnect** removes it.

**Planned savings.** The plans that reach this goal, each with the amount the projection assigns to it. A plan that is used up by earlier goals does not appear here.

**Reading the projection.** "Planned savings close this goal on 20 Dec 2026 · on time" means the cumulative plans up to that date cover the remaining need before the due date. "Cover up to … · short by …" means they do not; add a plan, move the date, or lower the target.
