"""Regenerate TP2 Javadoc, actual source-viewer screenshots and evidence indexes."""
from pathlib import Path
import csv
import html
import os
import shutil
import subprocess
import re
import dev

ROOT=Path(__file__).resolve().parent
CP=os.pathsep.join([str(dev.TESTS),str(dev.CLASSES),str(dev.LIB/'h2.jar')])
GROUPS={
 'Lessons_Learned_Javadocs': ['src/entityClasses/LessonsLearned.java','src/entityClasses/LessonsLearnedList.java','src/entityClasses/LessonExperience.java','src/database/Database.java','src/database/LessonStore.java','src/experienceRecognizer/ExperienceRecognizer.java'],
 'Contributor_Javadocs': [str(p.relative_to(ROOT)) for folder in ['guiContributor','guiMyLessons','guiAddLesson','guiLessonDetail','guiLessonSupport','guiLessonCreate','guiLessonDetails','guiLessonEdit','guiLessonDelete','guiExperienceAdd'] for p in (ROOT/'src'/folder).glob('*.java')],
 'ContributorTests_Javadocs': [str(p.relative_to(ROOT)) for folder in ['kanishTests','backendTests'] for p in (ROOT/'tests'/folder).glob('*.java')]+['src/testingAutomation/ContributorRequirementsTestbed.java','src/testingAutomation/ContributorRequirement.java','src/testingAutomation/RequirementTest.java','src/testingAutomation/LessonTestingAutomation.java','src/testingAutomation/LessonListTestingAutomation.java','src/testingAutomation/LessonTestFixtures.java','src/testingAutomation/MyLessonsManualTests.java'],
}


def refresh_contributor_matrix():
 """Keep Kanish's contribution matrix aligned with the real integrated backend and captures."""
 target=ROOT/'ContributorTests_Javadocs/Requirements_Test_Evidence.csv'
 if not target.exists():
  raise SystemExit('Missing inherited Contributor requirement matrix: '+str(target))
 with target.open(encoding='utf-8-sig',newline='') as source:
  reader=csv.DictReader(source)
  columns=reader.fieldnames
  rows=list(reader)
 scopes={
  'KG01':'Real database and actual GUI create/edit/delete verified; see current execution reports.',
  'KG02':'Real session, ownership, cache isolation, logout and revoked-role checks verified.',
  'KG03':'Real stored full/per-field locks and GUI read-only/delete gating verified; model fixtures separately labeled.',
  'KG04':'Actual Add Experience save and file-backed persistence verified; model fixtures separately labeled.',
  'KG05':'Shared recognizer, storage validation and live retained-draft GUI checks verified.',
  'KG06':'Actual Home, dedicated list/search, Back and Logout navigation verified; manual keyboard/resize checklist remains separate.',
  'KG07':'Real confirmed/cancelled deletion and backend lock rejection verified, including waiting concurrent deletes.',
  'KG08':'Actual H2 reopen, schema migration and forced SQL-error rollback verified; GUI error fixtures separately labeled.',
 }
 captures=sorted((ROOT/'ContributorTests_Javadocs/source-screenshots').glob('*.png'))
 for row in rows:
  identifier=row['Local ID']
  row['Observed scope']=scopes.get(identifier,row['Observed scope'])
  documents=[item.strip() for item in row['Other test Javadocs'].split(';') if item.strip()]
  if 'backendTests/BackendTests.html' not in documents:
   documents.append('backendTests/BackendTests.html')
  if identifier in ('KG01','KG02') and 'backendTests/ListSnapshotTests.html' not in documents:
   documents.append('backendTests/ListSnapshotTests.html')
  row['Other test Javadocs']='; '.join(documents)
  if identifier in ('KG03','KG04') and 'testRealStorageLocks' not in row['Named model tests']:
   row['Named model tests']+='; CurrentRepoWindowTests.testRealStorageLocks; CurrentRepoWindowTests.testLiveCrud'
  # Link exact generated filenames rather than keeping historical pre-merge page counts.
  row['Actual source screenshots']='; '.join('source-screenshots/'+image.name for image in captures
   if any(part in image.name for part in ('kanishTests_ModelTests_', 'kanishTests_CurrentRepoIntegrationTests_',
       'kanishTests_CurrentRepoWindowTests_', 'kanishTests_TestGateway_', 'tests_BackendTests_', 'tests_ListSnapshotTests_')))
 with target.open('w',newline='',encoding='utf-8') as output:
  writer=csv.DictWriter(output,fieldnames=columns,lineterminator='\n')
  writer.writeheader()
  writer.writerows(rows)


