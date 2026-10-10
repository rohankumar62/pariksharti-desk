import { allTopics, subjects } from './syllabus';
export type Settings={startDate:string;months:number;hours:number;optional:string;language:'en'|'hi';practiceDrafts:Record<string,string>};
export type PlannerState={progress:Record<string,string>;schedule:Record<string,string>;sessions:Record<string,boolean>;settings:Settings};
export function indiaToday(){return new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Kolkata'}).format(new Date());}
export function defaultState():PlannerState{return {progress:{},schedule:{},sessions:{},settings:{startDate:indiaToday(),months:12,hours:4,optional:'Undecided',language:'en',practiceDrafts:{}}};}
export function addDays(date:string,n:number){const d=new Date(date+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+n);return d.toISOString().slice(0,10);}
export function filterTopics(stage:string,subject:string,status:string,progress:Record<string,string>){return allTopics.filter(t=>(stage==='All'||t.stages.includes(stage as never))&&(subject==='All'||t.subject===subject)&&(status==='All'||(progress[t.id]??'not-started')===status));}
export function progressSummary(progress:Record<string,string>,ids=allTopics.map(t=>t.id)){const complete=ids.filter(id=>progress[id]==='complete').length;return {complete,total:ids.length,percent:ids.length?Math.round(100*complete/ids.length):0};}
export function generateSchedule(settings:Settings,progress:Record<string,string>){const schedule:Record<string,string>={};let used=0;const focusedHours=settings.hours*0.65;const priority=['csat-8','optional-1','polity-1','polity-2'];const ordered=[...priority.map(id=>allTopics.find(t=>t.id===id)!),...allTopics.filter(t=>!priority.includes(t.id))];const remaining=ordered.filter(t=>progress[t.id]!=='complete');for(const t of remaining){schedule[t.id]=addDays(settings.startDate,used);used+=Math.ceil(t.hours/focusedHours);}return {schedule,neededDays:used,availableDays:settings.months*30,focusedHours,totalHours:remaining.reduce((n,t)=>n+t.hours,0)};}
export function tasksForDate(date:string,state:PlannerState){return allTopics.filter(t=>{const start=state.schedule[t.id];if(!start)return false;const end=addDays(start,Math.ceil(t.hours/(state.settings.hours*.65))-1);return date>=start&&date<=end;});}
export function subjectProgress(id:string,state:PlannerState){return progressSummary(state.progress,subjects.find(s=>s.id===id)?.topics.map(t=>t.id)??[]);}
export const habits=[{id:'reading',title:'Current affairs & issue notes',share:0.15},{id:'revision',title:'Active recall & revision',share:0.1},{id:'practice',title:'PYQs / CSAT / answer writing',share:0.1}];

export function topicEndDate(id:string,state:PlannerState){const t=allTopics.find(t=>t.id===id);const start=state.schedule[id];return t&&start?addDays(start,Math.ceil(t.hours/(state.settings.hours*.65))-1):'';}
export function mergeChanges(prev:PlannerState,field:keyof PlannerState,patch:Record<string,unknown>):PlannerState{const next={...prev[field]} as Record<string,unknown>;for(const [id,value] of Object.entries(patch)){if(value===null)delete next[id];else if(field==='settings'&&id==='practiceDrafts')next[id]={...prev.settings.practiceDrafts,...value as Record<string,string>};else next[id]=value;}return {...prev,[field]:next};}

export const practicePromptIds=['constitution','inflation','urbanisation','wetlands','ethics','monsoon'] as const;
export function validPracticeDrafts(value:unknown){return !!value&&typeof value==='object'&&!Array.isArray(value)&&Object.entries(value).every(([id,text])=>practicePromptIds.includes(id as typeof practicePromptIds[number])&&typeof text==='string'&&text.length<=5000);}
export function dailyFocus(state:PlannerState,date=indiaToday()){
 const scheduled=tasksForDate(date,state).filter(t=>state.progress[t.id]!=='complete');
 const active=allTopics.filter(t=>state.progress[t.id]==='in-progress');
 const priority=['csat-8','optional-1','polity-1','polity-2'].map(id=>allTopics.find(t=>t.id===id)!).filter(t=>state.progress[t.id]!=='complete');
 const focus=scheduled[0]??active[0]??priority[0]??allTopics.find(t=>state.progress[t.id]!=='complete');
 const completed=allTopics.filter(t=>state.progress[t.id]==='complete');
 const count=habits.filter(h=>state.sessions[`${date}-${h.id}`]).length;
 const week=Array.from({length:7},(_,i)=>{const day=addDays(date,i-6);return {date:day,active:habits.some(h=>state.sessions[`${day}-${h.id}`])};});
 return {focus,scheduled,review:completed.slice(-3),count,week};
}
export function countWords(text:string){return text.trim()?text.trim().split(/\s+/u).length:0;}
export function timerRemaining(deadline:number,now=Date.now()){return Math.max(0,Math.ceil((deadline-now)/1000));}
