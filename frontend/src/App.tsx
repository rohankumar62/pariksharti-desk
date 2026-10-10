import {useEffect, useState, type FormEvent} from 'react';
import {BookOpen, ShieldCheck, ArrowRight, Target, Timer, UserRound, LoaderCircle} from 'lucide-react';
import {toast, Toaster} from 'sonner';
import Planner from './app/planner';
import {api, ApiError, resetCsrf} from './lib/api';
import {AuthContext, type Profile} from './lib/auth-context';
import {Button} from './components/ui/button';
import {Input} from './components/ui/input';
import {Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription} from './components/ui/sheet';

type Access = {registrationEnabled: boolean; inviteRequired: boolean};
type Language = 'en' | 'hi';
const errorHindi: Record<string, string> = {
  INVALID_CREDENTIALS: 'ईमेल या पासवर्ड सही नहीं है।', EMAIL_EXISTS: 'इस ईमेल से खाता पहले से है। साइन इन करें।',
  INVITE_REQUIRED: 'सही आमंत्रण कोड डालें।', REGISTRATION_CLOSED: 'अभी नए खाते नहीं बनाए जा सकते।',
  LOGIN_LIMIT: 'बहुत अधिक असफल प्रयास हुए। 15 मिनट बाद कोशिश करें।',
  INVALID_INPUT: 'अपने भरे हुए विवरण जाँचें। पासवर्ड कम से कम 10 अक्षरों का होना चाहिए।',
  FORBIDDEN: 'पेज रीफ्रेश करके फिर कोशिश करें।', AUTH_REQUIRED: 'जारी रखने के लिए साइन इन करें।',
};
function message(error: unknown, language: Language) {
  if (language === 'hi') return error instanceof ApiError ? errorHindi[error.code] ?? 'अनुरोध पूरा नहीं हुआ। फिर कोशिश करें।' : 'कनेक्शन नहीं बना। जाँचें कि backend चल रहा है।';
  return error instanceof Error ? error.message : 'Please try again.';
}

