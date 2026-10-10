# Pariksharti ko free mein online chalane ki poori guide

Yeh guide Windows aur VS Code ke beginner user ke liye hai. Ismein pehle GitHub par code rakhna, phir free database banana, phir Render par app deploy karna, aur ant mein online app check karna bataya gaya hai.

> **Important:** App ko internet par sach mein publish karne ke liye aapke apne GitHub, Neon aur Render accounts chahiye. Account password ya database password kisi ko chat mein mat bhejein. Sirf free plans choose karein; provider ke terms/limits badal sakte hain.

## Aapko kya milega?

- Website aur API ek hi Render URL par.
- React website + Java 17 backend, Docker se automatically build honge.
- PostgreSQL mein account aur study data, Neon Free par.
- Free hosting ki limit: Render kuch der use na hone par app ko sula deta hai. Pehli visit par app ko jagne mein ek minute ya usse zyada lag sakta hai. Free plan hamesha-on service ki guarantee nahi hai.

## Shuru karne se pehle

1. Is project ka sahi folder `java-react` hai. Is folder ke andar `backend`, `frontend`, `Dockerfile`, `render.yaml` aur `README.md` dikhne chahiye.
2. GitHub, Neon aur Render par apne accounts banayein/sign in karein. Ek hi email rakhna aasaan rahega.
3. Kisi step par paid plan/upgrade dikhai de, use select na karein. Sirf **Free** plan use karein.

## Hissa 1 — Code ko GitHub par rakhein

### 1. GitHub par khaali repository banayein

1. GitHub par sign in karein.
2. **New repository** chunein.
3. Repository ka naam `pariksharti-desk` rakhein.
4. **Private** select karein.
5. README, `.gitignore` ya license ko GitHub se initialize na karein—yeh files project mein pehle se hain.
6. **Create repository** karein. Agle page par jo HTTPS URL milega, use copy karein. Woh kuch is tarah dikhega: `https://github.com/USERNAME/pariksharti-desk.git`.

### 2. VS Code se project upload karein

1. VS Code mein project kholo. **Terminal → New Terminal** dabayein.
2. Neeche ka command chala kar project ke sahi folder mein jaayein:

```powershell
Set-Location -LiteralPath "$HOME\Downloads\PlanS(UPSC)\UPSC-Planner-Project\java-react"
```

Agar folder kisi aur jagah hai, to command mein path ko apne `java-react` folder ke path se badlein. Phir yeh check karein:

```powershell
Get-ChildItem -Name
```

List mein `backend`, `frontend`, `Dockerfile` aur `render.yaml` hone chahiye. Agar nahi hain, to sahi `java-react` folder kholein.

3. Git install/check karein:

```powershell
git --version
```

Agar `git` nahi milta, Git for Windows install karein, VS Code band karke dobara kholein, aur yahin se aage karein.

4. Yeh commands **ek-ek karke** chalaayein. Pehle apna naam/email set karein; quotes ke andar apni details likhein:

```powershell
git init
git config user.name "Aapka Naam"
git config user.email "Aapka GitHub email"
git add .
git status --short
```

`git status --short` mein project files dikhni chahiye. `.env`, password, database details ya `node_modules` upload nahi hone chahiye. Project ka `.gitignore` in local/generated files ko ignore karta hai. Agar koi secret file list mein dikhe, **aage mat badhein**—use GitHub par upload na karein.

5. Apna pehla commit aur upload karein. Pehle GitHub repository page se copy kiya hua URL lagayein:

```powershell
git commit -m "Upload preparation desk app"
git branch -M main
git remote add origin "https://github.com/USERNAME/pariksharti-desk.git"
git push -u origin main
```

`USERNAME` ko apne GitHub username se badlein. GitHub browser sign-in khole to sign in karein. **GitHub password/token ko command mein mat likhein.**

Agar `remote origin already exists` aaye, yeh dekhein:

```powershell
git remote -v
```

URL sahi ho to dobara remote add na karein. URL galat ho to:

```powershell
git remote set-url origin "https://github.com/USERNAME/pariksharti-desk.git"
git push -u origin main
```

GitHub repository page refresh karein. Uske main page par `backend`, `frontend`, `Dockerfile`, `render.yaml` aur `README.md` dikhne chahiye. **Agar `java-react` naam ka ek folder dikhe aur uske andar code ho, to repo galat folder se upload hua hai.** Render ko wahi repository chahiye jiske root par Dockerfile hai.

