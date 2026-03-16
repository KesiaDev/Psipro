import { SetMetadata } from '@nestjs/common';

export const ROLES_KEY = 'roles';

/**
 * Define quais roles podem acessar o endpoint.
 * Roles: admin | psychologist | assistant
 * Mapeamento ClinicUser.role: owner/admin → admin; psychologist → psychologist; assistant → assistant
 */
export const Roles = (...roles: string[]) => SetMetadata(ROLES_KEY, roles);