function SignIn({access, onLogin}: {access: Access; onLogin: (user: Profile, isNew: boolean) => void}) {
  const [language, setLanguage] = useState<Language>(() => localStorage.getItem('desk-language') === 'hi' ? 'hi' : 'en');
  const [register, setRegister] = useState(false), [busy, setBusy] = useState(false), [error, setError] = useState('');
  const [form, setForm] = useState({fullName: '', email: '', password: '', confirm: '', inviteCode: ''});
  const text = (en: string, hi: string) => language === 'hi' ? hi : en;
  useEffect(() => { document.documentElement.lang = language; localStorage.setItem('desk-language', language); }, [language]);
  async function submit(event: FormEvent) {
    event.preventDefault(); setError('');
    if (register && form.password !== form.confirm) { setError(text('Passwords do not match.', 'दोनों पासवर्ड एक जैसे रखें।')); return; }
    if (register && new TextEncoder().encode(form.password).length > 72) { setError(text('Use a password within 72 UTF-8 bytes.', 'पासवर्ड 72 UTF-8 bytes से छोटा रखें।')); return; }
    setBusy(true);
    try {
      if (register) await api('/api/auth/register', 'POST', {fullName: form.fullName, email: form.email.trim(), password: form.password, inviteCode: form.inviteCode});
      const user = await api<Profile>('/api/auth/login', 'POST', {email: form.email.trim(), password: form.password});
      resetCsrf();
      if (register) await api('/api/planner', 'PATCH', {field: 'settings', patch: {language}});
      onLogin(user, register);
    } catch (e) { setError(message(e, language)); } finally { setBusy(false); }
  }
  return <main className="account-page"><div className="account-top"><a className="account-brand" href="/"><BookOpen/><span>PARIKSHARTI<small>PREPARATION DESK</small></span></a>
    <select aria-label="Language / भाषा" value={language} onChange={e => setLanguage(e.target.value as Language)}><option value="en">English</option><option value="hi">हिन्दी</option></select></div>
    <div className="account-layout"><section className="account-story"><div className="eyebrow">{text('A LITTLE BETTER. EVERY DAY.', 'हर दिन, थोड़ा आगे।')}</div>
      <h1>{text('Your ambition.', 'आपका लक्ष्य।')}<br/><span>{text('A clear direction.', 'एक साफ़ रास्ता।')}</span></h1>
      <p>{text('A personal workspace for the long journey to UPSC. Build your foundations, focus on today and turn steady practice into progress.', 'UPSC की लंबी तैयारी के लिए आपका अपना कार्यक्षेत्र। बुनियाद मजबूत करें, आज की पढ़ाई पर ध्यान दें और नियमित अभ्यास से आगे बढ़ें।')}</p>
      <div className="account-orbit" aria-hidden="true"><div><BookOpen size={38}/><strong>ONE DAY<br/>AT A TIME</strong></div><span className="orbit-dot"/></div>
      <div className="account-benefits"><div><Target/><span>{text('102 topics. One complete overview.', '102 विषय बिंदु। तैयारी का पूरा नक्शा।')}</span></div><div><Timer/><span>{text('A daily desk, focus timer and study plan.', 'दैनिक पढ़ाई, फोकस टाइमर और स्टडी प्लान।')}</span></div><div><ShieldCheck/><span>{text('Your notes and progress belong to your account.', 'आपके नोट्स और प्रगति आपके खाते में सुरक्षित।')}</span></div></div>
    </section><section className="account-card"><div className="account-card-icon"><UserRound/></div><h2>{register ? text('Create your account', 'अपना खाता बनाएँ') : text('Welcome to your desk', 'अपने डेस्क पर आपका स्वागत है')}</h2>
      <p>{register ? text('Start with a name and email. Complete your profile next.', 'नाम और ईमेल से शुरू करें। फिर अपनी प्रोफाइल पूरी करें।') : text('Sign in to continue your preparation.', 'अपनी तैयारी जारी रखने के लिए साइन इन करें।')}</p>
      <form onSubmit={submit}>
        {register && <label>{text('Full name', 'पूरा नाम')}<Input autoComplete="name" required maxLength={100} value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})}/></label>}
        <label>{text('Email address', 'ईमेल')}<Input type="email" autoComplete="username" required maxLength={254} value={form.email} onChange={e => setForm({...form, email: e.target.value})}/></label>
        <label>{text('Password', 'पासवर्ड')}<Input type="password" autoComplete={register ? 'new-password' : 'current-password'} required minLength={register ? 10 : undefined} maxLength={72} value={form.password} onChange={e => setForm({...form, password: e.target.value})}/>{register && <small>{text('At least 10 characters. Choose a unique password.', 'कम से कम 10 अक्षर। अलग और मजबूत पासवर्ड रखें।')}</small>}</label>
        {register && <label>{text('Confirm password', 'पासवर्ड फिर डालें')}<Input type="password" autoComplete="new-password" required maxLength={72} value={form.confirm} onChange={e => setForm({...form, confirm: e.target.value})}/></label>}
        {register && access.inviteRequired && <label>{text('Invitation code', 'आमंत्रण कोड')}<Input type="password" autoComplete="off" required maxLength={200} value={form.inviteCode} onChange={e => setForm({...form, inviteCode: e.target.value})}/><small>{text('Ask the workspace owner for a code.', 'कार्यक्षेत्र के मालिक से कोड लें।')}</small></label>}
        {error && <div className="account-error" role="alert">{error}</div>}
        <Button type="submit" className="account-submit" disabled={busy}>{busy ? <LoaderCircle className="animate-spin"/> : <ArrowRight/>}{register ? text('Create account & start', 'खाता बनाएँ और शुरू करें') : text('Sign in', 'साइन इन करें')}</Button>
      </form>
      {access.registrationEnabled ? <button className="account-switch" type="button" disabled={busy} onClick={() => {setRegister(!register); setError(''); setForm({...form, password: '', confirm: ''});}}>{register ? text('Already have an account? Sign in', 'पहले से खाता है? साइन इन करें') : text('New here? Create an account', 'नए हैं? अपना खाता बनाएँ')}</button> : <p className="account-footnote">{text('New registrations are currently closed.', 'अभी नए खाते नहीं बनाए जा सकते।')}</p>}
      <div className="account-footnote"><ShieldCheck size={15}/>{text('A separate study workspace for every learner.', 'हर परीक्षार्थी के लिए अलग अध्ययन कार्यक्षेत्र।')}</div>
    </section></div><footer className="account-footer">{text('Consistency over intensity. Read · Recall · Practise.', 'नियमितता से आगे बढ़ें। पढ़ें · याद करें · अभ्यास करें।')}</footer></main>;
}

