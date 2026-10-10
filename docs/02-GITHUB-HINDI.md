# Local project ko GitHub par bhejein

Pehle local login/profile, progress saving aur build check kar lein. **Sirf `java-react` folder** ko repository root banayein. `original-sites` aur `self-hosted` purane editions hain; is Java deployment ke liye unhe repo mein daalne ki zarurat nahi.

## 1. Local checks

Frontend terminal mein:

```powershell
npm.cmd test
npm.cmd run build
```

Backend terminal ko Ctrl+C se stop karke:

```powershell
.\mvnw.cmd test
```

## 2. Empty GitHub repository banayein

1. [GitHub](https://github.com/) par sign in/sign up karein.
2. **New repository** → name `pariksharti-desk`.
3. Shuru mein **Private** select karein. Render ko is private repo ka access de sakte hain.
4. **Add README**, `.gitignore` aur license initialize na karein; source mein README/.gitignore pehle se hain.
5. **Create repository**. HTTPS URL copy karein, jaise `https://github.com/YOUR_USERNAME/pariksharti-desk.git`.

## 3. VS Code terminal se push

Naya terminal **`java-react` root folder** par kholein. `backend` ya `frontend` ke andar se `git init` mat karein.

```powershell
git init
git config user.name "YOUR_NAME"
git config user.email "YOUR_GITHUB_EMAIL_OR_NOREPLY_EMAIL"
git add .
git status
```

`YOUR_NAME` aur email ko apne details se replace karein. Yeh repo ke commit author details hain. `git status` mein source files dikhni chahiye; `data`, `node_modules`, `target`, passwords ya real database credentials nahi dikhne chahiye. Included `.gitignore` in generated/local files ko ignore karta hai.

Phir:

```powershell
git commit -m "Add Java React preparation desk with user accounts"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/pariksharti-desk.git
git push -u origin main
```

Repo URL mein `YOUR_USERNAME` replace karein. Git Credential Manager/browser sign-in khule to GitHub login complete karein. HTTPS Git push ke liye account password directly use nahi hota; browser-based authentication use karein. Token ko URL ya source file mein paste na karein.

GitHub page refresh karein: `backend`, `frontend`, `Dockerfile`, `README.md`, `render.yaml` root par dikhne chahiye.

## 4. GitHub Actions

Included workflow frontend tests/build aur backend tests/jar build chalata hai. GitHub → **Actions** mein result dekhein. Private repositories par included Actions minutes ki limit hoti hai; account usage mein check karein. Workflow kisi host par deploy nahi karta.

## 5. Aage changes bhejna

Code edit aur local checks ke baad root terminal mein:

```powershell
git add .
git commit -m "Describe your change"
git push
```

Render connect hone ke baad main branch ke naye commits deployment trigger kar sakte hain, service ke auto-deploy setting ke hisaab se. GitHub code rakhta hai; Render application chalata hai; Neon database data rakhta hai.

## Common errors

| Error | Fix |
|---|---|
| `Author identity unknown` | Upar `git config user.name` aur `user.email` set karein. |
| `remote origin already exists` | `git remote -v` check karein; sahi repo ho to dobara add na karein. Galat ho to `git remote set-url origin YOUR_REPO_URL`. |
| Repository not found / permission denied | URL aur logged-in GitHub account check karein; private repo ka access hona chahiye. |
| `non-fast-forward` on first push | Remote empty nahi hai. Beginner route: naya empty repo banakar uska URL use karein; `--force` use na karein. |
| `nothing to commit` | Koi naya source change nahi hai, ya files pehle commit ho chuki hain. |

Official reference: [GitHub: adding locally hosted code](https://docs.github.com/en/migrations/importing-source-code/using-the-command-line-to-import-source-code/adding-locally-hosted-code-to-github).

Agla step: [Render + Neon deployment](03-FREE-DEPLOYMENT-HINDI.md).
