"""Generate TP2/AICoaching.PDF (Task 5) from the field research below.

Aditya Gupta (agupt545): AI coaching compilation. Each field uses the stored attribute name from
Member 1's schema (lessonDB, lessonExperienceDB, lessonEffortDB, lessonCuratorDB) so the report
and the code agree. Edit FIELDS or SOURCES and rerun:

    python3 planning/make_ai_coaching_pdf.py      (from TP2; needs: pip install reportlab)
"""
from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import (KeepTogether, Paragraph, SimpleDocTemplate, Spacer, Table,
                                TableStyle)

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / 'AICoaching.PDF'
COMPILED = '2026-10-27'

KANISH, VISHWAM, JOSHUA = 'Kanish Garg', 'Vishwam Gupta', 'Joshua Luther'
A515, A545 = 'Aditya Gupta (agupt515)', 'Aditya Gupta (agupt545)'

# Where each member's two sections came from; shown in 5.3 so readers know what is confirmed.
SECTION_STATUS = {
    KANISH: 'Kanish Garg AI Coaching Current Fields.pdf (researched 2026-10-25)',
    VISHWAM: 'AI_Coaching_Vishwam.md (researched 2026-10-27)',
    JOSHUA: 'Drafted by agupt545 on 2026-10-27; Joshua to confirm or replace',
    A515: 'Researched 2026-10-27',
    A545: 'Researched 2026-10-27',
}

