import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
import ts from 'typescript';
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'upsc-locale-'));
try {
  for(const name of ['syllabus','planner','locale'])fs.writeFileSync(path.join(tmp,name+'.mjs'),ts.transpileModule(fs.readFileSync(`src/lib/${name}.ts`,'utf8').replace("from './syllabus'","from './syllabus.mjs'"),{compilerOptions:{module:ts.ModuleKind.ES2022,target:ts.ScriptTarget.ES2022}}).outputText);
  const {translator,dateLabel}=await import(pathToFileURL(path.join(tmp,'locale.mjs')));
  const {subjects,allTopics,phases,optionals,literature,optionalStarters}=await import(pathToFileURL(path.join(tmp,'syllabus.mjs')));
  const {defaultState,generateSchedule,mergeChanges,filterTopics,tasksForDate,habits}=await import(pathToFileURL(path.join(tmp,'planner.mjs')));
  const hi=translator('hi'),en=translator('en');
  const identities=new Set(['UPSC','RK','CSAT','The Hindu','The Indian Express']);
  const check=text=>{assert.equal(en(text),text);if(/[a-z]/i.test(text)&&!identities.has(text.trim()))assert.match(hi(text),/[\u0900-\u097F]/,`Missing Hindi: ${text}`);};
  for(const subject of subjects){for(const key of ['name','subtitle','book','foundation','method'])check(subject[key]);for(const topic of subject.topics){check(topic.title);check(topic.paper);}for(const resource of subject.resources){check(resource.title);check(resource.kind);}}
  for(const phase of phases)Object.values(phase).forEach(check);
  for(const value of [...optionals,...literature,...Object.values(optionalStarters),...habits.map(h=>h.title)])check(value);
  assert.equal(hi('Hindi Literature'),'हिंदी साहित्य');
  assert.equal(hi('{count} hours',{count:4}),'4 घंटे');
  assert.equal(en('{count} hours',{count:4}),'4 hours');
  assert.equal(hi('{complete} of {total} topics complete',{complete:2,total:102}),'102 में से 2 विषय पूरे');
  assert.match(dateLabel('2026-10-06',false,'hi'),/अक्टूबर/);
  assert.match(dateLabel('2026-10-06',false,'en'),/October/);
  const source=ts.createSourceFile('planner.tsx',fs.readFileSync('src/app/planner.tsx','utf8'),ts.ScriptTarget.Latest,true,ts.ScriptKind.TSX);
  function walk(node){
    if(ts.isCallExpression(node)&&node.expression.getText(source)==='tr'&&node.arguments[0]&&ts.isStringLiteral(node.arguments[0]))check(node.arguments[0].text);
    if(ts.isJsxText(node)&&/[a-z]/i.test(node.text))assert.ok(['English','Language / भाषा'].includes(node.text.trim()),`Untranslated UI text: ${node.text}`);
    ts.forEachChild(node,walk);
  }
  walk(source);
  const state=defaultState();state.settings.startDate='2026-10-06';state.progress['polity-1']='complete';state.sessions['2026-10-06-reading']=true;state.schedule=generateSchedule(state.settings,state.progress).schedule;
  const hindiState=mergeChanges(state,'settings',{language:'hi'});
  assert.deepEqual(hindiState.progress,state.progress);assert.deepEqual(hindiState.schedule,state.schedule);assert.deepEqual(hindiState.sessions,state.sessions);
  assert.deepEqual(generateSchedule(hindiState.settings,hindiState.progress),generateSchedule(state.settings,state.progress));
  assert.deepEqual(filterTopics('Prelims','polity','complete',hindiState.progress),filterTopics('Prelims','polity','complete',state.progress));
  assert.deepEqual(tasksForDate('2026-10-06',hindiState),tasksForDate('2026-10-06',state));
  assert.deepEqual(mergeChanges(hindiState,'settings',{language:'en'}),state);
  console.log(`PASS: Hindi/English UI coverage, ${allTopics.length} topic translations, books and methods, localized dates, templates, filters and identical schedules across languages`);
} finally {fs.rmSync(tmp,{recursive:true});}