## Hissa 2 — Free PostgreSQL database banayein (Neon)

1. Neon website par account banayein/sign in karein.
2. **New project** dabayein; project ka naam `pariksharti-desk` rakh sakte hain.
3. **Free** plan choose karein. Region pooche to baad mein Render ke region ke paas wala chunein.
4. Project banne ke baad **Connect** dabayein. Aapko **Connect to your branch** panel dikhega. Is panel mein branch `production`, database `neondb` aur role `neondb_owner` dikh sakte hain—yeh theek hai.
5. Connection options mein **Connection pooling** ka toggle/dropdown dekhein. Use **off / Direct connection** karein. Phir connection string dobara copy karein. Direct host mein `-pooler` nahi hota. Host, database, role aur password ko alag-alag pehchanein; password ko safe jagah par rakhein.
6. Aapki connection-string line aam taur par is format ki hogi (neeche ke capital placeholder asli values nahi hain):

```text
postgresql://ROLE:PASSWORD@DIRECT_HOST/DATABASE?sslmode=require
```

Ismein `@` ke baad aur agle `/` se pehle wala hissa **host** hai; `/` ke baad wala hissa **database** hai. Neon ke panel mein database `neondb` aur role `neondb_owner` dikh rahe hon to Render mein bhi yehi values use karein. **Pooled string** ke host mein `-pooler` aa sakta hai—use yahan direct host ke roop mein mat daalein. Direct option nahi mil raha ho to Connect panel ke connection type/pooling control ko dekhein; host ko khud se guess karke edit na karein.

7. **URL ko poora copy karke Render mein mat daalein.** Neon ki direct connection string mein se sirf host copy karke Render ka `DB_URL` banayein:

	- Neon ki string mein `@` ke baad aur uske baad aane wale `/` se pehle jo text hai, wahi **host** hai. Us host ko copy karein. Usmein `-pooler` nahi hona chahiye.
	- Neeche ke URL mein `DIRECT_HOST` ko apne copied host se replace karein. Baaki text jaisa hai waisa hi rehne dein:

	```text
	jdbc:postgresql://DIRECT_HOST/neondb?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory
	```

	- Is URL mein `postgresql://` ke aage sirf `jdbc:` add hua hai. Username aur password URL mein **nahi** hain. `neondb` aapka database name hai.
	- Render → **Environment** mein teen alag boxes mein yeh daalein:

| Render key | Neon se kaunsi value |
|---|---|
| `DB_URL` | Upar banaya JDBC URL (ismein password nahi hota) |
| `DB_USERNAME` | Role, aapke panel mein `neondb_owner` |
| `DB_PASSWORD` | Neon ka **naya/reset kiya hua** role password |

Password ko URL mein mat jodein. Agar host pehchanna ya URL banana ab bhi mushkil ho, Neon ke Connect panel mein pooling off karke nayi direct connection string copy karein; us poori string ko chat mein bhejne ke bajaye usmein se sirf host wala hissa dekhein. **Connection string mein password hota hai.** Agar woh pehle chat/public jagah par share hua hai, Neon mein **Role → Reset password** karke naya password banayein aur Render ke `DB_PASSWORD` mein wahi naya password daalein.

## Hissa 3 — Render par website/API deploy karein

### 1. Web Service banayein

1. Render par sign in karein.
2. **New + → Web Service** chunein.
3. GitHub connect karein, `pariksharti-desk` repository ko access dein, phir isi repository ko select karein.
4. Service settings is tarah rakhein:

| Setting | Kya select/type karna hai |
|---|---|
| Name | `pariksharti-desk` (agar naam available na ho to koi unique naam) |
| Branch | `main` |
| Root Directory | **Khali chhodein**—GitHub repo ke root mein hi Dockerfile hona chahiye |
| Runtime | **Docker** |
| Dockerfile Path | `./Dockerfile` |
| Docker Context | `.` / repository root |
| Instance Type / Plan | **Free** |
| Health Check Path | `/api/health` |
| Region | Neon ke paas ka available region, agar option mile |

Docker choose karne ke baad alag se Java ya Node build command mat daalein. `Dockerfile` frontend aur backend dono ko khud build karta hai.

### 2. Render par environment values daalein

