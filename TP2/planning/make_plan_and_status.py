"""Generate "TP2 Planning and Tracking/Plan and Status YYYY-MM-DD.pdf" (Task 8.0-8.1).

Aditya Gupta (agupt545): after each standup, update "TP2 Task Board.csv" (status, planned Done
date, done since last standup, next, blockers) and the summary-video rotation, then run from TP2:

    python3 planning/make_plan_and_status.py 2026-10-27     (needs: pip install reportlab)

The PDF has the plan (every task with owner, status and planned Done date), one status slide
per member (done / next / blockers, from that member's In Progress and recently Done tasks), and
the summary-video rotation.
"""
import csv
import sys
from datetime import date
from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.pagesizes import landscape, letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import PageBreak, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

FOLDER = Path(__file__).resolve().parent.parent / 'TP2 Planning and Tracking'
MEMBERS = [('Vishwam Gupta', 'Lesson entity, experience data, persistence, validation'),
           ('Kanish Garg', 'Contributor lesson and experience GUI'),
           ('Joshua Luther', 'Lesson list class, search, list GUI, Team Norms'),
           ('Aditya Gupta (agupt515)', 'Requirements coverage, integration, testing'),
           ('Aditya Gupta (agupt545)', 'AI coaching, planning evidence, final packaging')]


def owned_by(row, member):
    # "Aditya Gupta agupt545 (4 min)" and "Aditya Gupta (agupt545)" name the same member.
    owner = row['Owner'].replace('(', '').replace(')', '')
    name = member.replace('(', '').replace(')', '')
    return name in owner or row['Owner'] == 'All members'


def build(day):
    date.fromisoformat(day)   # reject a malformed date before writing a misnamed file
    tasks = list(csv.DictReader((FOLDER / 'TP2 Task Board.csv').open(encoding='utf-8')))
    rotation = list(csv.DictReader((FOLDER / 'Summary Video Rotation.csv').open(encoding='utf-8')))
    styles = getSampleStyleSheet()
    cell = ParagraphStyle('cell', parent=styles['BodyText'], fontSize=8.5, leading=10.5)
    body = ParagraphStyle('body', parent=styles['BodyText'], fontSize=11, leading=14)
    title = ParagraphStyle('title', parent=styles['Title'], fontSize=22, spaceAfter=6)
    h2 = ParagraphStyle('h2', parent=styles['Heading2'], fontSize=16, spaceAfter=6)
    grid = TableStyle([('GRID', (0, 0), (-1, -1), 0.4, colors.HexColor('#999999')),
                       ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#e8ecf1')),
                       ('VALIGN', (0, 0), (-1, -1), 'TOP')])
    shade = {'Done': '#e3f3e6', 'In Progress': '#fff4d6', 'To Do': '#f6e3e1'}

    def p(text, style=cell):
        return Paragraph(text.replace('&', '&amp;').replace('<', '&lt;'), style)

    story = [Paragraph(f'TP2 Plan and Status {day}', title),
             p('CSE 360 Team Project Phase Two, Team 53. Standup record maintained by Aditya Gupta '
               '(agupt545). Built from TP2 Task Board.csv; each member confirms their slide at the '
               'standup.', body), Spacer(1, 6)]
    counts = {s: sum(1 for t in tasks if t['Status'] == s) for s in ('Done', 'In Progress', 'To Do')}
    story.append(p(f"Plan: {len(tasks)} tasks. Done {counts['Done']}, In Progress "
                   f"{counts['In Progress']}, To Do {counts['To Do']}.", body))
    story.append(Spacer(1, 6))
    rows = [[Paragraph(f'<b>{h}</b>', cell) for h in
             ('ID', 'Task', 'Owner', 'Status', 'Planned Done', 'Blockers')]]
    style = TableStyle(grid.getCommands())
    for i, t in enumerate(tasks, 1):
        rows.append([p(t['ID']), p(t['Task']), p(t['Owner']), p(t['Status']), p(t['Planned Done']),
                     p(t['Blockers'])])
        style.add('BACKGROUND', (3, i), (3, i), colors.HexColor(shade.get(t['Status'], '#ffffff')))
    table = Table(rows, colWidths=[0.45 * inch, 3.6 * inch, 1.9 * inch, 0.85 * inch, 0.95 * inch,
                                   2.25 * inch], repeatRows=1)
    table.setStyle(style)
    story.append(table)

    for member, role in MEMBERS:
        mine = [t for t in tasks if owned_by(t, member)]
        active = [t for t in mine if t['Status'] != 'Done' or t['Planned Done'] == day]
        story += [PageBreak(), Paragraph(f'{member}: status', h2), p(role, body), Spacer(1, 8)]
        rows = [[Paragraph(f'<b>{h}</b>', cell) for h in
                 ('Task', 'Status', 'Done since last standup', 'Next', 'Blockers')]]
        for t in active:
            rows.append([p(f"{t['ID']} {t['Task']}"), p(t['Status']), p(t['Done since last standup']),
                         p(t['Next']), p(t['Blockers'])])
        if len(rows) == 1:
            rows.append([p('No open tasks'), p(''), p(''), p(''), p('')])
        table = Table(rows, colWidths=[2.6 * inch, 0.85 * inch, 2.55 * inch, 2.1 * inch, 1.9 * inch],
                      repeatRows=1)
        table.setStyle(grid)
        story.append(table)

    story += [PageBreak(), Paragraph('Standup summary video rotation (Task 8.2)', h2)]
    rows = [[Paragraph(f'<b>{h}</b>', cell) for h in rotation[0].keys()]]
    rows += [[p(v) for v in r.values()] for r in rotation]
    table = Table(rows, colWidths=[2.5 * inch, 1.8 * inch, 3.0 * inch, 1.6 * inch])
    table.setStyle(grid)
    story.append(table)

    output = FOLDER / f'Plan and Status {day}.pdf'
    SimpleDocTemplate(str(output), pagesize=landscape(letter), leftMargin=0.5 * inch,
                      rightMargin=0.5 * inch, topMargin=0.5 * inch, bottomMargin=0.5 * inch,
                      title=f'Plan and Status {day}', author='Team 53').build(story)
    print('Wrote', output)


if __name__ == '__main__':
    if len(sys.argv) != 2:
        raise SystemExit('Usage: python3 planning/make_plan_and_status.py YYYY-MM-DD')
    build(sys.argv[1])
