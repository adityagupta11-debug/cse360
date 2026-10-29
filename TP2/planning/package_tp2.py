"""Package TP2, check six matching status pairs, media and the extracted project.

python3 planning/package_tp2.py [--quick] [--name TP2-Team53.zip]
Uses only the standard library for MP4/MOV timing and audio/video track checks.
Subjective content checks are explicit in Media_Review.json, never inferred from filenames.
Incomplete archives are still written, but exit nonzero with NOT READY.
"""
import argparse
from collections import Counter
from datetime import date, datetime
import json
from pathlib import Path
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent.parent
VIDEO = ('.mp4', '.mov')
LIMIT = 100_000_000
EXCLUDED_DIRS = {'build', 'data', 'lib', 'bin', '__pycache__', '.settings_local', '.git'}
REQUIRED = [('AICoaching.PDF', 'Task 5'), ('TP2 Team Norms.pdf', 'Task 2'),
            ('Lessons_Learned_Javadocs/index.html', 'Task 3'),
            ('Contributor_Javadocs/index.html', 'Task 4'),
            ('ContributorTests_Javadocs/index.html', 'Task 7'),
            ('ContributorTests_Javadocs/Contributor_Requirements_and_Tests.html', 'Task 7.1'),
            ('src/module-info.java', 'Application'), ('.project', 'Eclipse'),
            ('.classpath', 'Eclipse'), ('dev.py', 'Build/test/run'),
            ('setup_dependencies.py', 'Portable dependencies')]
SCREENCASTS = [('LessonsLearnedScreencast', 300, 600),
              ('PhaseTwoGUIScreencast', 300, 600), ('TestingScreencast', 480, 900)]
PLAN = re.compile(r'Plan and Status (\d{4}-\d{2}-\d{2})\.pdf')
SUMMARY = re.compile(r'Status Summary (\d{4}-\d{2}-\d{2})\.(mp4|mov)')
MEMBERS = {'Vishwam Gupta', 'Joshua Luther', 'Kanish Garg',
           'Aditya Gupta (agupt515)', 'Aditya Gupta (agupt545)'}
results = []


def report(ok, message):
    results.append(bool(ok))
    print(('  PASS  ' if ok else '  FAIL  ') + message, flush=True)


def included(path):
    relative = path.relative_to(ROOT)
    return (path.is_file() and not EXCLUDED_DIRS.intersection(relative.parts[:-1])
            and relative.name != '.DS_Store' and path.suffix != '.class')


def valid_date(text):
    try:
        date.fromisoformat(text)
        return True
    except ValueError:
        return False


def atoms(handle, start, end):
    """Iterate bounded ISO BMFF boxes, rejecting truncated or invalid lengths."""
    position = start
    while position < end:
        if end - position < 8:
            raise ValueError('Truncated movie box header')
        handle.seek(position)
        size, kind = struct.unpack('>I4s', handle.read(8))
        header = 8
        if size == 1:
            size = struct.unpack('>Q', handle.read(8))[0]
            header = 16
        elif size == 0:
            size = end - position
        if size < header or position + size > end:
            raise ValueError('Invalid movie box length')
        yield kind, position + header, position + size
        position += size


def media_info(path):
    """Read timing/tracks only; codec integrity and content need separate review."""
    duration, handlers = None, set()
    with path.open('rb') as handle:
        for kind, start, end in atoms(handle, 0, path.stat().st_size):
            if kind != b'moov':
                continue
            for child, a, b in atoms(handle, start, end):
                if child == b'mvhd':
                    handle.seek(a)
                    data = handle.read(min(b - a, 36))
                    if data[0] not in (0, 1):
                        raise ValueError('Unknown movie timing version')
                    offset, width = (20, 8) if data[0] else (12, 4)
                    if len(data) < offset + 4 + width:
                        raise ValueError('Truncated movie timing')
                    scale = int.from_bytes(data[offset:offset + 4], 'big')
                    ticks = int.from_bytes(data[offset + 4:offset + 4 + width], 'big')
                    if not scale or ticks == (1 << (8 * width)) - 1:
                        raise ValueError('Unknown movie duration')
                    duration = ticks / scale
                elif child == b'trak':
                    for track, c, d in atoms(handle, a, b):
                        if track == b'mdia':
                            for item, e, f in atoms(handle, c, d):
                                if item == b'hdlr' and f - e >= 12:
                                    handle.seek(e + 8)
                                    handlers.add(handle.read(4))
    if duration is None or duration <= 0:
        raise ValueError('No positive movie duration')
    return {'duration_seconds': duration, 'video': b'vide' in handlers, 'audio': b'soun' in handlers}