Service create karne se pehle **Environment** section kholein. **Add Environment Variable** se neeche diye gaye exact naam aur values add karein:

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | Hissa 2 wala JDBC URL, asli Neon host/database ke saath |
| `DB_USERNAME` | Neon ka username/role |
| `DB_PASSWORD` | Neon ka database password |
| `APP_REGISTRATION_ENABLED` | `true` |

Signup ke liye invitation code ki zaroorat nahi hai. Har user **Create account** par apna naam, email aur password dekar account bana sakta hai. Database username/password **sirf Render ke Environment page** mein daalein—source file, GitHub ya chat mein nahi. Key names mein spelling/uppercase bilkul same rakhein.

### 3. Deploy shuru karein

1. Settings dobara check karein: Docker, Free plan, repo root khali, health path `/api/health`.
2. **Create Web Service** ya **Deploy** dabayein.
3. Render ke **Events/Logs** page par build dekhein. Pehli build mein kai minutes lag sakte hain; page band karne ki zaroorat nahi.
4. Build successful ho aur service status **Live** aaye, to Render jo `https://...onrender.com` URL de, use copy karein.

Build fail ho to Logs ka error dekhein. Sabse aam galtiyan: galat GitHub root, Dockerfile path galat, ya environment value missing. Neeche “Agar app nahi chale” section dekhein.

## Hissa 4 — Confirm karein ki online app chal rahi hai

1. Apna Render URL browser mein kholein, jaise `https://YOUR-SERVICE.onrender.com`. Pariksharti website dikhni chahiye.
2. Health check alag tab mein kholein: `https://YOUR-SERVICE.onrender.com/api/health`. Jawab yeh hona chahiye:

```json
{"status":"ok"}
```

3. Website par **Create account** karein. Apna naam, email aur password daalein; invitation code nahi maanga jayega.
4. Sign in karke profile ka naam/city save karein, ek planner topic update karein, aur page reload karke dekhein ki data bana hua hai.
5. Sign out karke dobara sign in karein. Profile aur progress wapas dikhni chahiye. Yeh confirm karega ki data Neon database mein save hua hai.

## Agar app nahi chale

| Kya dikh raha hai | Kya check karein |
|---|---|
| `Dockerfile not found` / build config error | GitHub repo ke root par `Dockerfile`, `backend` aur `frontend` hone chahiye. Agar root par `java-react` folder hi dikh raha hai, repo ko `java-react` ke contents se dobara banayein. |
| `DB_URL`/database config missing | Render → service → Environment mein `SPRING_PROFILES_ACTIVE=prod` aur teeno `DB_...` values check karein. Save ke baad redeploy karein. |
| Database login/authentication failure | Neon ka username/password dobara copy karein. URL ka host direct host ho, `-pooler` wala nahi; password URL mein nahi hona chahiye. |
| SSL/certificate error | JDBC URL mein `sslmode=verify-full` aur `sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory` dono maujood hon. |
| Registration band hone ka error | Render → service → Environment mein `APP_REGISTRATION_ENABLED=true` set karein, phir redeploy karein. |
| App khulne mein bahut time le / pehla request timeout ho | Free Render service idle hone par sleep karti hai. Ek minute dein, phir refresh karein aur Render Logs dekhein. |
| Website ke bajaye 404 ya 502 | Render service **Live** hai ya nahi dekhein; deploy/build logs check karein; service URL ke saath `/api/health` kholein. |
| Render service suspend/build limit error | Render aur Neon dashboard mein free usage/limits check karein. Paid upgrade zaroori nahi—quota reset hone tak ruk sakte hain. |

## Free plan ki zaroori baatein

- Render Free app ko idle hone par sula sakta hai. Isliye har visit par turant response ki guarantee nahi.
- Free plan aur database limits hain; high traffic/important business ke liye ise production guarantee na samjhein. Dashboard par current terms check karein.
- Is app ka login session restart/sleep ke baad khatam ho sakta hai; dobara sign in karein. Profile/progress PostgreSQL mein saved rehna chahiye.
- Local computer par bane accounts cloud database mein automatically copy nahi honge; online app par naya account banayein.
- Password bhoolne par email reset flow is app mein nahi hai. Apna password safe jagah par rakhein.
- Database credentials private rakhein. Agar galti se GitHub par upload ho gaye hon, turant rotate/change karein.

**Bas!** Agar health URL `{"status":"ok"}` dikhata hai, website khul rahi hai, aur account bana kar data save/reload ho raha hai, to deployment complete hai.