# Priority order: fields every Contributor fills in every lesson come first, because the
# Contributor Phase Three epic coaches lesson writing; experience text next; numeric experience
# fields next; Curator-only fields that support the Curator epics last.
FIELDS = [
    dict(field='lessonText', label='Lesson Text', table='lessonDB', owner=KANISH,
         story='Contributor C1; Contributor Phase 3 epic',
         why='The reusable takeaway; the field future readers rely on most.',
         reqs=[('P1', 'Ask focused questions when the action, outcome or practical takeaway is '
                'missing. Never invent events.', 'Vague input produces questions; no new facts.'),
               ('P1', 'Flag ambiguous wording and offer a clearer version that keeps technical '
                'terms and stated uncertainty.', 'Every claim in a suggestion is in the input.'),
               ('P2', 'Explain spelling, grammar and blaming-tone issues with specific fixes.',
                'Each proposed edit has a reason.'),
               ('P2', 'Require explicit acceptance; keep the original if rejected.',
                'Nothing is saved automatically.'),
               ('P3', 'Keep suggestions within the 20-2000 character rule.',
                'LessonRecognizer accepts any accepted suggestion.')],
         tools='Gemini Apps without a paid AI plan, used manually with a review prompt [S1][S2]. '
               'Use non-confidential sample lessons; Google describes possible human review of '
               'collected data [S3].',
         sources=['S1', 'S2', 'S3']),
    dict(field='situation', label='Situation', table='lessonDB', owner=JOSHUA,
         story='Contributor C1; Contributor Phase 3 epic',
         why='Context tells a later reader when the lesson applies.',
         reqs=[('P1', 'Ask for missing context: what the team was doing, when, and the impact. '
                'Never supply details the author did not give.',
                '"It broke" prompts for what broke and its effect; no details are added.'),
               ('P1', 'Flag text that states the lesson instead of the situation and suggest '
                'moving it to Lesson Text.', 'A takeaway sentence is flagged, not rewritten.'),
               ('P2', 'Flag spelling, grammar and readability problems.',
                'Each issue is shown with its position and a suggested fix.'),
               ('P3', 'Respect the Situation field lock and the 20-2000 character rule.',
                'A locked field offers no edit; suggestions stay within limits.')],
         tools='LanguageTool run as your own local HTTP server (core is LGPL 2.1+) for grammar '
               'and style checks [S5][S6], and Ollama running a local model with schema-'
               'constrained output for the missing-context questions [S4].',
         sources=['S4', 'S5', 'S6']),
    dict(field='title', label='Title', table='lessonDB', owner=KANISH,
         story='Contributor C1-C2; Viewer search (Phase 3)',
         why='Titles drive the My Lessons list and later searches.',
         reqs=[('P1', 'Flag vague titles; offer alternatives grounded only in the Situation and '
                'Lesson Text.', 'No unsupported event or result appears.'),
               ('P1', 'Flag a title that disagrees with the lesson and ask which is right.',
                'An unrelated title is flagged, not silently changed.'),
               ('P2', 'Offer up to three alternatives with reasons; the author may keep the '
                'original.', 'Nothing is saved automatically.'),
               ('P2', 'Keep each suggestion within 5-100 characters and keep domain terms.',
                'Every accepted title passes LessonRecognizer.')],
         tools='Gemini Apps without a paid AI plan, used manually [S1][S2][S3].',
         sources=['S1', 'S2', 'S3']),
    dict(field='whatDone', label='What was done (experience)', table='lessonExperienceDB',
         owner=VISHWAM, story='Contributor C3',
         why='Records the new attempt to apply the lesson.',
         reqs=[('P1', 'Ask for a concrete action and observed outcome when absent. Never invent '
                'tasks, measurements or success.', '"fixed it" prompts for the action and outcome.'),
               ('P2', 'Suggest clearer, neutral wording.', 'Rewrites need explicit approval.'),
               ('P3', 'Flag spelling and grammar.', 'Issues are listed, not auto-applied.')],
         tools='Ollama with a local model returning structured feedback (missing details, '
               'questions, suggested revision) through a JSON schema [S4]. The Ollama runtime '
               'is MIT licensed; each model has its own license [S7].',
         sources=['S4', 'S7']),
    dict(field='howDone', label='How it was done (experience)', table='lessonExperienceDB',
         owner=VISHWAM, story='Contributor C3',
         why='Steps and tools make the attempt repeatable.',
         reqs=[('P1', 'Coach for steps and tools, with enough detail to repeat the attempt.',
                '"used the tool" prompts for the tool, version and steps.'),
               ('P2', 'Ask for missing context without inventing commands.',
                'No command or setting appears that the author did not give.'),
               ('P3', 'Flag ambiguous wording and grammar; respect locks; keep the draft if the '
                'service fails.', 'A locked lesson offers no edit; a failure keeps the text.')],
         tools='The same local Ollama structured-feedback approach [S4][S7], with a self-hosted '
               'LanguageTool server for proofreading [S5][S6].',
         sources=['S4', 'S5', 'S6', 'S7']),
    dict(field='category', label='Category', table='lessonDB', owner=JOSHUA,
         story='Contributor C1; Viewer search (Phase 3)',
         why='A consistent category groups lessons for browsing and search.',
         reqs=[('P1', 'Suggest the best match from LessonsLearned.CATEGORIES only, with a '
                'confidence score, from the Title, Situation and Lesson Text.',
                'The suggestion is always one of the six categories.'),
               ('P1', 'Never change the category without the author choosing it.',
                'The chosen value is the one saved.'),
               ('P2', 'When the top two scores are close, show both and ask the author.',
                'Ties are presented as a question.'),
               ('P3', 'Respect the Category field lock.', 'A locked category offers no change.')],
         tools='Zero-shot classification with the free MIT-licensed facebook/bart-large-mnli '
               'model through the Hugging Face Transformers pipeline, passing the six categories '
               'as candidate labels [S8][S9].',
         sources=['S8', 'S9']),
    dict(field='searchTerms', label='Search terms (curator only)', table='lessonCuratorDB',
         owner=A545, story='Curator epic: AI-built search terms',
         why='Lets users find a lesson when their words differ from the lesson\'s words.',
         reqs=[('P1', 'Propose key phrases taken from the lesson text and ranked by relevance; '
                'the Curator accepts or removes each one.',
                'Every proposed phrase appears in, or is similar to, the lesson.'),
               ('P1', 'Never show search terms to the Contributor (story C1).',
                'Contributor views contain no search terms (testbed CR-08).'),
               ('P2', 'Suggest related terms by meaning (for example "merge conflict" for '
                '"branches collided") and show the similarity score.',
                'Each related term shows its score; low scores are not offered.'),
               ('P3', 'Keep the list within the 2000-character column.',
                'setCuratorData accepts the saved list.')],
         tools='KeyBERT (MIT) for keyword and key-phrase extraction with BERT embeddings [S10], '
               'with Sentence Transformers (Apache-2.0) for meaning-based similarity [S11][S12].',
         sources=['S10', 'S11', 'S12']),
    dict(field='effortHours', label='Member effort hours (memberName, effortHours)',
         table='lessonEffortDB', owner=A515, story='Contributor C3',
         why='Per-member effort is the raw data for the Curator time-and-effort epic.',
         reqs=[('P1', 'Turn typed amounts such as "90 min" or "half a day" into decimal hours and '
                'show the converted value for the author to confirm.',
                '"90 min" is shown as 1.5 hours before saving.'),
               ('P1', 'Ask when the effort total looks inconsistent with the duration (for '
                'example one member\'s effort is far above the elapsed time).',
                'A question is asked; nothing is changed automatically.'),
               ('P2', 'Ask about team members named in What or How who have no effort row.',
                'Missing members are listed as a question.'),
               ('P3', 'Keep the existing rules: each member once, non-negative hours, positive '
                'total.', 'ExperienceRecognizer accepts every accepted suggestion.')],
         tools='Duckling (BSD), an open-source parser that turns text such as "3 mins" into '
               'structured durations [S13], and a local Ollama model with a JSON schema for the '
               'consistency questions [S4].',
         sources=['S13', 'S4']),
    dict(field='durationHours', label='Duration in hours (experience)',
         table='lessonExperienceDB', owner=A515, story='Contributor C3',
         why='Elapsed time, separate from effort, shows how long reuse really takes.',
         reqs=[('P1', 'Convert typed durations ("2 days", "45 minutes") to hours and show the '
                'result for confirmation.', '"45 minutes" is shown as 0.75 hours.'),
               ('P2', 'Explain the difference between elapsed duration and member effort when the '
                'author enters the same value in both.', 'An explanation appears; values stay.'),
               ('P3', 'Keep the positive, three-decimal limit.',
                'ExperienceRecognizer accepts every accepted value.')],
         tools='Duckling duration parsing (BSD) [S13].',
         sources=['S13']),
    dict(field='aggregateSummary', label='Aggregate time-and-effort summary (curator only)',
         table='lessonCuratorDB', owner=A545,
         story='Curator epics: merge time and effort data; make it visible or not',
         why='Turns many experience records into numbers a future team can plan with.',
         reqs=[('P1', 'Compute the statistics (count, mean, median, minimum, maximum, standard '
                'deviation) with ordinary code, not with AI.',
                'Numbers match a hand calculation on sample data.'),
               ('P1', 'Let AI only draft a plain-language summary of those computed numbers, '
                'returned in a fixed JSON structure.', 'The draft contains no number that was not '
                'computed.'),
               ('P2', 'Warn when there are too few records for a meaningful summary.',
                'Fewer than three records shows a warning.'),
               ('P3', 'Show the summary to the Contributor only when summaryVisible is true.',
                'With summaryVisible false the Contributor sees nothing.')],
         tools='Apache Commons Math DescriptiveStatistics for the statistics (not AI) [S14], '
               'with a local Ollama model using a JSON schema for the narrative draft [S4][S7].',
         sources=['S14', 'S4', 'S7']),
]

