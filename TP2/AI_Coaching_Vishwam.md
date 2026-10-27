# Proposed Vishwam AI coaching contribution

Two fields proposed for Member 5's consolidation: experience `what` (stored as `whatDone`) and `how` (stored as `howDone`). These match the implemented record and GUI labels. Confirm allocations with the team; this document does not claim a prior assignment. Research checked October 27, 2026. This is planning research, not an implemented AI feature or the final team AICoaching.PDF.

| Field / owner | Priority | Coaching requirements in priority order | Free-to-use candidate and evidence | Acceptance example |
|---|---|---|---|---|
| What was done / Vishwam (proposed) | 1 | P1: ask for a concrete attempted action and observed outcome when absent. P2: suggest clearer wording and neutral tone. P3: flag spelling/grammar. Never invent tasks, measurements or success. | Ollama running locally can request structured feedback (missing details, questions, suggested revision) using a JSON schema. This is a proposed use of its structured-output capability, not an evaluated guarantee of coaching quality. Ollama's runtime is MIT licensed; select a model whose separate license permits the intended use. | Input "fixed it" prompts for the actual action and outcome; a proposed rewrite needs explicit author approval and does not add fabricated facts. |
| How it was done / Vishwam (proposed) | 2 | P1: coach for steps/tools and enough detail to repeat the attempt. P2: ask for missing context without inventing commands. P3: flag ambiguous wording and grammar. Respect field/lesson locks and retain draft on service failure. | Same local Ollama structured-feedback approach; one tool may support both fields. For optional proofreading, use a self-hosted LanguageTool instance rather than automating its public free endpoint, which prohibits automated requests. Self-hosting has operational/hardware costs even when there is no per-call hosted fee. | Input "used the tool" prompts for tool/version and steps. A locked field remains unchanged, and rejected suggestions leave the author's text intact. |

Sources:

- Ollama structured outputs: https://ollama.com/blog/structured-outputs (schema-constrained responses; illustrates local API).
- Ollama runtime license: https://github.com/ollama/ollama/blob/main/LICENSE (MIT; individual model licenses are separate).
- LanguageTool public API policy: https://dev.languagetool.org/public-http-api.html (public free service limits, no automated requests; directs automation to own instance/Enterprise).

Connection to TP3: the published Contributor epic calls for interactive writing, brainstorming and search support. Structured feedback is a candidate architecture for asking focused questions while preserving the Contributor's authorship. Any example retrieval must respect the existing own-record scope. This recommendation requires evaluation on representative team inputs before choosing a model.
