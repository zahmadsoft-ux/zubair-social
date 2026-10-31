import { createRemoteJWKSet, jwtVerify } from "jose";
import { AwsClient } from "aws4fetch";

export interface Env {
  // wrangler.toml [vars] — secret nahi
  FIREBASE_PROJECT_ID: string;
  R2_ACCOUNT_ID: string;
  R2_BUCKET: string;
  PUBLIC_BASE_URL: string;
  // `wrangler secret put` se — sirf Worker ke andar, app ya repo mein kabhi nahi
  R2_ACCESS_KEY_ID: string;
  R2_SECRET_ACCESS_KEY: string;
}

// Google Firebase ID tokens ki public keys (JWKS). jose inhein fetch karke cache kar leta hai,
// aur key rotate hone par khud naya fetch karta hai. Module level par hai taake har request par dobara na bane.
const FIREBASE_JWKS = createRemoteJWKSet(
  new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"),
);

const UPLOAD_URL_TTL_SECONDS = 300; // presigned URL 5 minute valid
const IMAGE_CONTENT_TYPE = "image/jpeg";

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });

/** "Authorization: Bearer <token>" se token nikalo, Firebase ke asli signature/issuer/audience se verify karo, uid lautao. */
async function verifyFirebaseToken(request: Request, projectId: string): Promise<string | null> {
  const match = /^Bearer (.+)$/.exec(request.headers.get("Authorization") ?? "");
  if (!match) return null;
  try {
    const { payload } = await jwtVerify(match[1], FIREBASE_JWKS, {
      issuer: `https://securetoken.google.com/${projectId}`, // token isi project ka ho
      audience: projectId,
      algorithms: ["RS256"],
    }); // exp (expiry) bhi yahin check ho jati hai
    return typeof payload.sub === "string" && payload.sub.length > 0 ? payload.sub : null;
  } catch {
    return null; // signature galat, expired, ya kisi aur project ka token
  }
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    if (url.pathname !== "/get-upload-url") return json({ error: "Not found" }, 404);
    if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);

    // 1) Pehle pehchano: token verify na ho to R2 ke paas jate hi nahi.
    const uid = await verifyFirebaseToken(request, env.FIREBASE_PROJECT_ID);
    if (!uid) return json({ error: "Unauthorized" }, 401);

    if (!env.R2_ACCESS_KEY_ID || !env.R2_SECRET_ACCESS_KEY) {
      console.error("R2 secrets set nahi hain (wrangler secret put)");
      return json({ error: "Server misconfigured" }, 500);
    }

    // 2) Key sirf is user ke folder mein: client key chun hi nahi sakta, isliye dusre ke folder mein likh nahi sakta.
    const key = `posts/${uid}/${Date.now()}.jpg`;

    // 3) R2 ka S3-compatible endpoint, aur presign (signature URL ke query mein).
    const objectUrl = new URL(`https://${env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com/${env.R2_BUCKET}/${key}`);
    objectUrl.searchParams.set("X-Amz-Expires", String(UPLOAD_URL_TTL_SECONDS));

    const aws = new AwsClient({
      accessKeyId: env.R2_ACCESS_KEY_ID,
      secretAccessKey: env.R2_SECRET_ACCESS_KEY,
      service: "s3",
      region: "auto", // R2 mein region hamesha "auto"
    });
    // Content-Type signature mein shamil hai: app ko PUT mein bhi wahi bhejna hoga, warna R2 reject karega.
    const signed = await aws.sign(
      new Request(objectUrl, { method: "PUT", headers: { "Content-Type": IMAGE_CONTENT_TYPE } }),
      { aws: { signQuery: true } },
    );

    return json({
      uploadUrl: signed.url,
      method: "PUT",
      contentType: IMAGE_CONTENT_TYPE,
      expiresInSeconds: UPLOAD_URL_TTL_SECONDS,
      key,
      imageUrl: `${env.PUBLIC_BASE_URL.replace(/\/$/, "")}/${key}`,
    });
  },
} satisfies ExportedHandler<Env>;