def check_contents(files):
    names = {p.relative_to(ROOT).as_posix() for p in files}
    for name, task in REQUIRED:
        report(name in names, f'{task}: {name}')
    try:
        review = json.loads((ROOT / 'Media_Review.json').read_text())
    except (OSError, ValueError):
        review = {}
    records, media = review.get('files', {}), {}
    for path in files:
        if path.suffix.lower() not in VIDEO:
            continue
        name = path.relative_to(ROOT).as_posix()
        report(path.stat().st_size < LIMIT, f'{name}: {path.stat().st_size / 1e6:.2f} MB; under 100 MB')
        try:
            media[name] = media_info(path)
            report(media[name]['video'] and media[name]['audio'], f'{name}: audio and video tracks')
        except (OSError, ValueError, IndexError, struct.error) as error:
            report(False, f'{name}: invalid media metadata: {error}')
    presenters_all = set()
    for stem, low, high in SCREENCASTS:
        found = [n for n in names if n in {stem + ext for ext in VIDEO}]
        report(len(found) == 1, f'Exactly one root-level {stem}.mp4 or .mov')
        for name in found:
            seconds = media.get(name, {}).get('duration_seconds', 0)
            record = records.get(name, {})
            report(low <= seconds <= high, f'{name}: {seconds:.2f}s; required {low}-{high}s')
            presenters = record.get('presenters', [])
            known = len(set(presenters)) == 2 and set(presenters) <= MEMBERS
            report(known, f'{name}: two identified team presenters')
            if known:
                presenters_all.update(presenters)
            report(all(record.get(k) is True for k in ('content_reviewed', 'text_readable', 'audio_clear')),
                   f'{name}: required content, readable materials and clear audio reviewed')
            if stem == 'TestingScreencast':
                report(record.get('actual_test_execution_shown') is True,
                       f'{name}: actual execution of every documented test reviewed')
    report(presenters_all == MEMBERS and review.get('balanced_presentation_reviewed') is True,
           'All five members present; roughly equal total presenting time reviewed')
    folder = ROOT / 'TP2 Planning and Tracking'
    plans = [p for p in files if p.parent == folder and PLAN.fullmatch(p.name)]
    summaries = [p for p in files if p.parent == folder and SUMMARY.fullmatch(p.name)]
    pd = [PLAN.fullmatch(p.name).group(1) for p in plans]
    sd = [SUMMARY.fullmatch(p.name).group(1) for p in summaries]
    report(len(plans) >= 6 and all(valid_date(d) for d in pd), f'{len(plans)} valid dated PDFs; at least six')
    report(len(summaries) >= 6 and Counter(pd) == Counter(sd)
           and all(count == 1 for count in Counter(sd).values()),
           'At least six matching PDF/video pairs, exactly one summary per PDF date')
    summary_presenters, meetings = set(), []
    for path in sorted(summaries):
        name = path.relative_to(ROOT).as_posix()
        record = records.get(name, {})
        seconds = media.get(name, {}).get('duration_seconds', 0)
        report(180 <= seconds <= 300, f'{name}: {seconds:.2f}s; required 180-300s')
        presenters = record.get('presenters', [])
        known = bool(presenters) and set(presenters) <= MEMBERS
        report(known and record.get('covers_all_members') is True
               and all(record.get(k) is True for k in ('content_reviewed', 'text_readable', 'audio_clear')),
               f'{name}: whole-team progress/next/blockers, visuals and audio reviewed')
        if known:
            summary_presenters.update(presenters)
        try:
            meeting = datetime.fromisoformat(record['meeting_at'])
            assert meeting.tzinfo is not None
            assert meeting.date().isoformat() == SUMMARY.fullmatch(path.name).group(1)
            meetings.append(meeting)
        except (KeyError, TypeError, ValueError, AssertionError):
            report(False, f'{name}: actual timezone-aware meeting time must be confirmed')
    report(summary_presenters == MEMBERS, 'All five members presented a summary')
    meetings.sort()
    report(len(meetings) == len(plans) and len(meetings) >= 6
           and all((b - a).total_seconds() >= 86400 for a, b in zip(meetings, meetings[1:])),
           'Actual confirmed meetings at least 24 hours apart')
    if meetings:
        weeks = Counter((d.isocalendar().year, d.isocalendar().week) for d in meetings)
        report(all(n >= 2 for n in weeks.values()), 'At least two meetings per covered week')
    print('Media_Review.json records explicit content review; metadata alone cannot prove readability, '
          'narration quality, test coverage or meeting authenticity.', flush=True)


def build_zip(files, output):
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(files):
            archive.write(path, Path('TP2') / path.relative_to(ROOT))
    report(output.stat().st_size > 0, f'ZIP written: {output} ({output.stat().st_size / 1e6:.1f} MB)')


def check_packaged_copy(output, quick):
    with tempfile.TemporaryDirectory(prefix='tp2-package-') as temp:
        with zipfile.ZipFile(output) as archive:
            archive.extractall(temp)
        copy = Path(temp) / 'TP2'
        report(not any((copy / p).exists() for p in ('lib', 'build', 'data', 'bin')),
               'ZIP excludes local dependencies, build output and databases')
        if (ROOT / 'lib').exists():
            shutil.copytree(ROOT / 'lib', copy / 'lib')
        else:
            subprocess.run([sys.executable, 'setup_dependencies.py'], cwd=copy, check=True)
        command = [sys.executable, 'dev.py', 'test']
        if quick:
            command.append('testingAutomation.ContributorRequirementsTestbed')
        run = subprocess.run(command, cwd=copy, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        log = ROOT / 'build/reports/packaged-project.txt'
        log.parent.mkdir(parents=True, exist_ok=True)
        log.write_text(run.stdout, encoding='utf-8')
        last = [s for s in run.stdout.splitlines() if 'suites passed' in s]
        report(run.returncode == 0, 'Extracted project builds and passes: '
               + (last[-1] if last else run.stdout[-500:]))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--name', default='TP2-Team53.zip')
    parser.add_argument('--quick', action='store_true')
    args = parser.parse_args()
    if Path(args.name).name != args.name or not args.name.lower().endswith('.zip'):
        parser.error('--name must be a ZIP filename without a directory')
    files = [p for p in ROOT.rglob('*') if included(p)]
    check_contents(files)
    build_zip(files, ROOT / 'build/package' / args.name)
    check_packaged_copy(ROOT / 'build/package' / args.name, args.quick)
    failed = results.count(False)
    print(f'\n{len(results) - failed} checks passed; {failed} failed: '
          + ('READY TO SUBMIT' if failed == 0 else 'NOT READY - resolve the listed failures'), flush=True)
    sys.exit(1 if failed else 0)


if __name__ == '__main__':
    main()
