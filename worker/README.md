# Zubair Social — Upload Worker

Cloudflare Worker (TypeScript) jo app ko R2 par image upload karne ke liye **temporary presigned URL** deta hai.

**Kyun zaroori hai?** App mein R2 ki secret key rakh nahi sakte (APK se nikal li jati hai). Isliye key sirf Worker ke paas hai. App Worker se kehti hai "mujhe upload URL do", Worker pehle check karta hai ke request asli logged-in Firebase user ki hai, phir 5 minute ka URL deta hai.

```
App --(Firebase ID token)--> Worker --(verify + presign)--> App --(PUT image)--> R2
```

## Worker kya karta hai (src/index.ts)

1. **Sirf `POST /get-upload-url`** — baaki sab par 404 / 405.
2. **Token verify:** `Authorization: Bearer <Firebase ID token>`. Google ki public keys (JWKS) se signature check hota hai, saath `issuer` (`https://securetoken.google.com/zubair-social`), `audience` (project id) aur expiry. Fail ho to **401** — R2 tak baat jati hi nahi.
3. **Key:** `posts/{uid}/{timestamp}.jpg`. `uid` token se aata hai, client se nahi, isliye koi dusre ke folder mein upload nahi kar sakta.
4. **Presign:** `aws4fetch` R2 ke S3-compatible endpoint ke liye signature URL ke query mein daalta hai. `X-Amz-Expires=300` (5 min). `Content-Type: image/jpeg` signature mein shamil hai, to app ko PUT mein wahi header bhejna hoga.
5. **Response:** `uploadUrl` (PUT yahan), `imageUrl` (final public URL, RTDB mein yahi save hoga), `key`, `expiresInSeconds`.

## Deploy steps

### Step 1 — R2 bucket banao
Cloudflare dashboard → **R2 Object Storage → Create bucket** → naam `zubair-social-images` (ya `wrangler.toml` ka `R2_BUCKET` apne naam se badlo).

### Step 2 — Bucket ko public read karo
Bucket → **Settings → Public access** → `r2.dev` subdomain **Allow** karo (ya apna custom domain jodo). Jo URL mile (jaise `https://pub-xxxx.r2.dev`) wo `wrangler.toml` mein `PUBLIC_BASE_URL` mein daalo.
> Sirf *read* public hota hai. Upload ke liye abhi bhi signed URL chahiye.

### Step 3 — R2 API token (yeh hain "R2 keys")
R2 overview → **Manage R2 API Tokens → Create API token** → permission **Object Read & Write**, sirf isi bucket par. Aap ko milega:
- **Access Key ID**
- **Secret Access Key** (sirf ek baar dikhta hai!)

Isi page par **Account ID** bhi hota hai → `wrangler.toml` ke `R2_ACCOUNT_ID` mein daalo.

### Step 4 — Worker install + login
```bash
cd worker
npm install
npx wrangler login
```

### Step 5 — Secrets set karo (yeh git mein kabhi nahi jate)
```bash
npx wrangler secret put R2_ACCESS_KEY_ID
npx wrangler secret put R2_SECRET_ACCESS_KEY
```
Har command value maangti hai, wahan paste karo.

### Step 6 — Deploy
```bash
npx wrangler deploy
```
Aakhir mein URL milega: `https://zubair-social-worker.<aap-ka-subdomain>.workers.dev`

### Local test (optional)
`worker/.dev.vars` file banao (yeh git-ignored hai):
```
R2_ACCESS_KEY_ID=...
R2_SECRET_ACCESS_KEY=...
```
phir `npm run dev` (port 8787).

## curl se test

Pehle apna Firebase ID token lo. `WEB_API_KEY` `app/google-services.json` mein `current_key` hai.

```bash
export WEB_API_KEY="<google-services.json wali current_key>"
export WORKER="https://zubair-social-worker.<subdomain>.workers.dev"

TOKEN=$(curl -s "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$WEB_API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{"email":"AAP_KA_EMAIL","password":"AAP_KA_PASSWORD","returnSecureToken":true}' | jq -r .idToken)
```

Upload URL maango:
```bash
curl -s -X POST "$WORKER/get-upload-url" -H "Authorization: Bearer $TOKEN" | jq
```
Aisa jawab aana chahiye:
```json
{
  "uploadUrl": "https://<account>.r2.cloudflarestorage.com/zubair-social-images/posts/<uid>/1730000000000.jpg?X-Amz-...",
  "method": "PUT",
  "contentType": "image/jpeg",
  "expiresInSeconds": 300,
  "key": "posts/<uid>/1730000000000.jpg",
  "imageUrl": "https://pub-xxxx.r2.dev/posts/<uid>/1730000000000.jpg"
}
```

Asli upload (Content-Type **zaroor** `image/jpeg`):
```bash
RESP=$(curl -s -X POST "$WORKER/get-upload-url" -H "Authorization: Bearer $TOKEN")
curl -i -X PUT -H "Content-Type: image/jpeg" --data-binary @photo.jpg "$(echo $RESP | jq -r .uploadUrl)"
echo $RESP | jq -r .imageUrl      # browser mein kholo, photo dikhni chahiye
```

Reject hone chahiye (security check):
```bash
curl -i -X POST "$WORKER/get-upload-url"                                    # 401: token nahi
curl -i -X POST "$WORKER/get-upload-url" -H "Authorization: Bearer abc.def.ghi"   # 401: fake token
curl -i "$WORKER/get-upload-url"                                            # 405: GET allowed nahi
```

## Dhyan rakhne wali baatein
- `R2_ACCESS_KEY_ID` / `R2_SECRET_ACCESS_KEY` sirf `wrangler secret` mein hon. `wrangler.toml` ke `[vars]` mein sirf non-secret cheezein hain.
- Key mein `Date.now()` hai (ms). Ek hi user ka ek hi millisecond mein do upload karna practically mumkin nahi, lekin zarurat pade to key mein random suffix jod sakte hain.
- Worker abhi file ka size ya asli type check nahi karta (presigned URL se aisa limit lagana mushkil hai). MVP ke liye theek hai; baad mein size limit ya post-upload check jod sakte hain.
- Worker ko koi limit/rate-limit nahi di gayi: koi bhi valid user bar bar URL maang sakta hai.
