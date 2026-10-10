import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
import ts from 'typescript';
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'upsc-desk-'));
try {
 for(const name of ['syllabus','planner','study-guide'])fs.writeFileSync(path.join(tmp,name+'.mjs'),ts.transpileModule(fs.readFileSync(`src/lib/${name}.ts`,'utf8').replace("from './syllabus'","from './syllabus.mjs'"),{compilerOptions:{module:ts.ModuleKind.ES2022,target:ts.ScriptTarget.ES2022}}).outputText);
 const {defaultState,dailyFocus,validPracticeDrafts,practicePromptIds,countWords,timerRemaining}=await import(pathToFileURL(path.join(tmp,'planner.mjs')));
 const {allTopics}=await import(pathToFileURL(path.join(tmp,'syllabus.mjs')));
 const {starterWeeks,practicePrompts,preparationMethods,answerChecks}=await import(pathToFileURL(path.join(tmp,'study-guide.mjs')));
 const state=defaultState(),today='2026-10-08';
 assert.equal(dailyFocus(state,today).focus.id,'csat-8','New aspirants should diagnose CSAT before a long reading plan');
 state.progress['polity-1']='in-progress';
 assert.equal(dailyFocus(state,today).focus.id,'polity-1');
 state.schedule['geography-1']=today;
 assert.equal(dailyFocus(state,today).focus.id,'geography-1','An unfinished scheduled topic has priority');
 state.progress['geography-1']='complete';
 assert.equal(dailyFocus(state,today).focus.id,'polity-1','A completed scheduled topic is not recommended as new work');
 state.sessions['2026-10-02-reading']=true;state.sessions['2026-10-08-revision']=true;state.sessions['2026-10-08-practice']=true;state.sessions['2026-10-01-reading']=true;
 const desk=dailyFocus(state,today);
 assert.equal(desk.count,2);assert.equal(desk.week.length,7);assert.equal(desk.week.filter(d=>d.active).length,2);
 assert(desk.review.every(t=>state.progress[t.id]==='complete'));
 for(const t of allTopics)state.progress[t.id]='complete';
 assert.equal(dailyFocus(state,today).focus,undefined);assert.equal(dailyFocus(state,today).review.length,3);
 assert(validPracticeDrafts({constitution:'मेरा उत्तर',inflation:'My answer'}));assert(validPracticeDrafts({}));
 for(const value of [null,[],{constitution:1},{constructor:'text'},{constitution:'x'.repeat(5001)}])assert(!validPracticeDrafts(value));
 assert.equal(countWords('  भारत का संविधान\nनागरिक अधिकार  '),5);assert.equal(countWords('  '),0);assert.equal(countWords('one\ttwo\nthree'),3);
 assert.equal(timerRemaining(60000,30000),30);assert.equal(timerRemaining(60000,59999),1);assert.equal(timerRemaining(60000,60000),0);assert.equal(timerRemaining(60000,90000),0);
 assert.deepEqual(practicePrompts.map(p=>p.id),[...practicePromptIds]);assert.equal(starterWeeks.length,5);
 function checkCopy(value){if(value&&typeof value==='object'){if('en'in value){assert.match(value.en,/[a-z]/i);assert.match(value.hi,/[\u0900-\u097F]/);}for(const child of Object.values(value))checkCopy(child);}}
 [starterWeeks,practicePrompts,preparationMethods,answerChecks].forEach(checkCopy);
 for(const filename of ['src/components/study-desk.tsx','src/components/preparation-guide.tsx']){
  const source=ts.createSourceFile(filename,fs.readFileSync(filename,'utf8'),ts.ScriptTarget.Latest,true,ts.ScriptKind.TSX);
  function walk(node){if(ts.isCallExpression(node)&&node.expression.getText(source)==='text'&&node.arguments.length===2&&ts.isStringLiteral(node.arguments[0])&&ts.isStringLiteral(node.arguments[1])){assert.match(node.arguments[1].text,/[\u0900-\u097F]/,node.arguments[0].text);}if(ts.isJsxText(node)&&/[a-z]/i.test(node.text))throw new Error('Untranslated new interface text: '+node.text.trim());ts.forEachChild(node,walk);}walk(source);
 }
 console.log('PASS: real daily priorities, completed-topic exclusion, trailing-week activity, bilingual preparation content, draft validation, Hindi word count and timer boundaries');
} finally {fs.rmSync(tmp,{recursive:true});}