function ProfileSheet({user, open, onClose, onSave}: {user: Profile; open: boolean; onClose: () => void; onSave: (user: Profile) => void}) {
  const [draft, setDraft] = useState(user), [busy, setBusy] = useState(false), [error, setError] = useState('');
  const language: Language = document.documentElement.lang === 'hi' ? 'hi' : 'en';
  const text = (en: string, hi: string) => language === 'hi' ? hi : en;
  useEffect(() => { if (open) {setDraft(user); setError('');} }, [open, user]);
  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('');
    try {
      const updated = await api<Profile>('/api/profile', 'PUT', {fullName: draft.fullName, city: draft.city, bio: draft.bio, targetYear: draft.targetYear});
      onSave(updated); toast.success(text('Profile saved', 'प्रोफाइल सेव हो गई')); onClose();
    } catch (e) {setError(message(e, language));} finally {setBusy(false);}
  }
  const year = new Date().getFullYear();
  return <Sheet open={open} onOpenChange={value => {if (!value && !busy) onClose();}}><SheetContent className="profile-sheet"><SheetHeader><SheetTitle>{text('Your learner profile', 'आपकी अध्ययन प्रोफाइल')}</SheetTitle><SheetDescription>{text('Make this workspace your own.', 'इस कार्यक्षेत्र को अपना बनाएँ।')}</SheetDescription></SheetHeader>
    <form className="profile-form" onSubmit={submit}><div className="profile-avatar">{user.fullName.trim().split(/\s+/).slice(0, 2).map(n => n[0]).join('').toUpperCase()}</div>
      <label>{text('Full name', 'पूरा नाम')}<Input required maxLength={100} autoComplete="name" value={draft.fullName} onChange={e => setDraft({...draft, fullName: e.target.value})}/></label>
      <label>{text('Email', 'ईमेल')}<Input readOnly type="email" value={user.email}/><small>{text('Your sign-in email.', 'इस ईमेल से साइन इन करें।')}</small></label>
      <label>{text('City (optional)', 'शहर (वैकल्पिक)')}<Input maxLength={80} autoComplete="address-level2" value={draft.city} onChange={e => setDraft({...draft, city: e.target.value})}/></label>
      <label>{text('Target attempt', 'लक्षित प्रयास')}<select value={draft.targetYear ?? ''} onChange={e => setDraft({...draft, targetYear: e.target.value ? Number(e.target.value) : null})}><option value="">{text('Not decided yet', 'अभी तय नहीं किया')}</option>{Array.from({length: 11}, (_, n) => year + n).map(y => <option key={y} value={y}>{y}</option>)}</select></label>
      <label>{text('About your preparation (optional)', 'अपनी तैयारी के बारे में (वैकल्पिक)')}<textarea rows={4} maxLength={500} value={draft.bio} placeholder={text('Your motivation, current stage or study goal…', 'आपकी प्रेरणा, तैयारी का स्तर या अध्ययन लक्ष्य…')} onChange={e => setDraft({...draft, bio: e.target.value})}/><small>{draft.bio.length}/500</small></label>
      {error && <div className="account-error" role="alert">{error}</div>}<Button disabled={busy} type="submit">{busy && <LoaderCircle className="animate-spin"/>}{text('Save profile', 'प्रोफाइल सेव करें')}</Button>
    </form></SheetContent></Sheet>;
}

export default function App() {
  const [user, setUser] = useState<Profile | null>(null), [access, setAccess] = useState<Access>({registrationEnabled: false, inviteRequired: false});
  const [loading, setLoading] = useState(true), [error, setError] = useState(''), [profileOpen, setProfileOpen] = useState(false);
  async function boot() {
    setLoading(true); setError('');
    try {
      setAccess(await api<Access>('/api/auth/config'));
      try {setUser(await api<Profile>('/api/auth/me'));} catch (e) {if (!(e instanceof ApiError) || e.status !== 401) throw e; setUser(null);}
    } catch (e) {setError(message(e, 'en'));} finally {setLoading(false);}
  }
  useEffect(() => {
    boot();
    const expired = () => {setUser(null); setProfileOpen(false); resetCsrf();};
    window.addEventListener('desk-session-expired', expired);
    return () => window.removeEventListener('desk-session-expired', expired);
  }, []);
  async function signOut() {
    try {await api('/api/auth/logout', 'POST'); resetCsrf(); setUser(null); setProfileOpen(false);}
    catch (e) {toast.error(message(e, 'en'));}
  }
  if (loading || error) return <div className="account-loading"><BookOpen size={36}/><h1>Pariksharti</h1>{loading ? <p>Opening your preparation desk…</p> : <><p role="alert">{error}</p>{import.meta.env.DEV && <p>Start the Spring Boot backend on port 8081, then retry.</p>}<Button onClick={boot}>Retry / फिर कोशिश करें</Button></>}</div>;
  return <>{user ? <AuthContext.Provider value={{user, openProfile: () => setProfileOpen(true), signOut}}><Planner key={user.id}/><ProfileSheet user={user} open={profileOpen} onClose={() => setProfileOpen(false)} onSave={setUser}/></AuthContext.Provider> : <SignIn access={access} onLogin={(profile, isNew) => {setUser(profile); setProfileOpen(isNew);}}/>}<Toaster richColors position="bottom-right"/></>;
}