SOURCES = {
    'S1': ('Google, Gemini Apps limits and feature availability',
           'https://support.google.com/gemini/answer/16275805?hl=en'),
    'S2': ('Google, Use Gemini Apps', 'https://support.google.com/gemini/answer/13275745?hl=en'),
    'S3': ('Google, Gemini Apps Privacy Hub',
           'https://support.google.com/gemini/answer/13594961?hl=en'),
    'S4': ('Ollama, Structured outputs (2024-12-06)', 'https://ollama.com/blog/structured-outputs'),
    'S5': ('LanguageTool, Run your own HTTP server', 'https://dev.languagetool.org/http-server'),
    'S6': ('LanguageTool source repository (LGPL 2.1 or later)',
           'https://github.com/languagetool-org/languagetool'),
    'S7': ('Ollama runtime license (MIT)', 'https://github.com/ollama/ollama/blob/main/LICENSE'),
    'S8': ('Hugging Face, facebook/bart-large-mnli model card (MIT)',
           'https://huggingface.co/facebook/bart-large-mnli'),
    'S9': ('Hugging Face, Zero-shot classification task guide',
           'https://huggingface.co/tasks/zero-shot-classification'),
    'S10': ('KeyBERT repository (MIT)', 'https://github.com/MaartenGr/KeyBERT'),
    'S11': ('Sentence Transformers documentation', 'https://www.sbert.net/'),
    'S12': ('Sentence Transformers repository (Apache-2.0)',
            'https://github.com/UKPLab/sentence-transformers'),
    'S13': ('Duckling repository (BSD)', 'https://github.com/facebook/duckling'),
    'S14': ('Apache Commons Math 3.6.1, DescriptiveStatistics',
            'https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/'
            'commons/math3/stat/descriptive/DescriptiveStatistics.html'),
}


