# Pehle VS Code par chalayein

Yeh **Java backend + React frontend** wala naya edition hai. ZIP extract karke `UPSC-Planner-Project/java-react` ko VS Code mein **File → Open Folder** se kholein. Poore ZIP ka parent folder kholne ke bajaye isi folder ko project root rakhein.

## 1. Ek baar software install karein

1. [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17) install karein. JRE ke bajaye **JDK** chunein. Installer mein JAVA_HOME/PATH options enable karein.
2. [Node.js 24 LTS](https://nodejs.org/en/download) install karein. npm saath mein aata hai.
3. [VS Code](https://code.visualstudio.com/) aur [Git](https://git-scm.com/downloads) install karein.
4. VS Code extensions: **Extension Pack for Java**, **Spring Boot Extension Pack**, **Tailwind CSS IntelliSense**. Is project mein recommendations bhi hain.
5. Installation ke baad VS Code close/open karein. Terminal → New Terminal mein check karein:

```powershell
java -version
node -v
npm.cmd -v
git --version
```

Java 17, Node 24 aur version numbers dikhne chahiye. Maven manually install karne ki zarurat nahi: `mvnw.cmd` pehli baar Maven download karega. Pehli run/install ko internet chahiye.

## 2. Backend: pehla terminal

Terminal ka current folder `java-react` hona chahiye. Windows PowerShell mein:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```sh
cd backend
chmod +x mvnw
./mvnw spring-boot:run
```

Pehli baar dependencies download hongi, isliye kuch minute lag sakte hain. Log mein **Started DeskApplication** aane ke baad backend taiyar hai. Browser mein http://localhost:8081/api/health kholne par `{"status":"ok"}` aana chahiye. Is terminal ko chalta rehne dein.

## 3. Frontend: doosra terminal

VS Code mein **Terminal → New Terminal**. Naya terminal project root `java-react` se shuru karein:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Linux/macOS mein `npm ci` aur `npm run dev` likhein. Har run par `npm ci` zaroori nahi; pehli installation ya dependencies badalne par karein.

Browser: **http://localhost:5173**. Hamesha isi URL par login karein. Frontend `/api` requests backend ke port 8081 tak proxy karta hai.

## 4. Apna account aur profile

1. **New here? Create an account** dabayein.
2. Naam, apna email, kam se kam 10 characters ka password aur confirmation bharein. Local setup mein invitation code nahi chahiye.
3. Account banne par sign in ho jayega. Profile mein city, target attempt aur preparation ke baare mein likh sakte hain; **Save profile** karein.
4. Sidebar mein apna naam dabakar ya topbar se **My profile** dobara kholein.
5. **Study plan** mein date, months, daily hours aur optional choose karke plan banayein.
6. **Syllabus & progress** mein topics complete/in progress mark karein.
7. **Preparation guide** mein answer likhkar **Save draft** dabayein. Drafts auto-save nahi hote.
8. Sign out karke doosra account banayein. Dono ke notes/progress alag rahenge.

Email is edition mein verify nahi hota; apna ya ek test email use kar sakte hain. Koi default/demo login nahi diya gaya hai. Password ke 72 UTF-8 bytes ki upper limit hai; Hindi letters multiple bytes lete hain.

## 5. Data kahan save hota hai?

Local mode mein `backend/data/preparation-desk.mv.db` H2 database file banegi. Backend stop/start karne ke baad account, profile aur progress wapas milenge. Login session restart par khatam hota hai; phir sign in karein.

Backup ke liye pehle backend **Ctrl+C** se stop karein, phir `backend/data` folder ko kisi doosri jagah copy karein. Is folder ko GitHub par upload na karein. Purane hosted planner ka data is database mein apne aap nahi aayega.

## 6. Code kahan edit karein?

| Kaam | File/folder |
|---|---|
| Main dashboard | `frontend/src/app/planner.tsx` |
| Login, registration, profile | `frontend/src/App.tsx` |
| Futuristic colours | `frontend/src/app/futuristic.css`, `account.css` |
| Syllabus/books | `frontend/src/lib/syllabus.ts` |
| Beginner guide/practice questions | `frontend/src/lib/study-guide.ts` |
| Hindi translations | `frontend/src/lib/locale.ts` |
| shadcn components | `frontend/src/components/ui/` |
| Authentication/profile API | `backend/src/main/java/in/pariksharti/desk/controller/AuthController.java` |
| User creation/profile persistence | `backend/src/main/java/in/pariksharti/desk/service/AccountService.java` |
| Planner validation and saving | `backend/src/main/java/in/pariksharti/desk/service/PlannerService.java` |
| Database schema | `backend/src/main/resources/db/migration/` |

React changes dev mode mein browser par refresh ho jaate hain. Java changes ke baad backend ko Ctrl+C se stop karke run command dobara chalayein.

Topic IDs ya optional list badalne par frontend terminal mein `npm.cmd run export:catalog` chalayein. Backend ke validator ko naya catalogue milega.

## 7. Problems aur fixes

| Problem | Fix |
|---|---|
| `java` not recognized / JAVA_HOME wrong | JDK 17 install/PATH set karein, VS Code restart karein. JAVA_HOME JDK root ho, `bin` folder nahi. |
| `npm.ps1 cannot be loaded` | PowerShell mein `npm.cmd` use karein, jaise commands upar hain. |
| Backend connect nahi hota | Pehle backend terminal logs aur `/api/health` check karein. Frontend port 5173 se kholein. |
| Port 8081/5173 already in use | Purana backend/frontend terminal Ctrl+C se stop karein. Do copies ek saath na chalayein. Oracle ka 8080 port use hota ho to local backend 8081 par hi rakhein. |
| Password mismatch / sign-in fail | Email aur password check karein; signup par password kam se kam 10 characters. 8 wrong attempts ke baad 15 minute wait. |
| `Forbidden` after backend restart | Browser refresh karein aur sign in dobara karein. |
| DB already in use | Isi `backend/data` file par doosra Java process chal raha ho to use stop karein. |
| Maven/npm download failed | Internet/proxy/firewall check karein; wahi command dobara chalayein. |

Local checks pass hone ke baad [GitHub guide](02-GITHUB-HINDI.md) follow karein.
