import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
import ts from 'typescript';
const temp=fs.mkdtempSync(path.join(os.tmpdir(),'upsc-tests-'));
for(const file of ['syllabus','planner']){let source=fs.readFileSync(`src/lib/${file}.ts`,'utf8').replace("from './syllabus'","from './syllabus.mjs'");fs.writeFileSync(path.join(temp,file+'.mjs'),ts.transpileModule(source,{compilerOptions:{target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.ES2022}}).outputText);}
const {allTopics,subjects,optionals,literature}=await import(pathToFileURL(path.join(temp,'syllabus.mjs')));
const {defaultState,addDays,filterTopics,progressSummary,generateSchedule,tasksForDate,habits}=await import(pathToFileURL(path.join(temp,'planner.mjs')));
assert.equal(new Set(allTopics.map(t=>t.id)).size,allTopics.length,'Topic IDs must be unique');
assert.equal(optionals.length,25);assert.equal(literature.length,23);
const state=defaultState();state.progress[allTopics[0].id]='complete';
assert.equal(progressSummary(state.progress).complete,1);
assert.equal(filterTopics('All','polity','complete',state.progress).length,1);
assert.equal(filterTopics('Interview','csat','All',state.progress).length,0);
assert(filterTopics('Prelims','All','All',{}).every(t=>t.stages.includes('Prelims')));
assert.equal(filterTopics('All','All','not-started',state.progress).length,allTopics.length-1);
assert.equal(addDays('2028-02-28',1),'2028-02-29');assert.equal(addDays('2026-12-31',1),'2027-01-01');
state.settings.startDate='2026-10-05';const result=generateSchedule(state.settings,state.progress);state.schedule=result.schedule;
assert.equal(result.schedule[allTopics[0].id],undefined,'Regeneration excludes completed topics');
assert.equal(Object.keys(result.schedule).length,allTopics.length-1);
let sessions=0;for(let i=0;i<result.neededDays;i++){const active=tasksForDate(addDays(state.settings.startDate,i),state);assert.equal(active.length,1,'Generated study sessions must not overlap or leave accidental gaps');sessions+=active.length;}
assert.equal(sessions,result.neededDays);
const optional=allTopics.find(t=>t.id==='optional-2');const begin=result.schedule[optional.id];assert(tasksForDate(addDays(begin,1),state).some(t=>t.id===optional.id),'Long topics continue on subsequent days');
assert.equal(tasksForDate(addDays(state.settings.startDate,-1),state).length,0);
const faster=generateSchedule({...state.settings,hours:8},state.progress);assert(faster.neededDays<result.neededDays,'More hours must shorten the schedule');
assert.equal(habits.reduce((n,h)=>n+h.share,0)+.65,1);
for(const s of subjects){assert(s.foundation&&s.book&&s.resources.length);for(const r of s.resources)assert.equal(new URL(r.url).protocol,'https:');}
console.log(`PASS: progress, combined filters, empty results, date boundaries, all ${allTopics.length} topics, optional catalogue, multi-day scheduling and time allocation`);
fs.rmSync(temp,{recursive:true});