def build():
    styles = getSampleStyleSheet()
    body = ParagraphStyle('body', parent=styles['BodyText'], fontSize=9.5, leading=12.5)
    small = ParagraphStyle('small', parent=body, fontSize=8.5, leading=10.5)
    h1 = ParagraphStyle('h1', parent=styles['Title'], fontSize=20, spaceAfter=4)
    h2 = ParagraphStyle('h2', parent=styles['Heading2'], fontSize=13, spaceBefore=10, spaceAfter=4)
    h3 = ParagraphStyle('h3', parent=styles['Heading3'], fontSize=11, spaceBefore=8, spaceAfter=2)
    grid = TableStyle([('GRID', (0, 0), (-1, -1), 0.4, colors.HexColor('#999999')),
                       ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#e8ecf1')),
                       ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                       ('LEFTPADDING', (0, 0), (-1, -1), 4), ('RIGHTPADDING', (0, 0), (-1, -1), 4)])

    def p(text, style=small):
        return Paragraph(text, style)

    story = [Paragraph('AI Coaching for Lesson Input Fields', h1),
             p('CSE 360 Team Project Phase Two, Team 53. Compiled by Aditya Gupta (agupt545) on '
               f'{COMPILED}. Field names are the stored attribute names in Member 1\'s schema '
               '(Vishwam Gupta), so this report matches the code. This is planning research for '
               'the Phase Three AI epics; no AI feature is claimed as implemented in TP2.', body),
             Paragraph('5.2 Prioritized field list', h2),
             p('Priority 1 is the field where coaching helps the most people most often. Fields '
               'every Contributor fills in every lesson come first, then experience text, then '
               'numeric experience fields, then Curator-only fields that serve the Curator epics.',
               body), Spacer(1, 4)]
    rows = [[p('<b>#</b>'), p('<b>Field (stored name)</b>'), p('<b>Table</b>'), p('<b>Owner</b>'),
             p('<b>Story / epic</b>'), p('<b>Why this priority</b>')]]
    for i, f in enumerate(FIELDS, 1):
        rows.append([p(str(i)), p(f"<b>{f['field']}</b><br/>{f['label']}"), p(f['table']),
                     p(f['owner']), p(f['story']), p(f['why'])])
    t = Table(rows, colWidths=[0.3 * inch, 1.55 * inch, 1.3 * inch, 1.15 * inch, 1.35 * inch,
                               1.65 * inch], repeatRows=1)
    t.setStyle(grid)
    story += [t, Paragraph('5.3 Field assignments (two per member)', h2)]
    owners = {}
    for i, f in enumerate(FIELDS, 1):
        owners.setdefault(f['owner'], []).append(f"{f['field']} (#{i})")
    rows = [[p('<b>Member</b>'), p('<b>Fields researched</b>'), p('<b>Section source</b>')]]
    for owner in [KANISH, VISHWAM, JOSHUA, A515, A545]:
        rows.append([p(owner), p(', '.join(owners[owner])), p(SECTION_STATUS[owner])])
    t = Table(rows, colWidths=[1.6 * inch, 2.6 * inch, 3.1 * inch])
    t.setStyle(grid)
    story += [t, Paragraph('Rules for every field', h3),
              p('AI output is a suggestion: the author (or Curator) must accept it, nothing is saved '
                'automatically, and a rejected suggestion leaves the original text unchanged. '
                'Suggestions never invent facts, events, numbers or results. Locked fields and '
                'locked lessons receive no suggestions. Every accepted value must still pass the '
                'existing validation (LessonRecognizer or ExperienceRecognizer). If the AI service '
                'is unavailable, the form keeps the typed draft. Evaluate each tool on '
                'non-confidential sample lessons before choosing it; free availability and limits '
                'must be rechecked before implementation.', body),
              Paragraph('5.4 Coaching requirements, free AI tools and citations', h2)]
    for i, f in enumerate(FIELDS, 1):
        rows = [[p('<b>Priority</b>'), p('<b>Coaching requirement</b>'), p('<b>Acceptance check</b>')]]
        rows += [[p(a), p(b), p(c)] for a, b, c in f['reqs']]
        t = Table(rows, colWidths=[0.65 * inch, 3.75 * inch, 2.9 * inch])
        t.setStyle(grid)
        cites = '; '.join(f"[{s}] {SOURCES[s][1]}" for s in f['sources'])
        story.append(KeepTogether([
            Paragraph(f"{i}. {f['field']} \u2013 {f['label']} \u2013 owner: {f['owner']}", h3), t, Spacer(1, 3),
            p(f"<b>Free AI tool:</b> {f['tools']}"), p(f"<b>Citations:</b> {cites}")]))
    story.append(Paragraph(f'Sources (URLs checked {COMPILED})', h2))
    for key, (title, url) in SOURCES.items():
        story.append(p(f'[{key}] {title}. <link href="{url}" color="blue">{url}</link>'))
    doc = SimpleDocTemplate(str(OUTPUT), pagesize=letter, leftMargin=0.6 * inch,
                            rightMargin=0.6 * inch, topMargin=0.6 * inch, bottomMargin=0.6 * inch,
                            title='AICoaching', author='Team 53')
    doc.build(story)
    print('Wrote', OUTPUT)


if __name__ == '__main__':
    build()
