"""Build and check the final TP2 submission zip (Task 1).

Aditya Gupta (agupt545): run from TP2 after every deliverable is in place:

    python3 planning/package_tp2.py                 # build build/package/TP2-Team53.zip and check it
    python3 planning/package_tp2.py --name X.zip    # use the zip name the assignment requires
    python3 planning/package_tp2.py --quick         # packaged-copy check runs only the testbed

Checks: required deliverables are present with the exact names; dated files use real
YYYY-MM-DD dates; every video is under 100 MB; local-only folders (lib, build, data, bin) are
left out; and the packaged copy, unzipped into a temporary folder, compiles and passes the tests.
Exit status 0 means every check passed. The zip is written either way so it can be inspected.
"""
import argparse
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VIDEO = ('.mp4', '.mov', '.m4v')
LIMIT = 100_000_000                       # "under 100 MB", read strictly as decimal megabytes
EXCLUDED_DIRS = {'build', 'data', 'lib', 'bin', '__pycache__', '.settings_local'}
EXCLUDED_FILES = {'.DS_Store'}
REQUIRED = [
    ('AICoaching.PDF', 'Task 5'),
    ('TP2 Team Norms.pdf', 'Task 2'),
    ('Lessons_Learned_Javadocs/index.html', 'Task 3'),
    ('Contributor_Javadocs/index.html', 'Task 4'),
    ('ContributorTests_Javadocs/index.html', 'Task 7'),
    ('ContributorTests_Javadocs/Contributor_Requirements_and_Tests.html', 'Task 7.1'),
]
SCREENCASTS = [('LessonsLearnedScreencast', 'Task 9.1'), ('PhaseTwoGUIScreencast', 'Task 9.2'),
               ('TestingScreencast', 'Task 9.3')]
PLAN = re.compile(r'Plan and Status (\d{4}-\d{2}-\d{2})\.pdf')
SUMMARY = re.compile(r'Status Summary (\d{4}-\d{2}-\d{2})\.mp4')
CARRIED_OVER = ['TP1 Plan.pdf', 'TP1 Test Cases.pdf', 'TP1-Team53 Team Norms.pdf']

results = []


def report(ok, message):
    results.append(ok)
    print(('  PASS  ' if ok else '  FAIL  ') + message)


def included(path):
    relative = path.relative_to(ROOT)
    return (path.is_file() and not EXCLUDED_DIRS.intersection(relative.parts[:-1])
            and relative.name not in EXCLUDED_FILES and not relative.name.endswith('.class'))


def valid_date(text):
    try:
        date.fromisoformat(text)
        return True
    except ValueError:
        return False


def check_contents(files):
    names = {f.relative_to(ROOT).as_posix() for f in files}
    print('Required deliverables')
    for name, task in REQUIRED:
        report(name in names, f'{task}: {name}')
    for stem, task in SCREENCASTS:
        found = [n for n in names if Path(n).stem == stem and n.lower().endswith(VIDEO)]
        report(bool(found), f'{task}: {stem}.mp4' + (f' ({found[0]})' if found else ' not found'))
    plans = [f for f in files if PLAN.fullmatch(f.name)]
    report(bool(plans) and all(valid_date(PLAN.fullmatch(f.name).group(1)) for f in plans)
           and all(f.parent.name == 'TP2 Planning and Tracking' for f in plans),
           f'Task 8.1: {len(plans)} Plan and Status PDF(s) in TP2 Planning and Tracking')
    summaries = [f for f in files if SUMMARY.fullmatch(f.name)]
    report(len(summaries) >= 5 and all(valid_date(SUMMARY.fullmatch(f.name).group(1))
                                       for f in summaries),
           f'Task 8.2: {len(summaries)} Status Summary video(s); at least one per member (5)')
    print('Videos under 100 MB')
    videos = [f for f in files if f.suffix.lower() in VIDEO]
    if not videos:
        report(False, 'no videos found to measure')
    for f in videos:
        size = f.stat().st_size
        report(size < LIMIT, f'{f.relative_to(ROOT)}: {size / 1_000_000:.1f} MB')
    for name in CARRIED_OVER:
        if name in names:
            print(f'  NOTE  {name} is a TP1 file; remove it if TP2 should not include it')


def build_zip(files, output):
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
        for f in sorted(files):
            archive.write(f, Path('TP2') / f.relative_to(ROOT))
    print(f'Wrote {output} ({output.stat().st_size / 1_000_000:.1f} MB, {len(files)} files)')
    report(output.stat().st_size > 0, 'zip written')


def check_packaged_copy(output, quick):
    print('Packaged project runs')
    with tempfile.TemporaryDirectory(prefix='tp2-package-') as temp:
        with zipfile.ZipFile(output) as archive:
            archive.extractall(temp)
        copy = Path(temp) / 'TP2'
        report(not (copy / 'lib').exists() and not (copy / 'build').exists(),
               'local lib and build folders are not in the zip')
        # lib is platform-specific; reuse this computer's checked jars, else download them.
        if (ROOT / 'lib').exists():
            shutil.copytree(ROOT / 'lib', copy / 'lib')
        else:
            subprocess.run([sys.executable, 'setup_dependencies.py'], cwd=copy, check=True)
        command = [sys.executable, 'dev.py', 'test']
        if quick:
            command.append('testingAutomation.ContributorRequirementsTestbed')
        run = subprocess.run(command, cwd=copy, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                             text=True)
        last = [line for line in run.stdout.splitlines() if 'suites passed' in line]
        report(run.returncode == 0, 'packaged copy builds and passes: '
               + (last[-1] if last else run.stdout.strip().splitlines()[-1]))


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--name', default='TP2-Team53.zip')
    parser.add_argument('--quick', action='store_true')
    args = parser.parse_args()
    files = [f for f in ROOT.rglob('*') if included(f)]
    check_contents(files)
    output = ROOT / 'build' / 'package' / args.name
    build_zip(files, output)
    check_packaged_copy(output, args.quick)
    failed = results.count(False)
    print(f'\n{len(results) - failed} checks passed, {failed} failed: '
          + ('READY TO SUBMIT' if failed == 0 else 'NOT READY'))
    sys.exit(1 if failed else 0)


if __name__ == '__main__':
    main()