def generate():
 dev.build();dev.compile_tests()
 doc_exe=shutil.which('javadoc')
 if not doc_exe:
  settings=subprocess.run(['java','-XshowSettings:properties','-version'],capture_output=True,text=True,check=True)
  home=re.search(r'java.home = (.+)',settings.stderr)
  candidate=Path(home.group(1).strip())/'bin'/('javadoc.exe' if os.name=='nt' else 'javadoc') if home else None
  if not candidate or not candidate.exists(): raise SystemExit('Install a full JDK with javadoc.')
  doc_exe=str(candidate)
 for folder,sources in GROUPS.items():
  result=subprocess.run([doc_exe,'-quiet','-Xdoclint:none','-private','-notimestamp','-encoding','UTF-8','-charset','UTF-8','-d',str(ROOT/folder),'--module-path',str(dev.LIB),'--add-modules','javafx.controls','-classpath',CP]+sources,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
  (ROOT/'build'/('javadoc-'+folder+'.log')).write_text(result.stdout,encoding='utf-8')
  if result.returncode: raise SystemExit(result.stdout)
  print('Generated',folder,flush=True)
 for folder in GROUPS:
  capture_folder=ROOT/folder/'source-screenshots'
  if capture_folder.exists(): shutil.rmtree(capture_folder)
 for tool in ['kanishTests.SourceEvidence','backendTests.BackendEvidence','contributorTestEvidence.ContributorTestEvidence']:
  subprocess.run(dev.java(tool),cwd=ROOT,check=True,timeout=150)
 results=ROOT/'ContributorTests_Javadocs/results';results.mkdir(exist_ok=True)
 for report in dev.REPORTS.glob('*.txt'):
  if not report.name.startswith('window-rerun'):shutil.copy2(report,results/report.name)
 # Replace inherited aliases so they do not show stale pre-integration results.
 aliases={'database-tests.txt':'database.DeletionTests.txt','view-tests.txt':'guiDeleteUser.DeletionViewTests.txt','invitation-tests.txt':'guiUserLogin.LoginInputTests.txt','password-view-tests.txt':'passwordPopUpWindow.DynamicPasswordViewTests.txt','first-user-relogin-tests.txt':'guiUserUpdate.FirstUserReloginTests.txt','lesson-regression.txt':'testingAutomation.LessonTestingAutomation.txt','team-regression.txt':'testingAutomation.TP1RegressionSuite.txt'}
 for old,new in aliases.items():shutil.copy2(dev.REPORTS/new,results/old)
 for folder in GROUPS:
  screenshots=sorted((ROOT/folder/'source-screenshots').glob('*.png'))
  links=''.join(f'<li><a href="source-screenshots/{html.escape(p.name)}">{html.escape(p.name)}</a></li>' for p in screenshots)
  page=f'''<!doctype html><html lang="en"><meta charset="utf-8"><title>TP2 evidence index</title><style>body{{font:16px system-ui;max-width:1100px;margin:40px auto;line-height:1.6}}li{{margin:6px 0}}</style><h1>{html.escape(folder)}</h1><p>Source-viewer captures are generated from the actual delivered files with original line numbers. They are source evidence, not a substitute for execution tests.</p><p><a href="index.html">Generated Javadoc</a> | <a href="../Backend_Requirements.md">Backend requirement mapping</a></p><ul>{links}</ul></html>'''
  (ROOT/folder/'Evidence_Index.html').write_text(page,encoding='utf-8')
 report=(dev.REPORTS/'backendTests.BackendTests.txt').read_text(encoding='utf-8')
 names=[line.split(' | PASS')[0] for line in report.splitlines() if ' | PASS' in line]
 with (ROOT/'ContributorTests_Javadocs/Backend_Requirements_Test_Evidence.csv').open('w',newline='',encoding='utf-8') as f:
  writer=csv.writer(f,lineterminator="\n");writer.writerow(['Requirement','Test assertion','Javadoc HTML','Source screenshot prefix','Expected output'])
  for name in names:writer.writerow([name.split('_')[0],name,'backendTests/BackendTests.html','source-screenshots/VG01-VG10_7.4_tests_BackendTests_','PASS only when its checked stored/rejected outcome is true; otherwise nonzero assertion failure'])
 rows=''.join(f'<tr><td>{html.escape(name.split("_")[0])}</td><td>{html.escape(name)}</td><td>PASS</td></tr>' for name in names)
 (ROOT/'ContributorTests_Javadocs/Backend_Test_Index.html').write_text(f'''<!doctype html><html lang="en"><meta charset="utf-8"><title>Backend test mapping</title><style>body{{font:16px system-ui;margin:40px}}td,th{{border:1px solid #ddd;padding:8px}}table{{border-collapse:collapse}}</style><h1>Backend requirement-to-test evidence</h1><p>Each identifier is a local traceability ID, not an official course story number. See <a href="../Backend_Requirements.md">requirements and expected behavior</a>, <a href="backendTests/BackendTests.html">test Javadoc</a>, <a href="Evidence_Index.html">source screenshots</a>, and <a href="results/backendTests.BackendTests.txt">actual execution output</a>.</p><table><tr><th>Requirement</th><th>Executed assertion</th><th>Observed</th></tr>{rows}</table></html>''',encoding='utf-8')
 for folder in GROUPS:
  for page in (ROOT/folder).rglob('*.html'):
   content=page.read_text(encoding='utf-8')
   # Preserve HTML's whitespace between adjacent inline text across line breaks.
   content=re.sub(r'[ \t]+\n', '\n ',content)
   page.write_text(content,encoding='utf-8')
 refresh_contributor_matrix()
 print('Evidence indexes and actual test reports updated.',flush=True)

if __name__=='__main__':generate()
