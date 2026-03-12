/**
 * OAuth2 client e geração de URL de autorização para Google Calendar.
 * Usa variáveis de ambiente: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GOOGLE_CALENDAR_REDIRECT_URI
 */

import { google } from 'googleapis';

export const GOOGLE_CALENDAR_SCOPES: string[] = [
  'https://www.googleapis.com/auth/calendar',
  'https://www.googleapis.com/auth/calendar.events',
];

export function createOAuth2Client(
  clientId: string,
  clientSecret: string,
  redirectUri: string,
) {
  return new google.auth.OAuth2(clientId, clientSecret, redirectUri);
}

export function getAuthorizationUrl(
  oauth2Client: ReturnType<typeof createOAuth2Client>,
  state: string,
): string {
  return oauth2Client.generateAuthUrl({
    access_type: 'offline',
    prompt: 'consent', // Força refresh_token na primeira autorização
    scope: GOOGLE_CALENDAR_SCOPES,
    state,
  });
}

export function parseState(state: string): { userId?: string; clinicId?: string | null; ts?: number } | null {
  try {
    const decoded = Buffer.from(state, 'base64url').toString();
    return JSON.parse(decoded) as { userId?: string; clinicId?: string | null; ts?: number };
  } catch {
    return null;
  }
}

export function buildState(userId: string, clinicId: string | null): string {
  return Buffer.from(
    JSON.stringify({ userId, clinicId, ts: Date.now() }),
  ).toString('base64url');
}

export const STATE_MAX_AGE_MS = 10 * 60 * 1000; // 10 minutos

export function isStateValid(parsed: { ts?: number } | null): boolean {
  if (!parsed?.ts) return false;
  return Date.now() - parsed.ts < STATE_MAX_AGE_MS;
}
