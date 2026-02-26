import { SetMetadata } from '@nestjs/common';

export const SKIP_CLINIC_CONTEXT = 'skipClinicContext';

/**
 * Marca rota/controller para não exigir contexto de clínica (X-Clinic-Id).
 * Usado em: /auth/me, /clinics (listar minhas clínicas), etc.
 */
export const SkipClinicContext = () => SetMetadata(SKIP_CLINIC_CONTEXT, true);
