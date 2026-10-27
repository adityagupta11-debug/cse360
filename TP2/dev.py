"""Portable TP2 build/test/run commands. Requires a JDK 21+ and setup_dependencies.py."""
from pathlib import Path
import argparse
import os
import shutil
import subprocess

ROOT = Path(__file__).resolve().parent
LIB = ROOT / 'lib'
CLASSES = ROOT / 'build/classes'
TESTS = ROOT / 'build/test-classes'
REPORTS = ROOT / 'build/reports'
SUITES = [
    'backendTests.BackendTests',
    'kanishTests.ModelTests',
    'kanishTests.CurrentRepoIntegrationTests',
    'kanishTests.CurrentRepoWindowTests',
    'testingAutomation.LessonTestingAutomation',
    'testingAutomation.LessonListTestingAutomation',
    'database.DeletionTests',
    'guiDeleteUser.DeletionViewTests',
    'guiUserLogin.LoginInputTests',
    'passwordPopUpWindow.DynamicPasswordViewTests',
    'guiUserUpdate.FirstUserReloginTests',
    'testingAutomation.TP1RegressionSuite',
]

def command(args):
    subprocess.run(args, cwd=ROOT, check=True)

def build():
    for jar in ['javafx-base.jar','javafx-graphics.jar','javafx-controls.jar','h2.jar']:
        if not (LIB / jar).exists():
            raise SystemExit('Run python3 setup_dependencies.py first.')
    # Avoid stale class files hiding a renamed or removed source class.
    if CLASSES.exists(): shutil.rmtree(CLASSES)
    CLASSES.mkdir(parents=True)
    command(['javac','-encoding','UTF-8','--release','21','--module-path',str(LIB),'-d',str(CLASSES)] + [str(p) for p in (ROOT/'src').rglob('*.java')])
    for p in (ROOT/'src').rglob('*.css'):
        dest=CLASSES/p.relative_to(ROOT/'src'); dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(p,dest)
    print('Application compiled successfully.', flush=True)

def compile_tests():
    if TESTS.exists(): shutil.rmtree(TESTS)
    TESTS.mkdir(parents=True)
    command(['javac','-encoding','UTF-8','--release','21','--module-path',str(LIB),'--add-modules','javafx.controls','-cp',os.pathsep.join([str(CLASSES),str(LIB/'h2.jar')]),'-d',str(TESTS)] + [str(p) for p in (ROOT/'tests').rglob('*.java')])

def java(suite):
    return ['java','--enable-native-access=javafx.graphics','--module-path',str(LIB),'--add-modules','javafx.controls','-cp',os.pathsep.join([str(TESTS),str(CLASSES),str(LIB/'h2.jar')]),suite]

def test(suites):
    compile_tests(); REPORTS.mkdir(parents=True,exist_ok=True)
    for suite in suites:
        result=subprocess.run(java(suite),cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=150)
        (REPORTS/(suite+'.txt')).write_text(result.stdout)
        print(result.stdout,flush=True)
        if result.returncode: raise SystemExit(f'Failed: {suite} (exit {result.returncode})')
        # Inherited testbeds originally printed failures but still returned zero.
        import re
        if re.search(r'(?:Number of tests failed:|Failed:)\s*[1-9]|\| FAIL|\*\*\*Failure',result.stdout):
            raise SystemExit('Failed assertions in '+suite)
    print(f'All {len(suites)} suites passed.',flush=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action',choices=['build','test','run'])
    parser.add_argument('suites',nargs='*')
    args=parser.parse_args()
    build()
    if args.action=='test': test(args.suites or SUITES)
    elif args.action=='run':
        command(['java','--enable-native-access=javafx.graphics','--module-path',os.pathsep.join([str(LIB),str(CLASSES)]),'-m','FoundationsF26/applicationMain.FoundationsMain'])
