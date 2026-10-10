'use client';
import { useEffect,useState } from 'react';
import { ArrowUpRight,BookOpen,CheckCircle2,Clock3,Compass,Flag,Pause,Play,RotateCcw,Sparkles,Target } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { useLocale } from '@/lib/locale-context';
import { dateLabel } from '@/lib/locale';
import { dailyFocus,habits,indiaToday,progressSummary,timerRemaining,type PlannerState } from '@/lib/planner';

export function DailyDesk({state,loaded,busy,onTopic,onPlan,onGuide,onHabit}:{state:PlannerState;loaded:boolean;busy:boolean;onTopic:(subject:string)=>void;onPlan:()=>void;onGuide:()=>void;onHabit:(id:string,value:boolean)=>void}){
 const {language,t:tr}=useLocale();
 const text=(en:string,hi:string)=>language==='hi'?hi:en;
 const [today,setToday]=useState(indiaToday);
 useEffect(()=>{const id=window.setInterval(()=>setToday(indiaToday()),60000);return()=>window.clearInterval(id);},[]);
 const daily=dailyFocus(state,today),summary=progressSummary(state.progress);
 return <div className="daily-desk">
  <section className="mission-hero">
   <div className="mission-copy"><span className="mission-label"><span className="status-dot"/>{text('YOUR PREPARATION, IN FOCUS','आपकी तैयारी, एकाग्रता के साथ')}</span>
    <h2>{text('Small steps.','छोटे कदम।')}<br/><span>{text('A stronger tomorrow.','बेहतर कल।')}</span></h2>
    <p>{text('Know your next topic, keep revision in the loop, and finish one useful session today.','अगला विषय जानें, दोहराई जारी रखें और आज एक उपयोगी अध्ययन सत्र पूरा करें।')}</p>
    <div className="mission-actions"><Button onClick={()=>daily.focus?onTopic(daily.focus.subject):onGuide()}><Play size={16}/>{text('Start today’s session','आज का सत्र शुरू करें')}</Button><Button variant="outline" onClick={onPlan}>{text('Adjust my plan','मेरी योजना सुधारें')}<ArrowUpRight size={16}/></Button></div>
    <div className="mission-meta"><span><Clock3 size={14}/>{state.settings.hours} {text('hours / day','घंटे / दिन')}</span><span><Flag size={14}/>{state.settings.months} {text('month horizon','माह की योजना')}</span></div>
   </div>
   <div className="mission-orbit" aria-label={text('Syllabus checklist progress','पाठ्यक्रम चेकलिस्ट की प्रगति')}>
    <div className="orbit-ring" style={{'--orbit-progress':summary.percent+'%'} as React.CSSProperties}><div><span>{text('FIRST-PASS PROGRESS','पहली पढ़ाई की प्रगति')}</span><strong>{summary.percent}<small>%</small></strong><p>{summary.complete} / {summary.total} {text('topics','विषय')}</p></div></div>
    <span className="orbit-note">{text('Understanding → recall → practice','समझ → स्मरण → अभ्यास')}</span>
   </div>
  </section>
  <section className="desk-metrics" aria-label={text('Daily preparation overview','दैनिक तैयारी का सार')}>
   <div><span>{text('TODAY','आज')}</span><strong>{dateLabel(today,true,language)}</strong><small>{text('India standard time','भारतीय मानक समय')}</small></div>
   <div><span>{text('DAILY ROUTINE','दैनिक अभ्यास')}</span><strong>{daily.count}<small> / 3</small></strong><small>{text('Checklists completed','पूरे किए दैनिक कार्य')}</small></div>
   <div><span>{text('LAST 7 DAYS','पिछले 7 दिन')}</span><strong>{daily.week.filter(d=>d.active).length}<small> / 7</small></strong><small>{text('Days with a checked activity','कार्य चिह्नित करने वाले दिन')}</small></div>
   <div><span>{text('YOUR OPTIONAL','आपका वैकल्पिक विषय')}</span><strong className="optional-value">{state.settings.optional==='Undecided'?text('Choose thoughtfully','सोच-समझकर चुनें'):tr(state.settings.optional)}</strong><button onClick={onPlan}>{text('Review your choice','अपना चयन देखें')}<ArrowUpRight size={13}/></button></div>
  </section>
  <div className="desk-work-grid">
   <section className="panel focus-topic"><div className="desk-section-title"><div><span className="eyebrow">{text('YOUR NEXT USEFUL STEP','आपका अगला उपयोगी कदम')}</span><h2>{text('What to study today','आज क्या पढ़ें')}</h2></div><BookOpen size={21}/></div>
    {daily.focus ? <><span className="desk-paper">{tr(daily.focus.subjectName)} · {tr(daily.focus.paper)}</span><h3>{tr(daily.focus.title)}</h3><p>{daily.scheduled.length?text('This unfinished topic is scheduled for today. Read one section, explain it from memory, then practise relevant questions.','यह अधूरा विषय आज की योजना में है। एक हिस्सा पढ़ें, याद से समझाएं और संबंधित प्रश्नों का अभ्यास करें।'):text('No unfinished topic is scheduled for today. This suggestion prioritises active work, a CSAT diagnostic and your foundations.','आज कोई अधूरा विषय निर्धारित नहीं है। यह सुझाव चल रहे विषय, CSAT स्तर जाँच और बुनियाद को प्राथमिकता देता है।')}</p>
     <div className="study-loop"><span>{text('Read','पढ़ें')}</span><Chevron/><span>{text('Recall','दोहराएं')}</span><Chevron/><span>{text('Practise','अभ्यास करें')}</span></div>
     <Button variant="outline" onClick={()=>onTopic(daily.focus!.subject)}>{text('Open subject checklist','विषय की चेकलिस्ट खोलें')}<ArrowUpRight size={15}/></Button></>:
     <><h3>{text('Your first pass is complete. Keep building depth.','पहली पढ़ाई पूरी है। अब समझ और मजबूत करें।')}</h3><p>{text('Revisit weak areas and practise timed papers. Topic completion is a record of your work, not a prediction of selection.','कमज़ोर हिस्से दोहराएं और समयबद्ध प्रश्नपत्र हल करें। विषय पूरा करना आपके काम का रिकॉर्ड है, चयन की भविष्यवाणी नहीं।')}</p><Button onClick={onGuide}>{text('Open preparation guide','तैयारी गाइड खोलें')}</Button></>}
   </section>
   <FocusTimer/>
   <section className="panel routine-panel"><div className="desk-section-title"><div><span className="eyebrow">{text('PROTECT THE BASICS','बुनियादी अभ्यास जारी रखें')}</span><h2>{text('Your daily routine','आपका दैनिक अभ्यास')}</h2></div><CheckCircle2 size={21}/></div>
    {habits.map(h=><label key={h.id} className="desk-habit"><Checkbox aria-label={tr(h.title)} disabled={!loaded||busy} checked={!!state.sessions[`${today}-${h.id}`]} onCheckedChange={v=>onHabit(h.id,!!v)}/><span><strong>{tr(h.title)}</strong><small>{Math.round(state.settings.hours*60*h.share)} {text('suggested minutes','सुझाए गए मिनट')}</small></span><span className="habit-state">{state.sessions[`${today}-${h.id}`]?text('Done','पूरा'):text('To do','करना है')}</span></label>)}
    <div className="consistency-week">{daily.week.map(d=><span key={d.date} title={dateLabel(d.date,false,language)} className={d.active?'active':''}><span>{new Date(d.date+'T12:00:00Z').toLocaleDateString(language==='hi'?'hi-IN':'en-IN',{weekday:'narrow',timeZone:'Asia/Kolkata'})}</span><i/></span>)}</div>
    <p className="desk-caption">{text('Check an activity after doing the work. Your routine is saved with your planner.','काम पूरा होने पर ही चिह्नित करें। दैनिक अभ्यास आपकी योजना के साथ सहेजा जाता है।')}</p>
   </section>
   <section className="panel recall-panel"><div className="desk-section-title"><div><span className="eyebrow">{text('KEEP KNOWLEDGE AVAILABLE','सीखी बातों को याद रखें')}</span><h2>{text('Recall before you reread','दोबारा पढ़ने से पहले याद करें')}</h2></div><RotateCcw size={21}/></div>
    <p>{text('Close your notes. Explain the idea, write a short outline and check what you missed.','नोट्स बंद करें। विचार समझाएं, छोटी रूपरेखा लिखें और देखें क्या छूट गया।')}</p>
    {daily.review.length ? <div className="recall-list">{daily.review.map(t=><button key={t.id} onClick={()=>onTopic(t.subject)}><span><small>{tr(t.subjectName)}</small>{tr(t.title)}</span><ArrowUpRight size={16}/></button>)}</div>:<div className="recall-empty"><Target size={25}/><p>{text('Completed topics will appear here as recall suggestions. Start your first topic, then revisit it later.','पूरे किए विषय यहाँ दोहराई के सुझावों में दिखेंगे। पहला विषय पढ़ें और बाद में फिर याद करके देखें।')}</p></div>}
    <button className="text-button" onClick={onGuide}>{text('See revision and writing methods','दोहराई और उत्तर लेखन के तरीके देखें')}<ArrowUpRight size={14}/></button>
   </section>
  </div>
  <section className="guide-teaser"><div className="guide-teaser-icon"><Compass size={25}/></div><div><span className="eyebrow">{text('START WITH CLARITY','स्पष्टता के साथ शुरू करें')}</span><h3>{text('Your first 30 days, mapped out.','आपके पहले 30 दिन, स्पष्ट रूपरेखा के साथ।')}</h3><p>{text('A beginner sequence, original answer-writing exercises and practical study methods in your language.','आपकी भाषा में शुरुआती क्रम, मौलिक उत्तर-लेखन अभ्यास और व्यावहारिक अध्ययन तरीके।')}</p></div><Button onClick={onGuide}>{text('Explore the guide','गाइड देखें')}<ArrowUpRight size={16}/></Button></section>
 </div>;
}
function Chevron(){return <span className="loop-arrow" aria-hidden="true">→</span>;}
function FocusTimer(){
 const {language}=useLocale();const text=(en:string,hi:string)=>language==='hi'?hi:en;
 const [duration,setDuration]=useState(25*60),[remaining,setRemaining]=useState(25*60),[deadline,setDeadline]=useState<number|null>(null),[finished,setFinished]=useState(false);
 useEffect(()=>{if(deadline===null)return;const tick=()=>{const left=timerRemaining(deadline);setRemaining(left);if(left===0){setDeadline(null);setFinished(true);}};tick();const interval=window.setInterval(tick,1000);return()=>window.clearInterval(interval);},[deadline]);
 function reset(seconds=duration){setDeadline(null);setDuration(seconds);setRemaining(seconds);setFinished(false);}
 function toggle(){if(deadline!==null){setRemaining(timerRemaining(deadline));setDeadline(null);}else if(remaining>0){setDeadline(Date.now()+remaining*1000);setFinished(false);}}
 const clock=String(Math.floor(remaining/60)).padStart(2,'0')+':'+String(remaining%60).padStart(2,'0');
 return <section className="panel focus-timer"><div className="desk-section-title"><div><span className="eyebrow">{text('ONE THING AT A TIME','एक समय में एक काम')}</span><h2>{text('Focus session','एकाग्र अध्ययन सत्र')}</h2></div><Sparkles size={20}/></div>
  <div className="timer-presets">{[25,50].map(minutes=><button key={minutes} disabled={deadline!==null} aria-pressed={duration===minutes*60} onClick={()=>reset(minutes*60)}>{minutes} {text('min','मिनट')}</button>)}</div>
  <div className="focus-clock" role="timer" aria-label={text('Time remaining','बचा समय')}>{clock}</div>
  <p className="timer-status" aria-live="polite">{finished?text('Session complete. Take a break, then recall what you learned.','सत्र पूरा। थोड़ा विराम लें, फिर सीखी बात याद करके बताएं।'):deadline!==null?text('Your next topic has your full attention.','अपने अगले विषय पर पूरा ध्यान दें।'):text('Choose one small, clear study target.','एक छोटा और स्पष्ट अध्ययन लक्ष्य चुनें।')}</p>
  <div className="timer-actions"><Button onClick={toggle} disabled={remaining===0}>{deadline!==null?<Pause size={16}/>:<Play size={16}/>} {deadline!==null?text('Pause','रोकें'):remaining===duration?text('Start focus','सत्र शुरू करें'):text('Resume','जारी रखें')}</Button><Button variant="outline" size="icon" aria-label={text('Reset timer','टाइमर रीसेट करें')} onClick={()=>reset()}><RotateCcw size={16}/></Button></div>
  <p className="desk-caption">{text('Timer runs in this tab. Mark your study work separately in the daily checklist.','टाइमर इसी टैब में चलता है। अपना अध्ययन कार्य दैनिक चेकलिस्ट में अलग से चिह्नित करें।')}</p>
 </section>;
}
